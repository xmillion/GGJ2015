package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Building extends Entity {

	public enum Type {
		NONE(1, 1, "tile"),
		HOUSE(1, 1, "house"),
		FACTORY(2, 2, "factory");
		
		public final int width;
		public final int height;
		public final String assetName;
		private Type(int width, int height, String assetName) {
			this.width = width;
			this.height = height;
			this.assetName = assetName;
		}
	}
	
	public final Type type;

	public Building(Type type, Tile currentTile) {
		super(currentTile);
		this.type = type;
	}

	public int getWidth() {
		return type.width;
	}

	public int getHeight() {
		return type.height;
	}
	
	public Type getType() {
		return type;
	}
	
	// *** Serialization ***

	/**
	 * Serialize a Tile into json.
	 * 
	 * @param json Json serializer, which will now have the tile's data.
	 * @param tile Tile to save
	 */
	@Override
	public void save(Json json) {
		json.writeObjectStart("building");
		json.writeValue("x", this.currentTile.x);
		json.writeValue("y", this.currentTile.y);
		json.writeValue("entity_type", "building");
		json.writeValue("building_type", this.type.name());
		json.writeObjectEnd();
	}

	/**
	 * Create a Tile using a json string.
	 * 
	 * @param data
	 * @return
	 */
	public static Building load(Json json, JsonValue jsonData, GameModel gameModel) {
		int x = jsonData.getInt("x");
		int y = jsonData.getInt("y");
		Type type = Type.valueOf(jsonData.getString("building_type"));
		return new Building(type, gameModel.getTile(x, y));
	}
}
