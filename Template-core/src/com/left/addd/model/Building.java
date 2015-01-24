package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Building {

	private int originX;
	private int originY;

	public final BuildingType type;

	public Building(BuildingType type) {
		this(type, 0, 0);
	}

	public Building(BuildingType type, int ox, int oy) {
		setOrigin(ox, oy);
		this.type = type;
	}

	public int getOriginX() {
		return originX;
	}

	public int getOriginY() {
		return originY;
	}

	public void setOrigin(int x, int y) {
		this.originX = x;
		this.originY = y;
	}

	public int getWidth() {
		return type.width;
	}

	public int getHeight() {
		return type.height;
	}
	
	// *** Rules ***

	public void update(int delta) {
	}
	
	// *** Serialization ***

	/**
	 * Serialize a Tile into json.
	 * 
	 * @param json Json serializer, which will now have the tile's data.
	 * @param tile Tile to save
	 */
	public void save(Json json) {
		json.writeObjectStart("building");
		json.writeValue("ox", this.originX);
		json.writeValue("oy", this.originY);
		json.writeValue("type", this.type.name());
		json.writeObjectEnd();
	}

	/**
	 * Create a Tile using a json string.
	 * 
	 * @param data
	 * @return
	 */
	public static Building load(Json json, JsonValue jsonData) {
		int ox = jsonData.getInt("ox");
		int oy = jsonData.getInt("oy");
		BuildingType type = BuildingType.valueOf(jsonData.getString("type"));
		return new Building(type, ox, oy);
	}
}
