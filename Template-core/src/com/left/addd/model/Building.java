package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Building extends Entity{

	private int originX;
	private int originY;

	public final BuildingType type;

	public Building(String name, BuildingType type, Tile currentTile) {
		super(name, currentTile );
		setOrigin(currentTile.x, currentTile.y);
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
		json.writeObjectStart("entity");
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
	public static Building load(Json json, JsonValue jsonData, GameModel gameModel) {
		int ox = jsonData.getInt("ox");
		int oy = jsonData.getInt("oy");
		String name = jsonData.getString("name");
		BuildingType type = BuildingType.valueOf(jsonData.getString("building_type"));
		return new Building(name, type, gameModel.getTile(ox, oy));
	}
}
