package com.left.addd.view;

import com.left.addd.model.Direction;
import com.left.addd.model.Tile;
import com.left.addd.utils.Res;

/**
 * Links to the actual tile graphics.
 */
public enum TileImageType {
	NONE(""),
	BLANK("grass"),
	ROCK("rock"),
	SAND("sand"),
	GRASS("grass"),
	WATER("water"),
	PATH("path"),
	ROAD_H("road-h"),
	ROAD_V("road-v"),
	ROAD_N("road-n"),
	ROAD_E("road-e"),
	ROAD_S("road-s"),
	ROAD_W("road-w"),
	ROAD_NE("road-ne"),
	ROAD_NW("road-nw"),
	ROAD_SE("road-se"),
	ROAD_SW("road-sw"),
	ROAD_TN("road-tn"),
	ROAD_TE("road-te"),
	ROAD_TS("road-ts"),
	ROAD_TW("road-tw"),
	ROAD_X("road-x");
	
	private final String fileName;
	private TileImageType(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return Res.TILES + fileName;
	}
	
	/**
	 * Returns the appropriate TileImageType.
	 * @param tile
	 * @return
	 */
	public static TileImageType getImageTypeFromTile(Tile tile) {
		switch(tile.getType()) {
		case EMPTY:
			return GRASS;
		case BUILDING:
			return BLANK;
		case ROAD:
			return getRoadImageTypeFromTile(tile);
		case PATH:
			return PATH;
		default:
			return BLANK;
		}
	}
	
	/**
	 * Returns the appropriate Road TileImageType. Road is dynamic and is affected by its neighbours.
	 * @param tile
	 * @return
	 */
	private static TileImageType getRoadImageTypeFromTile(Tile tile) {
		// Check neighbours
		boolean n = tile.getNeighbour(Direction.NORTH) != null;
		boolean e = tile.getNeighbour(Direction.EAST) != null;
		boolean s = tile.getNeighbour(Direction.SOUTH) != null;
		boolean w = tile.getNeighbour(Direction.WEST) != null;
		
		// Update self
		if(n && e && s && w) {
			return TileImageType.ROAD_X;
		} else if(n && s) {
			if(e) {
				return TileImageType.ROAD_TE;
			} else if(w) {
				return TileImageType.ROAD_TW;
			} else {
				return TileImageType.ROAD_V;
			}
		} else if(e && w) {
			if(n) {
				return TileImageType.ROAD_TN;
			} else if(s) {
				return TileImageType.ROAD_TS;
			} else {
				return TileImageType.ROAD_H;
			}
		} else if(n) {
			if(e) {
				return TileImageType.ROAD_NE;
			} else if(w) {
				return TileImageType.ROAD_NW;
			} else {
				return TileImageType.ROAD_N;
			}
		} else if(s) {
			if(e) {
				return TileImageType.ROAD_SE;
			} else if(w) {
				return TileImageType.ROAD_SW;
			} else {
				return TileImageType.ROAD_S;
			}
		} else if(e) {
			return TileImageType.ROAD_E;
		} else if(w) {
			return TileImageType.ROAD_W;
		} else {
			// isolated road piece
			return TileImageType.ROAD_H;
		}
	}
}