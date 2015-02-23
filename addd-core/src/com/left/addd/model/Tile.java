package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.StringBuilder;
import com.left.addd.utils.Utils;

import java.util.ArrayList;

/**
 * This class is the representation of a discrete unit in a Grid.
 * A Grid is made up of many interconnected Tiles.
 */
public class Tile {
	private static Tile dummyTile = new Tile(null, -1, -1, null);
	
	public enum Type {
		EMPTY("grass", false, false),
		BUILDING("building", false, false),
		ROAD("road", true, true),
		PATH("path", true, false);
		
		/** Used by renderer. If isDynamic == true, use getDynamicAssetName instead. */
		public final String assetName;
		/** If true, then it can be used for pathfinding. */
		public final boolean isNetwork;
		/** If true, then the asset or properties can change based on its neighbouring tiles. */
		public final boolean isDynamic;
		private Type(String assetName, boolean isNetwork, boolean isDynamic) {
			this.assetName = assetName;
			this.isNetwork = isNetwork;
			this.isDynamic = isDynamic;
		}
		
		public String getDynamicAssetName(boolean n, boolean e, boolean s, boolean w) {
			if (!isDynamic) {
				return assetName;
			}

			String dynamic;
			if(n && e && s && w) {
				dynamic = "-x";
			} else if(n && s) {
				if(e) {
					dynamic = "-te";
				} else if(w) {
					dynamic = "-tw";
				} else {
					dynamic = "-v";
				}
			} else if(e && w) {
				if(n) {
					dynamic = "-tn";
				} else if(s) {
					dynamic = "-ts";
				} else {
					dynamic = "-h";
				}
			} else if(n) {
				if(e) {
					dynamic = "-ne";
				} else if(w) {
					dynamic = "-nw";
				} else {
					dynamic = "-n";
				}
			} else if(s) {
				if(e) {
					dynamic = "-se";
				} else if(w) {
					dynamic = "-sw";
				} else {
					dynamic = "-s";
				}
			} else if(e) {
				dynamic = "-e";
			} else if(w) {
				dynamic = "-w";
			} else {
				// isolated road piece
				dynamic = "-o";
			}
			
			return assetName + dynamic;
		}
	}

	protected final TileManager manager;
	public final int x;
	public final int y;
	private Type type;

	/**
	 * Create a dummy tile. Don't use these.<br>
	 * Instead, check if a tile is a dummy tile with Tile.isDummyTile(t);<br>
	 * before using them.
	 * @param tileManager
	 */
	public Tile(TileManager tileManager) {
		this(tileManager, -1, -1, Type.EMPTY);
	}

	/**
	 * Create a Tile.
	 * @param tileManager
	 * @param x
	 * @param y
	 */
	public Tile(TileManager tileManager, int x, int y) {
		this(tileManager, x, y, Type.EMPTY);
	}
	
	/**
	 * Create a Tile.
	 * @param tileManager
	 * @param x
	 * @param y
	 * @param type
	 */
	public Tile(TileManager tileManager, int x, int y, Type type) {
		this.manager = tileManager;
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public static Tile dummyTile() {
		return dummyTile;
	}

	public static boolean isDummyTile(Tile t) {
		return dummyTile.equals(t);
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean isNetwork() {
		return type.isNetwork;
	}

	public boolean isDynamic() {
		return type.isDynamic;
	}
	
	/**
	 * Returns the tile in the given direction.<br>
	 * Never returns null. Check if it's valid using Tile.isDummyTile() first!
	 * @param dir Direction from this tile
	 * @return Tile in that direction
	 */
	public Tile tryGetNeighbour(Direction dir) {
		switch(dir) {
		case NORTH:
			return manager.getTile(x, y + 1);
		case EAST:
			return manager.getTile(x + 1, y);
		case SOUTH:
			return manager.getTile(x, y - 1);
		case WEST:
			return manager.getTile(x - 1, y);
		default:
			// unreachable code
			return manager.getTile(x, y);
		}
	}
	
	/**
	 * Returns the tile in the given direction.<br>
	 * Returns null if there isn't a neighbour there.
	 * @param dir
	 * @return
	 */
	public Tile getNeighbour(Direction dir) {
		Tile t = tryGetNeighbour(dir);
		return Tile.isDummyTile(t) ? null : t;
	}
	
	/**
	 * Returns up to 4 Tiles, which are the tiles in each direction.<br>
	 * Check if it's valid using Tile.isDummyTile() first!
	 * @return Tiles in all 4 directions
	 */
	public ArrayList<Tile> getNeighbours() {
		// TODO double check if we need a getNeighbours for non-network tiles
        ArrayList<Tile> available = new ArrayList<Tile>();
        for (Direction dir : Direction.values()) {
        	available.add(tryGetNeighbour(dir));
        }
        return available;
    }
	
	public boolean isNeighbour(Tile t) {
		// TODO double check if we need a isNeighbour for network tiles
		for (Direction dir : Direction.values()) {
			if (t.equals(tryGetNeighbour(dir))) {
				return true;
			}
		}
		return false;
	}
	
	public Direction getDirection(Tile neighbour) {
		if (this.equals(neighbour)) {
			return null;
		}
		int deltaX = neighbour.x - this.x;
		int deltaY = neighbour.y - this.y;
		// slice diagonally
		if (deltaX > deltaY) {
			// south or east
			if (deltaY < 0 && deltaX < -deltaY) {
				return Direction.SOUTH;
			} else {
				return Direction.EAST;
			}
		} else {
			if (deltaY > 0 && deltaY > -deltaX) {
				return Direction.NORTH;
			} else {
				return Direction.WEST;
			}
		}
	}

	// *** Rules ***

	public void update(int delta) {
		// nothing here at the moment
	}
	
	/**
	 * Provides information about this Tile
	 * 
	 * @return Tile info
	 */
	public String query() {
		// TODO query's final form should return a Query object, not a string
		if(Tile.isDummyTile(this)) {
			log("Query", "Not a Tile " + pCoords(this.x, this.y));
			return "";
		}

		StringBuilder query = new StringBuilder();

		// Tile data
		query.append("Tile ");
		query.append(pCoords(this.x, this.y));
		query.append(" is type ");
		query.append(this.getType().name());
		query.append(".\n");

		// Network data
		for(Direction dir: Direction.values()) {
			Tile neighbour = tryGetNeighbour(dir);
			if(Tile.isDummyTile(neighbour)) {
				query.append("No ");
				query.append(dir.name());
				query.append(" neighbour.\n");
			} else {
				query.append(dir.name());
				query.append(" neighbour is type ");
				query.append(neighbour.getType().name());
				if (neighbour.isNetwork()) {
					query.append(" (is network).\n");
				} else {
					query.append(" (not network).\n");
				}
			}
		}
		log(":::Query:::", query.toString());
		return query.toString();
	}

	// *** Serialization ***

	/**
	 * Serialize a Tile into json.
	 * 
	 * @param json Json serializer, which will now have the tile's data.
	 * @param tile Tile to save
	 */
	public void save(Json json) {
		json.writeObjectStart();
		json.writeValue("x", this.x);
		json.writeValue("y", this.y);
		json.writeValue("type", type.name());
		json.writeObjectEnd();
	}

	/**
	 * Create a Tile using a json string.
	 * 
	 * @param data
	 * @return
	 */
	public static Tile load(JsonValue jsonData, TileManager tileManager) {
		int x = jsonData.getInt("x");
		int y = jsonData.getInt("y");
		Type type = Type.valueOf(jsonData.getString("type"));
		return new Tile(tileManager, x, y, type);
	}

	/**
	 * A tile is identified by its (x,y) coordinates.
	 */
	@Override
	public int hashCode() {
		return (manager == null) ? -1 : x * manager.width + y;
	}

	/**
	 * A tile is identified by its (x,y) coordinates.
	 */
	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if(o instanceof Tile) {
			Tile t = (Tile) o;
			result = x == t.x && y == t.y;
		}
		return result;
	}

	/**
	 * Returns (x,y) coordinates.
	 */
	@Override
	public String toString() {
		return "Tile " + pCoords(x, y);
	}
}
