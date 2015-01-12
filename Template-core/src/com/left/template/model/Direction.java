package com.left.template.model;

public enum Direction {
	NORTH, EAST, SOUTH, WEST;
	
	public Direction opposite() {
		switch(this) {
		case NORTH:
			return SOUTH;
		case EAST:
			return WEST;
		case SOUTH:
			return NORTH;
		case WEST:
			return EAST;
		default:
			// unreachable code
			return null;
		}
	}
}
