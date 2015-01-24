package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

public class Entity {

	private String name;
	private Tile currentTile;
	private Tile nextTile;
	private Tile objectiveTile;
	
	/** Number of ticks to move to an adjacent tile */
	private int moveDuration;
	private int moveProgress;
	
	// this is willis's 4:30am event handling implementation
	private boolean moveStarted = false;
	private boolean moveCompleted = false;
	
	public Entity(String name, Tile currentTile, Tile objectiveTile) {
		this.name = name;
		this.currentTile = currentTile;
		this.nextTile = currentTile;
		this.objectiveTile = objectiveTile;
		
		this.moveDuration = 6;
		this.moveProgress = 0;
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
	}
	
	public Tile getNextTile() {
		return nextTile;
	}
	
	public void setNextTile(Tile t) {
		// make this private, it should be determined by a pathfinder
		this.nextTile = t;
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
	
	public boolean triggerMoveStarted() {
		if (moveStarted) {
			moveStarted = false;
			return true;
		}
		return false;
	}
	
	public boolean triggerMoveCompleted() {
		if (moveCompleted) {
			moveCompleted = false;
			return true;
		}
		return false;
	}
	
	public boolean move(Direction dir) {
		Tile next = currentTile.getNeighbour(dir);
		if (!Tile.isDummyTile(next)) {
			nextTile = next;
			moveStarted = true;
			moveProgress = 0;
		}
		
		return moveStarted;
	}
	
	// go back to old tile
	public void stop() {
		nextTile = currentTile;
		moveCompleted = true;
		moveProgress = 0;
	}

	public void update(int ticks) {
		moveProgress += ticks;
		if (moveProgress >= moveDuration) {
			// finished moving
			currentTile = nextTile;
			moveCompleted = true;
			moveProgress = 0;
			
			// TODO determine nextTile based on a pathfinder
			move(Direction.EAST);
		}
	}
}
