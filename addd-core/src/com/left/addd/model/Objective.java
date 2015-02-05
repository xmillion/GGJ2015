package com.left.addd.model;

import static com.left.addd.utils.Log.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.LoadingException;
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
	 * Upon completing this objective, the source entity should gain these new objectives.
	 */
	private final List<Objective> chainedObjectives;
	
	/**
	 * Empty objective will complete instantly.
	 */
	public Objective() {
		this(Res.generateId(), null, null, null, null);
	}
	
	/**
	 * Objective with a target entity.
	 * @param target
	 */
	public Objective(Entity target) {
		this(Res.generateId(), target, null, null, null);
	}
	
	/**
	 * Objective with a target entity and a reward.
	 * @param target
	 * @param rewardItems
	 */
	public Objective(Entity target, HashMap<String, Integer> rewardItems) {
		this(Res.generateId(), target, null, rewardItems, null);
	}
	
	/**
	 * Objective of gathering the required items.
	 * @param requiredItems
	 */
	public Objective(HashMap<String, Integer> requiredItems) {
		this(Res.generateId(), null, requiredItems, null, null);
	}
	
	/**
	 * Objective of gathering the required items, with a reward.
	 * @param requiredItems
	 * @param rewardItems
	 */
	public Objective(HashMap<String, Integer> requiredItems, HashMap<String, Integer> rewardItems) {
		this(Res.generateId(), null, requiredItems, rewardItems, null);
	}
	
	/**
	 * Objective of gathering items and using them on a target entity, with a reward.
	 * @param target
	 * @param requiredItems
	 * @param rewardItems
	 */
	public Objective(Entity target, HashMap<String, Integer> requiredItems, HashMap<String, Integer> rewardItems) {
		this(Res.generateId(), target, requiredItems, rewardItems, null);
	}
	
	/**
	 * Full constructor for the serializer.
	 * @param id
	 * @param target
	 * @param requiredItem
	 * @param requiredItemAmount
	 * @param chainedObjectives
	 */
	private Objective(long id, Entity target, HashMap<String, Integer> requiredItems, HashMap<String, Integer> rewardItems, List<Objective> chainedObjectives) {
		this.id = id;
		this.target = target;
		this.requiredItems = requiredItems;
		this.rewardItems = rewardItems;
		this.chainedObjectives = chainedObjectives;
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
				if (source.getItemAmount(item) < this.getRequiredItemAmount(item)) {
					return false;
				}
			}
			return true;
		}
	}
	
	// ** Update ***
	
	/**
	 * Update this objective by checking if the given entity can complete it.
	 * @param source The objective's owner
	 * @param target The entity that the owner has encountered
	 * @return true if the objective is completed. This will remove required items from the owner. The owner should then remove this objective.<br>
	 * false if the objective has not completed. No changes have been made to the owner.
	 */
	public boolean update(Entity source, Entity target) {
		if (isComplete(target)) {
			// TODO maybe the Entity should be responsible for removing the required items, then this function becomes unneccessary.
			if (requiredItems != null) {
				// remove required items from source's inventory
				for (String item: requiredItems.keySet()) {
					source.removeItem(item, requiredItems.get(item));
				}
			}
			return true;
		}
		return false;
	}
	
	// *** Serialization ***
	
	/**
	 * Turn an Objective into json.
	 * @param json
	 */
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
		if (chainedObjectives != null) {
			json.writeArrayStart("chain");
			for (Objective o: chainedObjectives) {
				// supposedly, chained objectives must be pre-defined.
				json.writeValue(o.id);
			}
			json.writeArrayEnd();
		}
		json.writeObjectEnd();
	}
	
	/**
	 * Loads an Objective from stored ID.
	 * ID's can come from save data or initialization data.
	 * @param id
	 * @return
	 * @throws LoadingException if no such Objective exists with that ID.
	 */
	public static Objective load(long id) throws LoadingException {
		// TODO id based loading
		return new Objective(id, null, null, null, null);
	}
	
	/**
	 * Loads an Objective from save data.
	 * @param jsonData
	 * @param gameModel
	 * @return
	 */
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
		if (objectiveJson.hasChild("reward")) {
			JsonValue rewardJson = objectiveJson.get("reward");
			rewardItems = new HashMap<String, Integer>();
			for (JsonValue item = rewardJson.child(); item != null; item = item.next()) {
				int amount = item.getInt("amount");
				if (amount > 0) {
					rewardItems.put(item.getString("name"), amount);
				}
			}
		}
		List<Objective> chainedObjectives = null;
		if (objectiveJson.hasChild("chain")) {
			JsonValue chainJson = objectiveJson.get("chain");
			chainedObjectives = new ArrayList<Objective>();
			for (JsonValue objective = chainJson.child(); objective != null; objective = objective.next()) {
				long chainId = objective.asLong();
				try {
					chainedObjectives.add(load(chainId));
				} catch(LoadingException e) {
					log("Load", e.getMessage());
				}
			}
		}
		return new Objective(id, target, requiredItems, rewardItems, chainedObjectives);
	}
}
