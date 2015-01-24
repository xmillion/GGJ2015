package com.left.addd.model;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum BuildingType {
	NONE(1, 1),
	HOUSE(1, 1),
	FACTORY(2, 2);
	
	public final int width;
	public final int height;
	
	private BuildingType(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
