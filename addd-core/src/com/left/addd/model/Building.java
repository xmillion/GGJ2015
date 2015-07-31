package com.left.addd.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.Res;

/**
 * Represents entities that don't move, and can occupy more than one tile.
 */
public class Building extends Entity {

	public enum Type {
		NONE(1, 1, "tile"),
		HOUSE(1, 1, "house"),
		FACTORY(2, 2, "factory"),
		SCHOOL(2, 2, "school"),
		LIBRARY(2, 2, "library");
		
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

	public Building(Type type, Tile tile) {
		this(Res.generateId(), "Building", "", tile, type);
	}
	
	public Building(String name, String description, Tile tile, Type type) {
		this(Res.generateId(), name, description, tile, type);
	}

	/**
	 * Full constructor for serializer
	 * @param id
	 * @param name
	 * @param description
	 * @param tile
	 * @param type
	 */
	private Building(long id, String name, String description, Tile tile, Type type) {
		super(id, name, description, tile);
		this.type = type;
		
		// When this building is constructed, the tiles underneath must be set to EMPTY so they don't become networks.
		for (Tile ti = tile; !Tile.isDummyTile(ti) && (ti.x < tile.x + type.width); ti = ti.tryGetNeighbour(Direction.EAST)) {
			for (Tile tj = ti; !Tile.isDummyTile(tj) && (tj.y < tile.y + type.height); tj = tj.tryGetNeighbour(Direction.NORTH)) {
				tj.setType(Tile.Type.EMPTY);
			}
		}
	}

	public int getWidth() {
		return type.width;
	}

	public int getHeight() {
		return type.height;
	}
	
	@Override
	public String getAssetName() {
		return type.assetName;
	}
	
	// *** Serialization ***

	/**
	 * Serialize a Building into json.
	 * 
	 * @param json serializer
	 */
	@Override
	public void save(Json json) {
		json.writeObjectStart();
		json.writeValue("sub", "building");
		json.writeValue("id", id);
		json.writeValue("name", getName());
		json.writeValue("desc", getDescription());
		json.writeValue("x", this.tile.x);
		json.writeValue("y", this.tile.y);
		json.writeValue("type", this.type.name());
		json.writeArrayStart("objectives");
		for (Objective obj: objectives) {
			obj.save(json);
		}
		// TODO inventory
		json.writeArrayEnd();
		json.writeObjectEnd();
	}

	/**
	 * Create a Building using the json string.
	 * @param jsonData
	 * @param gameModel
	 * @return
	 */
	public static Building load(JsonValue jsonData, GameModel gameModel) {
		long id = jsonData.getLong("id");
		String name = jsonData.getString("name");
		String description = jsonData.getString("desc");
		int x = jsonData.getInt("x");
		int y = jsonData.getInt("y");
		Type type = Type.valueOf(jsonData.getString("type"));
		List<Objective> objectives = new ArrayList<Objective>();
		JsonValue objectiveJson = jsonData.get("objectives");
		JsonValue objectiveValue;
		for(int i = 0; i < objectiveJson.size; i++) {
			objectiveValue = objectiveJson.get(i);
			Objective obj = Objective.load(objectiveValue, gameModel);
			objectives.add(obj);
		}
		return new Building(id, name, description, gameModel.getTile(x, y), type);
	}
}
