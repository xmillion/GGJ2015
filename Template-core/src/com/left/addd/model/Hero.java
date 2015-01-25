package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Hero extends NPC{

	public final HeroType type;

	public Hero(HeroType type, Tile currentTile) {
		super(NPCType.HERO, currentTile);
		this.type = type;
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
		NPCType type = NPCType.valueOf(jsonData.getString("npc_type"));
		return new NPC(type, gameModel.getTile(x, y));
	}
}