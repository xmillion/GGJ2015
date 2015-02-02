package com.left.addd.model;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
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

	public NPC(Type type, Tile tile) {
		this(Res.generateId(), type, tile);
	}
	
	private NPC(long id, Type type, Tile tile) {
		super(id, tile);
		this.type = type;
		this.targetEntity = null;
		this.targetTile = null;
		this.path = new LinkedList<Tile>();
		this.nextTile = tile;
		this.moveRate = type.moveRate;
		this.moveProgress = -1;
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
	
	public void update(int ticks) {
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
				int newStepsTaken = currentNode.stepsTaken + 1;
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
				while (!currentTile.equals(currentNode.previous.tile)) {
					// add tiles to the head of the path because it is in reverse order.
					path.push(currentNode.tile);
					currentNode = currentNode.previous;
				}
			} else {
				// FAILURE
				stop();
			}
		}
	}
	
	private boolean findPathToTarget() {
		if (targetEntity == null) {
			return false;
		}
		int stepsTaken = 0;
		boolean success = false;
		Tile targetTile = targetEntity.currentTile;
		Node currNode = new Node(currentTile, currentTile, 0, null);
		PriorityQueue<Node> searchList = new PriorityQueue<Node>();
		HashMap<Tile, Node> visitedNodes = new HashMap<Tile, Node>();
		searchList.add(new Node(currentTile, targetTile, stepsTaken, null));
		while(searchList.size()>0) {
			currNode = searchList.poll();
			if (currNode.tile == targetTile) {
				success = true;
				break;
			}
			int newStepsTaken = currNode.stepsTaken + 1;
			for( Tile tile : currNode.tile.getNeighbours()) {
				if(visitedNodes.get(tile) != null) {
					visitedNodes.get(tile).updateNode(newStepsTaken, currNode);
				} else {
					searchList.add(new Node(tile, targetTile, newStepsTaken, currNode));
				}
			}
			visitedNodes.put(currNode.tile, currNode);
		}
		if (success) {
			if (currNode.tile == this.currentTile) {
				return;
			}
			while (currNode.previous.tile != this.currentTile) {
				currNode = currNode.previous;
			}
			this.nextTile = currNode.tile;
		} else {
			this.nextTile = this.currentTile;
		}
		
		return success;
	}
	
	private class Node implements Comparable<Node> {
		public final Tile tile;
		int stepsTaken;
		double value;
		Node previous;
		public Node(Tile startTile, Tile endTile, int stepsTaken, Node previous) {
			this.tile = startTile;
			this.stepsTaken = stepsTaken;
			this.previous = previous;
			this.value = Math.sqrt(Math.pow(startTile.x - endTile.x, 2) + Math.pow(startTile.y - endTile.y, 2));
		}
		
		void updateNode(int stepsTaken, Node previous) {
			if (stepsTaken < this.stepsTaken) {
				this.stepsTaken = stepsTaken;
				this.previous = previous;
			}
		}
		
		int getStepsTaken() {
			return this.stepsTaken;
		}
		
		double getValue() {
			return this.stepsTaken + this.value;
		}
		
		@Override
		public int compareTo(Node other) {
			return (this.getValue() < other.getValue()) ? -1 : 1;
		}
	}
	
	// *** Serialization ***

	/**
	 * Serialize a Tile into json.
	 * 
	 * @param json Json serializer, which will now have the tile's data.
	 * @param tile Tile to save
	 */
	@Override
	public void save(Json json) {
		json.writeObjectStart("npc");
		json.writeValue("id", id);
		json.writeValue("x", currentTile.x);
		json.writeValue("y", currentTile.y);
		json.writeValue("type", type.name());
		// don't need to store movement for now
		json.writeObjectEnd();
	}

	/**
	 * Create a Tile using a json string.
	 * 
	 * @param data
	 * @return
	 */
	public static NPC load(Json json, JsonValue jsonData, GameModel gameModel) {
		long id = jsonData.getLong("id");
		int x = jsonData.getInt("x");
		int y = jsonData.getInt("y");
		Type type = Type.valueOf(jsonData.getString("type"));
		return new NPC(id, type, gameModel.getTile(x, y));
	}
}