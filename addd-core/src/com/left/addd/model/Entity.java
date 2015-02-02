package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.Res;

public class Entity {

	public final long id;
	protected Tile currentTile;
	/** @deprecated */
	private Tile nextTile;

	/** @deprecated Number of ticks to move to an adjacent tile */
	private int moveDuration;
	/** @deprecated */
	private int moveProgress;
	/** @deprecated */
	private List<MoveStateListener<Entity>> listeners;
	
	/**
	 * @deprecated
	 * Metadata of the entity (used to store strings, sprite dimension info etc)
	 */
	private final HashMap<String,Object> mMetadata;
	/** @deprecated */
	private HashMap<Entity,Entity> mObjectives;
	/** @deprecated */
	private Entity mTargetEntity;

	public Entity(Tile tile) {
		this(Res.generateId(), tile);
	}
	
	protected Entity(long id, Tile tile) {
		this.id = id;
		this.currentTile = tile;
		this.nextTile = tile;

		this.moveDuration = 1;
		this.moveProgress = 0;
		this.listeners = new ArrayList<MoveStateListener<Entity>>(1);
		
		mMetadata = new HashMap<String,Object>();
		mObjectives = new HashMap<Entity,Entity>();
		mTargetEntity = null;
	}

	public Tile getCurrentTile() {
		return currentTile;
	}

	public void setCurrentTile(Tile t) {
		this.currentTile = t;
		stateChanged();
	}

	public Tile getNextTile() {
		return nextTile;
	}

	public void setNextTile(Tile t) {
		// make this private, it should be determined by a pathfinder
		this.nextTile = t;
		stateChanged();
	}

	public int getMoveRate() {
		return moveDuration;
	}

	public void setMoveRate(int speed) {
		this.moveDuration = speed;
	}

	public int getMoveProgress() {
		return moveProgress;
	}

	public void addStateChangedListener(MoveStateListener<Entity> listener) {
		if (!listeners.contains(listener)) {
			this.listeners.add(listener);
			listener.OnStateChanged(this);
		}
	}

	public boolean move(Direction dir) {
		if (dir == null) {
			return false;
		}
		Tile next = currentTile.getNeighbour(dir);
		if (!Tile.isDummyTile(next)) {
			nextTile = next;
			moveProgress = 0;
			stateChanged();
			return true;
		}
		return false;
	}

	// go back to old tile
	public void stop() {
		nextTile = currentTile;
		moveProgress = 0;
		stateChanged();
	}

	private void finishedMoving() {
		currentTile = nextTile;
		moveProgress = 0;
		findPathToTarget();
		stateChanged();
	}

	public void update(int ticks) {
		moveProgress += ticks;
		if (moveProgress >= moveDuration) {
			finishedMoving();

			// TODO determine nextTile based on a pathfinder
//			move(Direction.EAST);
//			findPathToTarget();
			move(getDirection());
		}
	}
	
	protected Direction getDirection() {
		if (nextTile.y - currentTile.y > 0) {
			return Direction.NORTH;
		} else if (nextTile.y == currentTile.y) {
			if (nextTile.x - currentTile.x > 0) {
				return Direction.EAST;
			} else if (nextTile.x == currentTile.x) {
				return null;
			} else {
				return Direction.WEST;
			}
		} else {
			return Direction.SOUTH;
		}
	}

	private void stateChanged() {
		for(MoveStateListener<Entity> l : listeners) {
			l.OnStateChanged(this);
		}
	}
	
	private void findPathToTarget() {
		if (mTargetEntity == null){
			return;
		}
		int stepsTaken = 0;
		Boolean success = false;
		Tile targetTile = mTargetEntity.currentTile;
		Node currNode = new Node(currentTile, currentTile, 0, null);
		PriorityQueue<Node> searchList = new PriorityQueue<Node>();
		HashMap<Tile, Node> visitedNodes = new HashMap<Tile, Node>();
		searchList.add(new Node(currentTile, targetTile, stepsTaken, null));
		while(searchList.size()>0){
			currNode = searchList.poll();
			if (currNode.tile == targetTile) {
				success = true;
				break;
			}
			int newStepsTaken = currNode.stepsTaken + 1;
			for( Tile tile : currNode.tile.getNeighbours()) {
				if(visitedNodes.get(tile) != null){
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
		
	}
	
	private class Node implements Comparable<Node> {
		public final Tile tile;
		int stepsTaken;
		double value;
		Node previous;
		public Node(Tile startTile, Tile endTile, int stepsTaken, Node previous){
			this.tile = startTile;
			this.stepsTaken = stepsTaken;
			this.previous = previous;
			this.value = Math.sqrt(Math.pow(startTile.x - endTile.x, 2) + Math.pow(startTile.y - endTile.y, 2));
		}
		
		void updateNode(int stepsTaken, Node previous){
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
		public int compareTo(Node other){
			return (this.getValue() < other.getValue()) ? -1 : 1;
		}
	}
	
	public Entity getTargetEntity() {
		return mTargetEntity;
	}
	
	public void setTargetEntity(Entity targetEntity) {
		mTargetEntity = targetEntity;
	}
	
	public HashMap<Entity,Entity> getObjectives() {
		return mObjectives;
	}
	
	public void addObjective(Entity e1, Entity e2) {
		mObjectives.put(e1, e2);
	}
	
	public void addMetadata(String key, Object value) {
		mMetadata.put(key, value);
	}
	
	public HashMap<String,Object> getMetadata() {
		return mMetadata;
	}
	
	// *** Serialization ***

	/**
	 * Serialize a Tile into json.
	 *
	 * @param json Json serializer, which will now have the tile's data.
	 * @param tile Tile to save
	 */
	public void save(Json json) {
		json.writeObjectStart("entity");
		json.writeValue("x", currentTile.x);
		json.writeValue("y", currentTile.y);
		json.writeValue("entity_type", "entity");
		// TODO save metadata
		json.writeObjectEnd();
	}
	
	/**
	 * Create a Tile using a json string.
	 *
	 * @param data
	 * @return
	 */
	public static Entity load(JsonValue jsonData, GameModel gameModel) {
		//EntityType type = EntityType.valueOf(jsonData.getString("entity_type"));
		// TODO load metadata
		//if (type==EntityType.BUILDING){
		//	Entity building = Building.load(json, jsonData, gameModel);
		//	return building;
		//} else {
			int x = jsonData.getInt("x");
			int y = jsonData.getInt("y");
			return new Entity(gameModel.getTile(x, y));
		//}
	}
}
