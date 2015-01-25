package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.StringBuilder;
import com.left.addd.model.GameModel;
import com.left.addd.model.Network;
import com.left.addd.model.Tile;
import com.left.addd.model.Time;

/**
 * TemplateModel is the model for this game. It represents the logic behind this game.
 * It does not know anything about how the representation is drawn, or how the player interacts with it.
 * Reference: https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class GameModel {
	private final Tile[][] tiles;
	public final int width;
	public final int height;
	private Time time;
	private ArrayList<Entity> entity_list;
	
	private List<Entity> entities;
	
	private List<StateChangedListener> listeners;

	public GameModel(int width, int height) {
		this(width, height, 0, true);
	}
	
	public GameModel(int width, int height, long timeInHours, boolean initializeTiles) {
		this.width = width;
		this.height = height;
		this.tiles = new Tile[width][height];
		this.entity_list = new ArrayList<Entity>();
		if(initializeTiles) {
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					tiles[i][j] = new Tile(this, i, j, new Network(NetworkType.ROAD, null, null, this.getTile(i, j-1).getNetwork(), this.getTile(i-1, j).getNetwork()));
				}
			}
		}
		
		this.time = new Time(timeInHours);
		
		this.entities = new ArrayList<Entity>();
		Entity testEntity = new Entity(tiles[2][1]);
		Entity testEntity2 = new Entity(tiles[5][4]);
		testEntity.addMetadata("Name", "Bob");
		testEntity.addMetadata("Description", "Bob is the first test entity");
		testEntity2.addMetadata("Name", "Alice");
		testEntity2.addMetadata("Description", "Alice is the second test entity");
		entities.add(testEntity);
		entities.add(testEntity2);
		testEntity.move(Direction.NORTH);
		testEntity.setTargetEntity(testEntity2);
		
		this.listeners = new ArrayList<StateChangedListener>();
	}
	
	public Tile getTileByEntityProperty(){
		return new Tile(this,-1,-1,null);
	}
	
	public Time getTime() {
		return time;
	}
	
	public List<Entity> getEntities() {
		return entities;
	}

	public Tile[][] getTiles() {
		return tiles;
	}

	public Tile getTile(int x, int y) {
		if(0 > x || x >= width || 0 > y || y >= height) {
			// log("Not a Tile: " + Utils.pCoords(x, y));
			return Tile.dummyTile();
		}
		return tiles[x][y];
	}
	
	public void addListener(StateChangedListener listener) {
		this.listeners.add(listener);
		listener.OnStateChanged();
	}
	
	/**
	 * Provides information on the given Tile.
	 * 
	 * @param x
	 * @param y
	 * @return Tile info
	 */
	public String query(int x, int y) {
		// TODO query's final form should return a Query object, not a debug string
		Tile tile = getTile(x, y);
		if(Tile.isDummyTile(tile)) {
			log("Query", "Not a Tile " + pCoords(tile.x, tile.y));
			return "";
		}

		StringBuilder query = new StringBuilder();

		// Tile data
		query.append("Tile " + pCoords(tile.x, tile.y));
		query.append(".\n");

		// Network data
		if(tile.hasNetwork()) {
			Network n = tile.getNetwork();
			query.append("Network: " + n.type.name());
		} else {
			query.append("No network");
		}
		query.append(".\n");

		log(":::Query::", query.toString());
		return query.toString();
	}
	
	public void update(float delta) {
		int ticks = time.update(delta);
		updateTiles(ticks);
		updateEntities(ticks);
	}

	private void updateTiles(int ticks) {
		for(Tile[] ts: tiles) {
			for(Tile t: ts) {
				t.update(ticks);
			}
		}
	}
	
	private void updateEntities(int ticks) {
		for(Entity e : entities) {
			e.update(ticks);
		}
	}
	
	public void save(Json json) {
		json.writeObjectStart();
		json.writeValue("width", width);
		json.writeValue("height", height);
		json.writeValue("time", time.getTime());
		json.writeArrayStart("tiles");
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				tiles[i][j].save(json);
			}
		}
		json.writeArrayEnd();
		json.writeObjectEnd();
	}

	public static GameModel load(Json json, JsonValue jsonData) {
		int width = jsonData.getInt("width");
		int height = jsonData.getInt("height");
		long timeInHours = jsonData.getLong("time");
		GameModel gameModel = new GameModel(width, height, timeInHours, false);

		JsonValue tileData = jsonData.get("tiles");

		JsonValue tileValue;
		for(int i = 0; i < tileData.size; i++) {
			tileValue = tileData.get(i);
			Tile tile = Tile.load(json, tileValue, gameModel);
			gameModel.tiles[tile.x][tile.y] = tile;
		}
		
		JsonValue entityData = jsonData.get("entities");
		JsonValue entityValue;
		for(int i = 0; i < entityData.size; i++) {
			entityValue = entityData.get(i);
			Entity entity = Entity.load(json, entityValue, gameModel);
			gameModel.entity_list.add(entity);
		}
		

		return gameModel;
	}
}
