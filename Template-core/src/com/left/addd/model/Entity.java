package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.ArrayList;
import java.util.List;

public class Entity {

	private String name;
	private Tile currentTile;
	private Tile nextTile;
	private Tile objectiveTile;
	
	/** Number of ticks to move to an adjacent tile */
	private int moveDuration;
	private int moveProgress;
	
	// this is willis's 11am event handling implementation
	private List<StateChangedListener> listeners;
	
	public Entity(String name, Tile currentTile, Tile objectiveTile) {
		this.name = name;
		this.currentTile = currentTile;
		this.nextTile = currentTile;
		this.objectiveTile = objectiveTile;
		
		this.moveDuration = 3;
		this.moveProgress = 0;
		this.listeners = new ArrayList<StateChangedListener>(1);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
	
	public Tile getObjectiveTile() {
		return objectiveTile;
	}
	
	public void setObjectiveTile(Tile t) {
		this.objectiveTile = t;
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
}
