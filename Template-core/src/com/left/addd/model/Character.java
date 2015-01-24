package com.left.addd.model;

public class Character {

	private String name;
	private Tile currentTile;
	private Tile objectiveTile;
	
	public Character(String name) {
		this.name = name;
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
	
	public Tile getObjectiveTile() {
		return objectiveTile;
	}
	
	public void setObjectiveTile(Tile t) {
		this.objectiveTile = t;
	}
}
