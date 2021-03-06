package com.left.addd.model;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Network implements Disposable {

	public final NetworkType type;

	private Network[] neighbours;
	
	public Network(NetworkType type) {
		this.type = type;
		this.neighbours = new Network[Direction.values().length];
	}

	/**
	 * Creates a new Network with appropriate connections.
	 * The connections can be null.
	 */
	public Network(NetworkType type, Network north, Network east, Network south, Network west) {
		this(type);
		connect(Direction.NORTH, north);
		connect(Direction.EAST, east);
		connect(Direction.SOUTH, south);
		connect(Direction.WEST, west);
	}

	public Network getNeighbour(Direction dir) {
		return neighbours[dir.ordinal()];
	}
	
	// *** Networking ***

	public boolean connect(Direction dir, Network n) {
		if(n == null || this.type != n.type)
			return false;
		neighbours[dir.ordinal()] = n;
		n.neighbours[dir.opposite().ordinal()] = this;
		return true;
	}

	public boolean disconnect(Direction dir) {
		Network n = neighbours[dir.ordinal()];
		if(n != null) {
			n.neighbours[dir.opposite().ordinal()] = null;
			neighbours[dir.ordinal()] = null;
		}
		return true;
	}
	
	// *** Rules ***

	public void update(int delta) {
	}

	// *** Serialization ***
	
	/**
	 * Serialize a Tile into json.
	 * 
	 * @param json Json serializer, which will now have the tile's data.
	 * @param tile Tile to save
	 */
	public void save(Json json) {
		json.writeObjectStart("network");
		json.writeValue("type", this.type.name());
		json.writeObjectEnd();
	}

	/**
	 * Create a Tile using a json string.
	 * 
	 * @param data
	 * @return
	 */
	public static Network load(Json json, JsonValue jsonData, Network south, Network west) {
		NetworkType type = NetworkType.valueOf(jsonData.getString("type"));
		return new Network(type, null, null, south, west);
	}

	/**
	 * Call this when this is slated for deletion
	 */
	@Override
	public void dispose() {
		// Remove all network references to avoid leak
		for(Direction dir: Direction.values()) {
			disconnect(dir);
		}
	}
}
