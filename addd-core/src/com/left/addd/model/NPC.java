package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.LoadingException;
import com.left.addd.utils.Res;

public class NPC extends Entity {
	
	public enum Type {
		NONE(1, "main"),
		HERO(0.2f, "old"),
		POLICE(1/3f, "redshirt"),
		FACULTY(1/7f, "blueshirt"),
		STUDENT(0.5f, "young");
		
		public final float moveSpeed;
		public final String assetName;
		private Type(float moveSpeed, String assetName) {
			this.moveSpeed = moveSpeed;
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
	/** Cache the direction of the neighbouring tile. */
	private Direction nextDirection;
	/** The speed of this NPC is measured in tiles per second (real time) */
	private float moveSpeed;
	/** The progress of this NPC's movement, from 0 (start) to 1 (complete). -1 when it's not moving. */
	private float moveProgress;
	/** Floating point form of its current tile, to account for movement. */
	private Vector2 tileCoordinate;

	public NPC(Tile tile, Type type) {
		this(Res.generateId(), "NPC", null, tile, type);
	}
	
	public NPC(String name, String description, Tile tile, Type type) {
		this(Res.generateId(), name, description, tile, type);
	}
	
	/**
	 * Full constructor for serializer
	 * @param id
	 * @param name
	 * @param description
	 * @param tile
	 * @param type
	 */
	private NPC(long id, String name, String description, Tile tile, Type type) {
		super(id, name, description, tile);
		this.type = type;
		this.targetEntity = null;
		this.targetTile = null;
		this.path = new LinkedList<Tile>();
		this.nextTile = tile;
		this.moveSpeed = type.moveSpeed;
		this.moveProgress = -1;
		this.tileCoordinate = new Vector2(tile.x, tile.y);
	}
	
	@Override
	public void setCurrentTile(Tile t) {
		if (!tile.equals(t)) {
			super.setCurrentTile(t);
			// empty the path queue to update it
			path.clear();
			if (targetEntity != null) {
				findPathToTarget();
			}
		}
	}
	
	@Override
	public String getAssetName() {
		return type.assetName;
	}
	
	public Entity getTargetEntity() {
		return targetEntity;
	}
	
	public Tile getNextTile() {
		return nextTile;
	}

	/**
	 * Gets the move rate in real time.
	 * @return
	 */
	public float getMoveSpeed() {
		return moveSpeed;
	}

	/**
	 * Sets the move rate in real time.
	 * @param speed
	 */
	public void setMoveSpeed(float speed) {
		this.moveSpeed = speed;
	}

	/**
	 * Gets the move progress in real time.
	 * @return
	 */
	public float getMoveProgress() {
		return moveProgress;
	}
	
	public Direction getMoveDirection() {
		return nextDirection;
	}
	
	public Vector2 getTileCoordinate() {
		return tileCoordinate;
	}
	
	public boolean isMoving() {
		// All of the following are true if this is moving:
		// targetEntity != null;
		// !path.isEmpty();
		// moveProgress < 0;
		return nextDirection != null;
	}
	
	// *** Movement ***
	
	/**
	 * Stop moving and remove the current path.
	 */
	public void stop() {
		targetEntity = null;
		path.clear();
		nextTile = tile;
		nextDirection = null;
		moveProgress = -1;
	}
	
	/**
	 * Start moving towards the current target if there is one.
	 */
	public void start() {
		// If this is called mid-move, I think it'll jitter.
		if (tile.equals(nextTile)) {
			// It's not moving, lets see if it should be moving.
			if (targetEntity == null) {
				// Actively move towards the next objective with a target if possible.
				for (Objective obj: getObjectives()) {
					if (!obj.isTargetComplete(this)) {
						log(getName(), getObjectives().size() + " objectives. Next up is " + obj.getTarget().getName());
						targetEntity = obj.getTarget();
						findPathToTarget();
						break;
					}
				}
			} else if (!path.isEmpty() && targetTile.equals(targetEntity.tile)) {
				// Continue along the path.
				nextTile = path.poll();
				if (!tile.equals(nextTile)) {
					nextDirection = tile.getDirection(nextTile);
					moveProgress = 0;
				}
			} else {
				// Regenerate a path and use it.
				path.clear();
				findPathToTarget();
			}
		}
	}
	
	@Override
	public void update(int ticks) {
		super.update(ticks);
		if (nextDirection == null) {
			// Start the next move.
			start();
		}
	}
	
	/**
	 * Render function for more fluid movement.
	 * @param delta
	 */
	public void render(float delta) {
		if (nextDirection != null) {
			moveProgress += delta * moveSpeed;
			if (moveProgress > 1.0f) {
				// finish moving.
				tile = nextTile;
				nextDirection = null;
				moveProgress = -1;
				tileCoordinate.set(tile.x, tile.y);
			} else {
				float moveX = tile.x;
				float moveY = tile.y;
				switch(nextDirection) {
				case NORTH:
					moveY += moveProgress;
					break;
				case EAST:
					moveX += moveProgress;
					break;
				case SOUTH:
					moveY -= moveProgress;
					break;
				case WEST:
					moveX -= moveProgress;
					break;
				}
				tileCoordinate.set(moveX, moveY);
			}
		}
	}
	
	// *** Pathfinding ***
	
	/**
	 * Update this.path using pathfinding algorithm.
	 * Ensure that targetEntity != null before calling this.
	 */
	private void findPathToTarget() {
		if (tile.equals(targetEntity.tile)) {
			// we're already here.
			targetEntity = null;
			return;
		}
		
		targetTile = targetEntity.tile;
		int stepsTaken = 0;
		boolean success = false;
		Node currentNode = new Node(tile, targetTile, stepsTaken, null);
		PriorityQueue<Node> searchList = new PriorityQueue<Node>(); // frontier
		HashMap<Tile, Node> visitedNodes = new HashMap<Tile, Node>();
		
		searchList.add(currentNode);
		while (searchList.size() > 0) {
			currentNode = searchList.poll();
			// just being in a neighbouring tile is close enough for entity interaction.
			if (targetTile.equals(currentNode.tile)) {
				success = true;
				break;
			}
			
			int newStepsTaken = currentNode.getStepsTaken() + 1;
			for (Tile tile: currentNode.tile.getNeighbours()) {
				if (Tile.isDummyTile(tile)) {
					continue;
				} else if (visitedNodes.containsKey(tile)) {
					visitedNodes.get(tile).updateNode(newStepsTaken, currentNode);
				} else if (tile.isNetwork() || tile.equals(targetTile)) {
					searchList.add(new Node(tile, targetTile, newStepsTaken, currentNode));
				}
			}
			visitedNodes.put(currentNode.tile, currentNode);
		}
		
		if (success) {
			while (!tile.equals(currentNode.tile)) {
				path.push(currentNode.tile);
				currentNode = currentNode.getPrevious();
				if (currentNode == null) {
					log("Pathfinder", "Beginning of path doesn't match current tile.");
					break;
				}
			}
		} else {
			// FAILURE
			stop();
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
		
		@Override
		public String toString() {
			return "Node=" + pCoords(tile) + " Previous=" + ((previous == null) ? "null" : pCoords(previous.tile)) + " Steps=" + stepsTaken +" Value=" + value;
		}
	}
	
	// *** Serialization ***

	/**
	 * Turn an NPC into json.
	 * @param json
	 */
	@Override
	public void save(Json json) {
		json.writeObjectStart();
		json.writeValue("sub", "npc");
		json.writeValue("id", id);
		json.writeValue("name", getName());
		json.writeValue("desc", getDescription());
		json.writeValue("x", tile.x);
		json.writeValue("y", tile.y);
		json.writeValue("type", type.name());
		json.writeArrayStart("objectives");
		for (Objective obj: objectives) {
			obj.save(json);
		}
		// TODO inventory
		json.writeArrayEnd();
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
		long id = jsonData.getLong("id");
		String name = jsonData.getString("name");
		String description = jsonData.getString("desc");
		int x = jsonData.getInt("x");
		int y = jsonData.getInt("y");
		Type type = Type.valueOf(jsonData.getString("type"));
		List<Objective> objectives = new ArrayList<Objective>();
		JsonValue objectiveJson = jsonData.get("objectives");
		JsonValue objectiveValue;
		for(int i = 0; i < objectiveJson.size; i++) {
			objectiveValue = objectiveJson.get(i);
			Objective obj = Objective.load(objectiveValue, gameModel);
			objectives.add(obj);
		}
		return new NPC(id, name, description, gameModel.getTile(x, y), type);
	}
}