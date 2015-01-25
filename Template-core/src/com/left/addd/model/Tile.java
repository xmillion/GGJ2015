package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.utils.Utils;
import java.util.ArrayList;

/**
 * This class is the representation of a discrete unit in a Grid.
 * A Grid is made up of many interconnected Tiles.
 */
public class Tile {
	private static Tile dummyTile;

	protected final GameModel gameModel;
	public final int x;
	public final int y;

	private Network network;

	public Tile(GameModel gameModel) {
		this(gameModel, -1, -1, null);
	}

	public Tile(GameModel gameModel, int x, int y) {
		this(gameModel, x, y, null);
	}

	/**
	 * Create a Tile.
	 * 
	 * @param gameModel Governing model
	 * @param x coordinate
	 * @param y coordinate
	 * @param building Building on this Tile
	 * @param network Network on this Tile
	 */
	public Tile(GameModel gameModel, int x, int y, Network network) {
		this.gameModel = gameModel;
		this.x = x;
		this.y = y;

		this.network = network;
	}

	public static Tile dummyTile() {
		if(dummyTile == null) {
			// never use other methods on dummyTiles
			dummyTile = new Tile(null, -1, -1, null);
		}
		return dummyTile;
	}

	public static boolean isDummyTile(Tile t) {
		return t.equals(dummyTile);
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		// connect neighbours if possible
		for(Direction dir: Direction.values()) {
			Tile t = getNeighbour(dir);
			if(t.hasNetwork()) {
				network.connect(dir, t.getNetwork());
			}
		}
		this.network = network;
	}

	public void clearNetwork() {
		// disconnect neighbours
		this.network.dispose();
		this.network = null;
	}

	public boolean hasNetwork() {
		return this.network != null;
	}
	
	public Tile getNeighbour(Direction dir) {
		switch(dir) {
		case NORTH:
			return gameModel.getTile(x, y + 1);
		case EAST:
			return gameModel.getTile(x + 1, y);
		case SOUTH:
			return gameModel.getTile(x, y - 1);
		case WEST:
			return gameModel.getTile(x - 1, y);
		default:
			// unreachable code
			return gameModel.getTile(x, y);
		}
	}
	
	public Tile tryGetNeighbour(Direction dir, Tile defaultTile) {
		Tile t = getNeighbour(dir);
		return Tile.isDummyTile(t) ? defaultTile : t;
	}
	
	public ArrayList<Tile> getNeighbours() {
        ArrayList<Tile> Available = new ArrayList<Tile>();
        for (Direction dir : Direction.values()) {
            if (checkForNetwork(dir)) {
                Available.add(getNeighbour(dir));
            }
        }
        return Available;
    }
    
    
    private boolean checkForNetwork(Direction dir) {
        if (!this.equals(getNeighbour(dir))) {
        	if (getNeighbour(dir).getNetwork() != null) {
        		if (getNeighbour(dir).getNetwork().type == NetworkType.ROAD) {
                    return true;
                } else {
                    return false;
                }
        	} else {
        		return false;
        	}
        } else {
            return false;
        }
    }

	// *** Rules ***

	public void update(int delta) {
		if(network != null) {
			network.update(delta);
		}
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
		if(hasNetwork()) {
			network.save(json);
		}
		json.writeObjectEnd();
	}

	/**
	 * Create a Tile using a json string.
	 * 
	 * @param data
	 * @return
	 */
	public static Tile load(Json json, JsonValue jsonData, GameModel gameModel) {
		int x = jsonData.getInt("x");
		int y = jsonData.getInt("y");

		JsonValue networkData = jsonData.get("network");
		Network network = null;
		if(networkData != null) {
			// Can only load tiles[x][y-1] and tiles[x-1][y]
			// because the next ones haven't been initialized yet.
			network = Network.load(json, networkData, gameModel.getTile(x, y - 1).getNetwork(),
					gameModel.getTile(x - 1, y).getNetwork());
		}

		return new Tile(gameModel, x, y, network);
	}

	/**
	 * A tile is identified by its (x,y) coordinates.
	 */
	@Override
	public int hashCode() {
		return x * gameModel.width + y;
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
		return "Tile " + Utils.pCoords(x, y);
	}
}
