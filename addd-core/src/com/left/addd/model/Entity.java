package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.LoadingException;
import com.left.addd.utils.Res;

/**
 * Represents an in game object that is rendered and can interact with other Entities.
 */
public class Entity {

	public final long id;
	private String name;
	private String description;
	/**
	 * This entity currently occupies this tile.
	 */
	protected Tile tile;
	/**
	 * An entity's inventory consists of items identified with strings, with quantity as the value in a map.
	 */
	protected HashMap<String, Integer> inventory;
	/**
	 * Objectives are in a sorted list. The top priority is always at the head of the list.
	 */
	protected List<Objective> objectives;
	
	public Entity(Tile tile) {
		this(Res.generateId(), "Entity", "", tile);
	}
	
	public Entity(String name, String description, Tile tile) {
		this(Res.generateId(), name, description, tile);
	}
	
	/**
	 * Full constructor for the serializer
	 * @param id
	 * @param tile
	 */
	protected Entity(long id, String name, String description, Tile tile) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.tile = tile;
		this.inventory = new HashMap<String, Integer>();
		this.objectives = new ArrayList<Objective>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Asset name for rendering. Subclasses must override this...
	 * @return
	 */
	public String getAssetName() {
		return "tile";
	}

	public Tile getTile() {
		return tile;
	}

	public void setCurrentTile(Tile t) {
		this.tile = t;
	}
	
	/**
	 * @param entity
	 * @return true if the given entity is neighbouring this entity.
	 */
	public boolean isAdjacentTo(Entity entity) {
		return tile.isNeighbour(entity.tile);
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
		// TODO this is supposed to be public or protected?
		return objectives;
	}

	public Objective getCurrentObjective() {
		if (objectives.isEmpty()) {
			return null;
		}
		return objectives.get(0);
	}
	
	public void addObjective(Objective obj) {
		objectives.add(obj);
	}
	
	/**
	 * Update any objectives involving the given entity.
	 * This doesn't update the partner's objectives. To do that, call partner.interact(this);
	 * @param partner
	 */
	public void interact(Entity target) {
		List<Objective> completedObjectives = new ArrayList<Objective>();
		// Can't edit objectives in-line due to concurrent modification limitation.
		for (Objective obj: objectives) {
			if (obj.isComplete(this, target)) {
				completedObjectives.add(obj);
			}
		}
		
		// Update inventory and objectives.
		for (Objective obj: completedObjectives) {
			
			Map<String, Integer> requiredItems = obj.getRequiredItems();
			Map<String, Integer> rewardItems = obj.getRewardItems();
			List<Objective> chainedObjectives = obj.getChainedObjectives();
			if (requiredItems != null) {
				for (String item: requiredItems.keySet()) {
					removeItem(item, requiredItems.get(item));
				}
			}
			if (rewardItems != null) {
				for (String item: rewardItems.keySet()) {
					addItem(item, rewardItems.get(item));
				}
			}
			if (chainedObjectives != null) {
				for (Objective chainedObj: chainedObjectives) {
					addObjective(chainedObj);
				}
			}
		}
		
		// Objective completed!
		objectives.removeAll(completedObjectives);
	}
	
	public void update(int ticks) {
		// No work needed here. EntityManager is doing the neighbouring Entity interactions for us.
	}
	
	// *** Serialization ***

	/**
	 * Turn an Entity into json.
	 * @param json
	 */
	public void save(Json json) {
		json.writeObjectStart();
		json.writeValue("sub", "entity");
		json.writeValue("id", id);
		json.writeValue("name", name);
		json.writeValue("desc", description);
		json.writeValue("x", tile.x);
		json.writeValue("y", tile.y);
		json.writeValue("type", "entity");
		json.writeArrayStart("objectives");
		for (Objective obj: objectives) {
			obj.save(json);
		}
		// TODO inventory
		json.writeArrayEnd();
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
		String sub = jsonData.getString("sub");
		if (sub.equalsIgnoreCase("building")) {
			return Building.load(jsonData, gameModel);
		} else if (sub.equalsIgnoreCase("npc")) {
			return NPC.load(jsonData, gameModel);
		} else {
			long id = jsonData.getLong("id");
			String name = jsonData.getString("name");
			String description = jsonData.getString("desc");
			int x = jsonData.getInt("x");
			int y = jsonData.getInt("y");
			List<Objective> objectives = new ArrayList<Objective>();
			JsonValue objectiveJson = jsonData.get("objectives");
			JsonValue objectiveValue;
			for(int i = 0; i < objectiveJson.size; i++) {
				objectiveValue = objectiveJson.get(i);
				Objective obj = Objective.load(objectiveValue, gameModel);
				objectives.add(obj);
			}
			return new Entity(id, name, description, gameModel.getTile(x, y));
		}
	}
}
