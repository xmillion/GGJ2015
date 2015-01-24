package com.left.addd.view;

import com.left.addd.model.Building;
import com.left.addd.model.Direction;
import com.left.addd.model.Network;
import com.left.addd.model.Tile;
import com.left.addd.utils.Res;

/**
 * Links to the actual tile graphics.
 */
public enum TileImageType {
	NONE(""),
	BLANK("tile"),
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
	ROAD_X("road-x"),
	HOUSE("house"),
	FACTORY("factory");
	
	private final String fileName;
	private TileImageType(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return Res.TILES + fileName;
	}
	
	/**
	 * Returns the appropriate TileImageType given the Tile.
	 * @param tile legitimate tile
	 */
	public static TileImageType getImageFromTile(Tile tile) {
		if(tile.hasBuilding()) {
			Building b = tile.getBuilding();
			if(tile.x == b.getOriginX() && tile.y == b.getOriginY()) {
				switch(b.type) {
				case HOUSE:
					return TileImageType.HOUSE;
				case FACTORY:
					return TileImageType.FACTORY;
				default:
					return TileImageType.NONE;
				}
			} else {
				return TileImageType.NONE;
			}
		} else if(tile.hasNetwork()) {
			Network network = tile.getNetwork();
			// Check neighbours
			boolean n = network.getNeighbour(Direction.NORTH) != null;
			boolean e = network.getNeighbour(Direction.EAST) != null;
			boolean s = network.getNeighbour(Direction.SOUTH) != null;
			boolean w = network.getNeighbour(Direction.WEST) != null;
			
			// Update self
			switch(network.type) {
			case ROAD:
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
			default:
				return TileImageType.NONE;
			}
		}
		
		// unreachable code
		return TileImageType.NONE;
	}
}