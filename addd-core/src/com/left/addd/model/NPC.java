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

/**
 * An Entity that can also move.<br>
 * NPCs that have targets will find a path to the target and move along it.<br>
 * It's modeled so the NPC waits a certain amount of time (it's "speed"), then instantly goes to the adjacent tile.<br>
 * Visually, the sprite will be dragged towards its current tile at a constant velocity, meaning it doesn't draw a transition state.<br>
 * This avoids any interpolation and jarring sprite transforms. Inspired by troop movement in EU4.
 */
public class NPC extends Entity {
	
	public enum Type {
		NONE(60, "main"),
		HERO(30, "old"),
		POLICE(90, "redshirt"),
		FACULTY(120, "blueshirt"),
		STUDENT(20, "young");
		
		/** Number of minutes to move by one tile */
		public final int moveSpeed;
		public final String assetName;
		private Type(int moveSpeed, String assetName) {
			this.moveSpeed = moveSpeed;
			this.assetName = assetName;
		}
	}

	public final Type type;
	
	// Pathfinding
	
	/** If not null, try to move to this entity. */
	private Entity targetEntity;
	/** Cache the target's position */
	private Tile targetTile;
	/** If not empty, NPC is moving along this list of tiles. */
	private Deque<Tile> path;
	/** Cache the neighbouring tile for movement. */
	private Tile nextTile;
	
	// Move animations
	
	/** Number of minutes to move by one tile. */
	private int moveSpeed;
	/** Remaining time before move can take place. */
	private int moveTimer;
	/** Flag to indicate arrival. */
	private boolean justArrived;
	/** Amount of time given for a move animation. */
	private static final float MOVE_ANIMATION_TIME = 1f;
	/** Remaining time for move animation. */
	private float moveAnimationTimer;
	/** Direction of move animation */
	private Direction moveAnimationDirection;
	/** Current position of NPC during movement in fractional Tile coordinates. */
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
		this.moveTimer = 0;
		this.justArrived = false;
		this.moveAnimationTimer = 0;
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
	
	public Direction getMoveDirection() {
		return moveAnimationDirection;
	}

	public int getMoveSpeed() {
		return moveSpeed;
	}

	/**
	 * @param speed New move speed
	 */
	public void setMoveSpeed(int speed) {
		this.moveSpeed = speed;
	}
	
	public Vector2 getTileCoordinate() {
		return tileCoordinate;
	}
	
	public boolean isMoving() {
		// All of the following are true if this is moving:
		// !tile.equals(nextTile);
		// targetEntity != null;
		// !path.isEmpty();
		return moveTimer > 0;
	}
	
	// *** Movement ***
	
	/**
	 * Stop moving. Remove target and path.
	 */
	public void stop() {
		targetEntity = null;
		targetTile = null;
		path.clear();
		nextTile = tile;
		moveTimer = 0;
	}
	
	@Override
	public void interact(Entity target) {
		super.interact(target);
		if (targetEntity != null && targetEntity.equals(target)) {
			// I just interacted with the target.
			if (targetEntity.tile.equals(targetTile)) {
				// I'm about to move into the same Tile as the target. Don't do that.
				stop();
			}
		}
	}
	
	/**
	 * Update the model.
	 * @param ticks
	 */
	@Override
	public void update(int ticks) {
		super.update(ticks);
		if (moveTimer > 0) {
			// I am moving.
			moveTimer -= ticks;
			justArrived = true;
		} else {
			// I am idle.
			if (justArrived) {
				// I just arrived at my next tile.
				moveAnimationTimer = MOVE_ANIMATION_TIME;
				moveAnimationDirection = tile.getDirection(nextTile);
				tile = nextTile;
				justArrived = false;
			}
			
			// Should I move?
			if (targetEntity == null) {
				// I have no destination. Look for one.
				for (Objective obj: getObjectives()) {
					if (obj.hasTarget())
					{
						log(getName(), getObjectives().size() + " objectives. Next up is " + obj.getTarget().getName());
						targetEntity = obj.getTarget();
						findPathToTarget();
						break;
					}
				}
			} else if (!targetEntity.tile.equals(targetTile)) {
				// My target moved so I have to find a new path.
				findPathToTarget();
			}
			
			if (!path.isEmpty()) {
				// I now have a destination. Start going to it.
				nextTile = path.poll();
				if (!tile.equals(nextTile)) {
					moveTimer = moveSpeed;
				}
			} else {
				// I have nothing to do.
				moveAnimationDirection = null;
			}
		}
	}
	
	/**
	 * Render the sprite. The draw position of the sprite is separate from the NPC's current tile
	 * @param delta
	 */
	public void render(float delta) {
		if (moveAnimationTimer > 0) {
			// I need to move the sprite.
			if (moveAnimationDirection != null) {
				// Move towards current tile
				float moveX = tile.x;
				float moveY = tile.y;
				switch(moveAnimationDirection) {
				case NORTH:
					moveY -= moveAnimationTimer;
					break;
				case EAST:
					moveX -= moveAnimationTimer;
					break;
				case SOUTH:
					moveY += moveAnimationTimer;
					break;
				case WEST:
					moveX += moveAnimationTimer;
					break;
				}
				tileCoordinate.set(moveX, moveY);
			} else {
				tileCoordinate.set(tile.x, tile.y);
			}
			
			moveAnimationTimer -= delta;
		} else {
			// The sprite is where I actually am.
			tileCoordinate.set(tile.x, tile.y);
		}
		
		if (moveTimer > 0) {
			// I am in the process of moving. Draw the move animation in place.
		} else {
			// I am idle. Draw the idle animation
		}
	}
	
	// *** Pathfinding ***
	
	/**
	 * Update this.path using pathfinding algorithm.
	 * Ensure that targetEntity != null before calling this.
	 * @pre targetEntity != null
	 * @post targetTile is correct
	 * @post path is correct
	 */
	private void findPathToTarget() {
		if (tile.equals(targetEntity.tile)) {
			// we're already here.
			path.clear();
			targetEntity = null;
			return;
		}
		
		targetTile = targetEntity.tile;
		path.clear();
		int stepsTaken = 0;
		boolean success = false;
		Node currentNode = new Node(tile, targetTile, stepsTaken, null);
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
			// I can't find a path.
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