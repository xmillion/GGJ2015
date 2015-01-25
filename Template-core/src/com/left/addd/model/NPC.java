package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class NPC extends Entity {
	
	public enum Type {
		NONE("main"),
		HERO("old"),
		POLICE("redshirt"),
		FACULTY("redshirt"),
		STUDENT("young");
		
		public final String assetName;
		private Type(String assetName) {
			this.assetName = assetName;
		}
	}

	public final Type type;

	public NPC(Type type, Tile currentTile) {
		super(currentTile);
		this.type = type;
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
		json.writeObjectStart("npc");
		json.writeValue("x", this.currentTile.x);
		json.writeValue("y", this.currentTile.y);
		json.writeValue("entity_type", "building");
		json.writeValue("npc_type", this.type.name());
		json.writeObjectEnd();
	}

	/**
	 * Create a Tile using a json string.
	 * 
	 * @param data
	 * @return
	 */
	public static NPC load(Json json, JsonValue jsonData, GameModel gameModel) {
		int x = jsonData.getInt("x");
		int y = jsonData.getInt("y");
		Type type = Type.valueOf(jsonData.getString("npc_type"));
		return new NPC(type, gameModel.getTile(x, y));
	}
}