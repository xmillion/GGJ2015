package com.left.addd.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.LoadingException;
import com.left.addd.utils.Res;

public class Entity {

	public final long id;
	protected Tile currentTile;
	/**
	 * An entity's inventory consists of items identified with strings, with quantity as the value in a map.
	 */
	private HashMap<String, Integer> inventory;
	/**
	 * Objectives are in a sorted list. The top priority is always at the head of the list.
	 */
	private List<Objective> objectives;
	
	public Entity(Tile tile) {
		this(Res.generateId(), tile);
	}
	
	protected Entity(long id, Tile tile) {
		this.id = id;
		this.currentTile = tile;
		this.inventory = new HashMap<String, Integer>();
		this.objectives = new ArrayList<Objective>();
	}

	public Tile getCurrentTile() {
		return currentTile;
	}

	public void setCurrentTile(Tile t) {
		this.currentTile = t;
	}
	
	/**
	 * @param entity
	 * @return true if the given entity is neighbouring this entity.
	 */
	public boolean isAdjacentTo(Entity entity) {
		return currentTile.isNeighbour(entity.currentTile);
	}
	
	// *** Inventory ***
	
	public HashMap<String, Integer> getInventory() {
		// TODO this is supposed to be hidden?
		return inventory;
	}
	
	public boolean hasItems() {
		return !inventory.isEmpty();
	}
	
	public int getItemAmount(String item) {
		if (inventory.containsKey(item)) {
			return inventory.get(item);
		} else {
			return 0;
		}
	}
	
	/**
	 * Add the amount of items to the inventory.<br>
	 * To remove items, add a negative amount.
	 * @param item
	 * @param amount
	 * @return the new amount of items this entity is carrying.
	 */
	public int addItem(String item, int amount) {
		int oldAmount = 0;
		if (inventory.containsKey(item)) {
			oldAmount = inventory.get(item);
		}
		// don't allow negative resulting amounts
		if (oldAmount + amount <= 0) {
			// don't allow zeroes in inventory
			inventory.remove(item);
			return 0;
		} else {
			inventory.put(item, oldAmount + amount);
			return oldAmount + amount;
		}
	}
	
	public int removeItem(String item, int amount) {
		return addItem(item, -amount);
	}
	
	// *** Objectives ***
	
	public List<Objective> getObjectives() {
		// TODO this is supposed to be hidden?
		return objectives;
	}

	public Objective getCurrentObjective() {
		return objectives.get(0);
	}
	
	/**
	 * Update any objectives involving the given entity.
	 * This doesn't update the partner's objectives. To do that, call partner.interact(this);
	 * @param partner
	 */
	public void interact(Entity target) {
		List<Objective> objectivesToRemove = new ArrayList<Objective>();
		for (Objective obj: objectives) {
			if (obj.update(this, target)) {
				objectivesToRemove.add(obj);
			}
		}
		objectives.removeAll(objectivesToRemove);
	}
	
	public void update(int ticks) {
		// find neighbours
	}
	
	// *** Serialization ***

	/**
	 * Turn an Entity into json.
	 * @param json
	 */
	public void save(Json json) {
		json.writeObjectStart("entity");
		json.writeValue("id", id);
		json.writeValue("x", currentTile.x);
		json.writeValue("y", currentTile.y);
		// TODO there's a few more fields
		json.writeObjectEnd();
	}
	
	/**
	 * Loads an Entity from stored ID.
	 * ID's can come from save data or initialization data.
	 * @param id
	 * @return
	 * @throws LoadingException if no such Entity exists with that ID.
	 */
	public static Entity load(long id) throws LoadingException {
		// TODO ID based loading
		return null;
	}

	/**
	 * Loads an Entity from save data.
	 * @param jsonData
	 * @param gameModel
	 * @return
	 */
	public static Entity load(JsonValue jsonData, GameModel gameModel) {
		JsonValue entityJson = jsonData.get("entity");
		long id = entityJson.getLong("id");
		int x = entityJson.getInt("x");
		int y = entityJson.getInt("y");
		return new Entity(id, gameModel.getTile(x, y));
	}
}
