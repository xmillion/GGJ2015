package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class TileManager {
	private final Tile[][] tiles;
	public final int width;
	public final int height;
	
	/**
	 * Create the TileManager with pre-loaded Tiles
	 * @param tiles
	 */
	public TileManager(Tile[][] tiles) {
		this.tiles = tiles;
		this.width = tiles.length;
		this.height = tiles[0].length;
	}
	
	/**
	 * Create the TileManager
	 * @param width
	 * @param height
	 * @param initializeTiles If true, this will construct all the Tiles. If false, then the caller must construct each Tile manually.
	 */
	public TileManager(int width, int height, boolean initializeTiles) {
		this.tiles = new Tile[width][height];
		this.width = width;
		this.height = height;
		
		if (initializeTiles) {
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					tiles[i][j] = new Tile(this, i, j);
				}
			}
		}
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
		return getTile(x, y).query();
	}
	
	public void update(int ticks) {
		for(Tile[] ts: tiles) {
			for(Tile t: ts) {
				t.update(ticks);
			}
		}
	}
	
	// *** Serialization ***

	public void save(Json json) {
		// TODO save tiles as a char array or string or something, to save space.
		json.writeObjectStart("tm");
		json.writeValue("width", width);
		json.writeValue("height", height);
		json.writeArrayStart("tiles");
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				tiles[i][j].save(json);
			}
		}
		json.writeArrayEnd();
		json.writeObjectEnd();
	}

	public static TileManager load(JsonValue jsonData, GameModel model) {
		JsonValue tileData = jsonData.get("tm");
		int width = tileData.getInt("width");
		int height = tileData.getInt("height");
		JsonValue tileJson = tileData.get("tiles");
		TileManager tileManager = new TileManager(width, height, false);
		JsonValue tileValue;
		for(int i = 0; i < tileJson.size; i++) {
			tileValue = tileJson.get(i);
			Tile tile = Tile.load(tileValue, tileManager);
			tileManager.tiles[tile.x][tile.y] = tile;
		}

		return tileManager;
	}
}
