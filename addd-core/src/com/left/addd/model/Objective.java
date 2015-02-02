package com.left.addd.model;

import java.util.HashMap;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.Res;

public class Objective {
	/**
	 * Objectives can be predefined or they can be created on the fly.
	 * Predefined objectives have a predefined id.
	 * Custom objectives generate their own id's.
	 */
	public final long id;

	/**
	 * If this is not null,
	 * then this objective is to be adjacent or on the tile of the target entity.
	 */
	private final Entity target;
	
	/**
	 * If this is not null,
	 * then this objective is to acquire all of the required items in the given amounts.
	 * Values must be greater than zero.
	 */
	private final HashMap<String, Integer> requiredItems;
	
	/**
	 * If this is not null,
	 * then the source entity will gain the given items.
	 * Values must be greater than zero.
	 */
	private final HashMap<String, Integer> rewardItems;
	
	/**
	 * Empty objective will complete instantly.
	 */
	public Objective() {
		this(Res.generateId(), null, null, null);
	}
	
	/**
	 * Objective with a target entity.
	 * @param target
	 */
	public Objective(Entity target) {
		this(Res.generateId(), target, null, null);
	}
	
	/**
	 * Objective with a target entity and a reward.
	 * @param target
	 * @param rewardItems
	 */
	public Objective(Entity target, HashMap<String, Integer> rewardItems) {
		this(Res.generateId(), target, null, rewardItems);
	}
	
	/**
	 * Objective of gathering the required items.
	 * @param requiredItems
	 */
	public Objective(HashMap<String, Integer> requiredItems) {
		this(Res.generateId(), null, requiredItems, null);
	}
	
	/**
	 * Objective of gathering the required items, with a reward.
	 * @param requiredItems
	 * @param rewardItems
	 */
	public Objective(HashMap<String, Integer> requiredItems, HashMap<String, Integer> rewardItems) {
		this(Res.generateId(), null, requiredItems, rewardItems);
	}
	
	/**
	 * Objective of gathering items and using them on a target entity, with a reward.
	 * @param target
	 * @param requiredItems
	 * @param rewardItems
	 */
	public Objective(Entity target, HashMap<String, Integer> requiredItems, HashMap<String, Integer> rewardItems) {
		this(Res.generateId(), target, requiredItems, rewardItems);
	}
	
	/**
	 * Full constructor for the serializer.
	 * @param id
	 * @param target
	 * @param requiredItem
	 * @param requiredItemAmount
	 */
	private Objective(long id, Entity target, HashMap<String, Integer> requiredItems, HashMap<String, Integer> rewardItems) {
		this.id = id;
		this.target = target;
		this.requiredItems = requiredItems;
		this.rewardItems = rewardItems;
	}
	
	public Entity getTarget() {
		return target;
	}
	
	public int getRequiredItemAmount(String itemName) {
		if (requiredItems.containsKey(itemName)) {
			return requiredItems.get(itemName);
		}
		return 0;
	}
	
	public HashMap<String, Integer> getRequiredItems() {
		// return a copy, so the original cannot be mutated.
		return new HashMap<String, Integer>(requiredItems);
	}
	
	public int getRewardItemAmount(String itemName) {
		if (rewardItems.containsKey(itemName)) {
			return rewardItems.get(itemName);
		}
		return 0;
	}
	
	public HashMap<String, Integer> getRewardItems() {
		// return a copy, so the original cannot be mutated.
		return new HashMap<String, Integer>(rewardItems);
	}
	
	public boolean isComplete(Entity source) {
		return isTargetComplete(source) && isRequiredItemsComplete(source); 
	}
	
	public boolean isTargetComplete(Entity source) {
		if (target == null) {
			return true;
		} else if (source == null) {
			return false;
		} else {
			return source.isAdjacentTo(target);
		}
	}
	
	public boolean isRequiredItemsComplete(Entity source) {
		if (requiredItems == null) {
			return true;
		} else if (!source.hasItems()) {
			return false;
		} else {
			for (String item : requiredItems.keySet()) {
				if (source.getInventoryAmount(item) < this.getRequiredItemAmount(item)) {
					return false;
				}
			}
			return true;
		}
	}
	
	// Serialization
	
	public void save(Json json) {
		json.writeObjectStart("objective");
		json.writeValue("id", id);
		if (target != null) {
			json.writeValue("target", target.id);
		}
		if (requiredItems != null) {
			json.writeArrayStart("required");
			for (String item : requiredItems.keySet()) {
				json.writeObjectStart(item);
				json.writeValue(item, requiredItems.get(item));
				json.writeObjectEnd();
			}
			json.writeArrayEnd();
		}
		if (rewardItems != null) {
			json.writeArrayStart("reward");
			for (String item : rewardItems.keySet()) {
				json.writeObjectStart(item);
				json.writeValue("name", item);
				json.writeValue("amount", rewardItems.get(item));
				json.writeObjectEnd();
			}
			json.writeArrayEnd();
		}
		json.writeObjectEnd();
	}
	
	public static Objective load(long id) {
		// TODO some way to load an objective from a pre-defined pool of objectives
		return new Objective(id, null, null, null);
	}
	
	public static Objective load(JsonValue jsonData, GameModel gameModel) {
		JsonValue objectiveJson = jsonData.get("objective");
		long id = objectiveJson.getLong("id");
		Entity target = null;
		if (objectiveJson.hasChild("target")) {
			target = Entity.load(objectiveJson, gameModel);
		}
		HashMap<String, Integer> requiredItems = null;
		if (objectiveJson.hasChild("required")) {
			JsonValue requiredJson = objectiveJson.get("required");
			requiredItems = new HashMap<String, Integer>();
			for (JsonValue item = requiredJson.child(); item != null; item = item.next()) {
				int amount = item.getInt("amount");
				if (amount > 0) {
					requiredItems.put(item.getString("name"), amount);
				}
			}
		}
		HashMap<String, Integer> rewardItems = null;
		
		return new Objective(id, target, requiredItems, rewardItems);
	}
}
