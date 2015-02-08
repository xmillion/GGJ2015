package com.left.addd.model;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.LoadingException;
import com.left.addd.utils.Res;

public class NPC extends Entity {
	
	public enum Type {
		NONE(1, "main"),
		HERO(5, "old"),
		POLICE(3, "redshirt"),
		FACULTY(7, "blueshirt"),
		STUDENT(2, "young");
		
		public final int moveRate;
		public final String assetName;
		private Type(int moveRate, String assetName) {
			this.moveRate = moveRate;
			this.assetName = assetName;
		}
	}

	public final Type type;
	/** If not null, try to move to this entity. */
	private Entity targetEntity;
	/** Cache the target's position */
	private Tile targetTile;
	/** If not empty, NPC is moving along this list of tiles. */
	private Deque<Tile> path;
	/** Cache the neighbouring tile for movement. */
	private Tile nextTile;
	/** Number of ticks it takes to move to the next tile. */
	private int moveRate;
	/** Ticks remaining until the move completes. Negative if not moving. */
	private int moveProgress;
	
	private List<MoveStateListener<NPC>> listeners;

	public NPC(Tile tile, Type type) {
		this(Res.generateId(), "NPC", null, tile, type);
	}
	
	public NPC(String name, String description, Tile tile, Type type) {
		this(Res.generateId(), name, description, tile, type);
	}
	
	private NPC(long id, String name, String description, Tile tile, Type type) {
		super(id, name, description, tile);
		this.type = type;
		this.targetEntity = null;
		this.targetTile = null;
		this.path = new LinkedList<Tile>();
		this.nextTile = tile;
		this.moveRate = type.moveRate;
		this.moveProgress = -1;
	}
	
	public void setCurrentTile(Tile t) {
		if (!currentTile.equals(t)) {
			super.setCurrentTile(t);
			moveCompleted();
			// empty the path queue to update it
			path.clear();
			updatePath();
		}
	}
	
	public String getAssetName() {
		return type.assetName;
	}
	
	public Entity getTargetEntity() {
		return targetEntity;
	}
	
	public Tile getNextTile() {
		return nextTile;
	}

	public int getMoveRate() {
		return moveRate;
	}

	public void setMoveRate(int rate) {
		this.moveRate = rate;
	}

	public int getMoveProgress() {
		return moveProgress;
	}
	
	public boolean isMoving() {
		// All of the following are true if this is moving:
		// targetEntity != null
		// !path.isEmpty();
		// nextTile != tile;
		// moveProgress >= 0
		return moveProgress >= 0;
	}
	
	// *** Listener ***
	
	public void addMoveStateListener(MoveStateListener<NPC> listener) {
		if (!listeners.contains(listener)) {
			this.listeners.add(listener);
			// initial trigger
			if (isMoving()) {
				listener.OnMoveStarted(this);
			} else {
				listener.OnMoveCompleted(this);
			}
		}
	}
	
	public void removeMoveStateListener(MoveStateListener<NPC> listener) {
		listeners.remove(listener);
	}
	
	private void moveStarted() {
		for(MoveStateListener<NPC> listener: listeners) {
			listener.OnMoveStarted(this);
		}
	}
	
	private void moveCompleted() {
		for(MoveStateListener<NPC> listener: listeners) {
			listener.OnMoveCompleted(this);
		}
	}
	
	// *** Movement ***
	
	/**
	 * Start moving to an adjacent tile
	 */
	public void move() {
		if (!path.isEmpty()) {
			nextTile = path.poll();
		}
		if (currentTile.equals(nextTile)) {
			// we're already there.
			pause();
		} else {
			moveProgress = 0;
			moveStarted();
		}
	}
	
	/**
	 * Stop moving for the time being. Resume with move().
	 */
	public void pause() {
		nextTile = currentTile;
		moveProgress = -1;
		moveCompleted();
	}
	
	/**
	 * Stop moving and remove the current path.
	 */
	public void stop() {
		targetEntity = null;
		path.clear();
		nextTile = currentTile;
		moveProgress = -1;
		moveCompleted();
	}

	/**
	 * Complete the move to the adjacent tile.
	 */
	private void finishedMoving() {
		currentTile = nextTile;
		moveProgress = -1;
		moveCompleted();
	}
	
	@Override
	public void update(int ticks) {
		super.update(ticks);
		moveProgress += ticks;
		if (moveProgress >= moveRate) {
			finishedMoving();
			// update this.path if needed
			updatePath();
			move();
		}
	}
	
	// *** Pathfinding ***
	
	/**
	 * Update this.path using pathfinding algorithm.
	 */
	private void updatePath() {
		if (targetEntity == null) {
			// no target, don't move
			stop();
			return;
		} else if (!path.isEmpty() && targetTile.equals(targetEntity.currentTile)) {
			// no change needed for path.
			return;
		} else {
			// path is empty or target moved, need to regenerate this.path.
			targetTile = targetEntity.currentTile;
			int stepsTaken = 0;
			boolean success = false;
			Node currentNode = new Node(currentTile, targetTile, stepsTaken, null);
			PriorityQueue<Node> searchList = new PriorityQueue<Node>(); // frontier
			HashMap<Tile, Node> visitedNodes = new HashMap<Tile, Node>();
			
			searchList.add(currentNode);
			while (searchList.size() > 0) {
				currentNode = searchList.poll();
				if (targetTile.equals(currentNode.tile)) {
					success = true;
					break;
				}
				int newStepsTaken = currentNode.getStepsTaken() + 1;
				for (Tile tile: currentNode.tile.getNeighbours()) {
					if (visitedNodes.containsKey(tile)) {
						visitedNodes.get(tile).updateNode(newStepsTaken, currentNode);
					} else {
						searchList.add(new Node(tile, targetTile, newStepsTaken, currentNode));
					}
				}
				visitedNodes.put(currentNode.tile, currentNode);
			}
			if (success) {
				while (!currentTile.equals(currentNode.getPrevious().tile)) {
					// add tiles to the head of the path because it is in reverse order.
					path.push(currentNode.tile);
					currentNode = currentNode.getPrevious();
				}
			} else {
				// FAILURE
				stop();
			}
		}
	}
	
	private class Node implements Comparable<Node> {
		public final Tile tile;
		private int stepsTaken;
		private double value;
		private Node previous;
		
		public Node(Tile startTile, Tile endTile, int stepsTaken, Node previous) {
			this.tile = startTile;
			this.stepsTaken = stepsTaken;
			this.previous = previous;
			this.value = Math.sqrt(Math.pow(startTile.x - endTile.x, 2) + Math.pow(startTile.y - endTile.y, 2));
		}
		
		public void updateNode(int stepsTaken, Node previous) {
			if (stepsTaken < this.stepsTaken) {
				this.stepsTaken = stepsTaken;
				this.previous = previous;
			}
		}
		
		public int getStepsTaken() {
			return stepsTaken;
		}
		
		public double getValue() {
			return stepsTaken + value;
		}
		
		public Node getPrevious() {
			return previous;
		}
		
		@Override
		public int compareTo(Node other) {
			return (this.getValue() < other.getValue()) ? -1 : 1;
		}
	}
	
	// *** Serialization ***

	/**
	 * Turn an NPC into json.
	 * @param json
	 */
	@Override
	public void save(Json json) {
		json.writeObjectStart("npc");
		json.writeValue("id", id);
		json.writeValue("name", getName());
		json.writeValue("desc", getDescription());
		json.writeValue("x", currentTile.x);
		json.writeValue("y", currentTile.y);
		json.writeValue("type", type.name());
		// don't need to store movement for now
		json.writeObjectEnd();
	}
	
	/**
	 * Loads an NPC from stored ID.
	 * ID's can come from save data or initialization data.
	 * @param id
	 * @return
	 * @throws LoadingException if no such NPC exists with that ID.
	 */
	public static NPC load(long id) throws LoadingException {
		// TODO id based loading
		return null;
	}

	/**
	 * Loads an NPC from save data.
	 * @param jsonData
	 * @param gameModel
	 * @return
	 */
	public static NPC load(JsonValue jsonData, GameModel gameModel) {
		JsonValue npcJson = jsonData.get("npc");
		long id = npcJson.getLong("id");
		String name = npcJson.getString("name");
		String description = npcJson.getString("description");
		int x = npcJson.getInt("x");
		int y = npcJson.getInt("y");
		Type type = Type.valueOf(npcJson.getString("type"));
		return new NPC(id, name, description, gameModel.getTile(x, y), type);
	}
}