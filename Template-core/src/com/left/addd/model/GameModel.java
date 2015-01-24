package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.StringBuilder;
import com.left.addd.model.Building;
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
	
	private Entity testEntity;

	public GameModel(int width, int height) {
		this(width, height, 0, true);
	}
	
	public GameModel(int width, int height, long timeInHours, boolean initializeTiles) {
		this.width = width;
		this.height = height;
		this.tiles = new Tile[width][height];
		if(initializeTiles) {
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					tiles[i][j] = new Tile(this, i, j);
				}
			}
		}
		
		this.time = new Time(timeInHours);
		
		this.testEntity = new Entity("testguy", tiles[3][3], tiles[10][10]);
	}
	
	public Time getTime() {
		return time;
	}
	
	public Entity getTestEntity() {
		return testEntity;
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

		// Building data
		if(tile.hasBuilding()) {
			Building b = tile.getBuilding();
			query.append("Building: " + b.type.name());
			query.append(" Origin=" + pCoords(b.getOriginX(), b.getOriginY()));
			query.append(" Size=" + b.getWidth() + "x" + b.getHeight());
		} else {
			query.append("No building");
		}
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
	}

	private void updateTiles(int ticks) {
		for(Tile[] ts: tiles) {
			for(Tile t: ts) {
				t.update(ticks);
			}
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

		return gameModel;
	}
}
