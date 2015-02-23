package com.left.addd.model;

import static com.left.addd.utils.Log.log;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;


public class EntityManager {
	
	private List<Entity> entities;
	
	/**
	 * Construct an EntityManager for new game
	 */
	public EntityManager() {
		this(new ArrayList<Entity>());
	}
	
	/**
	 * Full constructor for serializer
	 * @param entities
	 */
	public EntityManager(List<Entity> entities) {
		this.entities = entities;
	}
	
	public void addEntity(Entity em) {
		entities.add(em);
	}
	
	public void freeEntity(Entity em) {
		entities.remove(em);
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	// *** Update ***
	public void update(int ticks) {
		// For each entity, find any neighbouring entities, then give it to them to process.
		// TODO improve the algorithm
		for (Entity first: entities) {
			// Find neighbouring entities for this entity
			List<Tile> neighbouringTiles = first.getTile().getNeighbours();
			neighbouringTiles.add(first.getTile());
			for (Entity second: entities) {
				if (!first.equals(second) && neighbouringTiles.contains(second.getTile())) {
					// Have the entity interact with the neighbour
					first.interact(second);
				}
			}
			first.update(ticks);
		}
	}
	
	// *** Serialization ***

	/**
	 * Turn an EntityManager into json.
	 * @param json
	 */
	public void save(Json json) {
		json.writeObjectStart("em");
		json.writeArrayStart("entities");
		for (Entity e: entities) {
			e.save(json);
		}
		json.writeArrayEnd();
		json.writeObjectEnd();
	}

	/**
	 * Loads an EntityManager from save data.
	 * @param jsonData
	 * @param gameModel
	 * @return
	 */
	public static EntityManager load(JsonValue jsonData, GameModel gameModel) {
		JsonValue entityJson = jsonData.get("em").get("entities");
		List<Entity> entities = new ArrayList<Entity>();
		JsonValue entityValue;
		for(int i = 0; i < entityJson.size; i++) {
			entityValue = entityJson.get(i);
			entities.add(Entity.load(entityValue, gameModel));
		}
		
		return new EntityManager(entities);
	}
}
