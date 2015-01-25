package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Entity {


	private Tile currentTile;
	private Tile nextTile;


	/** Number of ticks to move to an adjacent tile */
	private int moveDuration;
	private int moveProgress;

	// this is willis's 11am event handling implementation
	private List<StateChangedListener> listeners;
	
	/**
	 * Metadata of the entity (used to store strings, sprite dimension info etc)
	 */
	private final HashMap<String,Object> mMetadata;
	
	/*
	 * Current state of the entity and its actions
	 */
	private EntityState mCurrentState;
	
	private HashMap<Entity,Entity> mObjectives;
	
	private Entity mTargetEntity;

	public Entity(Tile currentTile) {
		
		this.currentTile = currentTile;
		this.nextTile = currentTile;

		this.moveDuration = 3;
		this.moveProgress = 0;
		this.listeners = new ArrayList<StateChangedListener>(1);
		
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

	public int getMoveDuration() {
		return moveDuration;
	}

	public void setMoveDuration(int speed) {
		this.moveDuration = speed;
	}

	public int getMoveProgress() {
		return moveProgress;
	}

	public void addStateChangedListener(StateChangedListener listener) {
		this.listeners.add(listener);
		listener.OnStateChanged();
	}

	public boolean move(Direction dir) {
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
		stateChanged();
	}

	public void update(int ticks) {
		moveProgress += ticks;
		if (moveProgress >= moveDuration) {
			finishedMoving();

			// TODO determine nextTile based on a pathfinder
			move(Direction.EAST);
		}
	}

	private void stateChanged() {
		for(StateChangedListener l : listeners) {
			l.OnStateChanged();
		}
	}

	// *** Serialization ***

	/**
	 * Serialize a Tile into json.
	 *
	 * @param json Json serializer, which will now have the tile's data.
	 * @param tile Tile to save
	 */
	public void save(Json json) {
		/*
		json.writeObjectStart("entity");
		json.writeValue("ox", this.originX);
		json.writeValue("oy", this.originY);
		json.writeValue("type", this.type.name());
		json.writeObjectEnd();
		*/
	}
	
	private void findPathToTarget() {
		int stepsTaken = 0;
		Boolean success = false;
		Tile targetTile = mTargetEntity.currentTile;
		Node currNode = new Node(currentTile, targetTile, 0,null);
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
					searchList.add(new Node(currNode.tile, targetTile, newStepsTaken, currNode));
				}
			}
			
		}
		if (success) {
			while (currNode.previous != null) {
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

	/**
	 * Create a Tile using a json string.
	 *
	 * @param data
	 * @return
	 */
	public static Entity load(Json json, JsonValue jsonData, GameModel gameModel) {
		EntityType type = EntityType.valueOf(jsonData.getString("entity_type"));
		if (type==EntityType.BUILDING){
			Entity building = Building.load(json, jsonData, gameModel);
			return building;
		} else {
			return null;
		}
	}
	
	public void setCurrentState(EntityState es) {
		mCurrentState = es;
	}
	
	public EntityState getCurrentState() {
		return mCurrentState;
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
	

	/**
	 * Private class to keep track of the states of the entity
	 * @author kev
	 *
	 */
	public class EntityState {
		
		private int mX;
		private int mY;
		private Tile mNextTile;
		
		public EntityState() {
			mX = 0;
			mY = 0;
		}
		
		public EntityState(int x, int y) {
			mX = x;
			mY = y;
		}
		
		public void setX(int x) {
			mX = x;
		}
		
		public int getX() {
			return mX;
		}
		
		public void setY(int y) {
			mY = y;
		}
		
		public int getY() {
			return mY;
		}
		
		public void setNextTile(Tile next) {
			mNextTile = next;
		}
		
		public Tile getNextTile() {
			return mNextTile;
		}
	}
}
