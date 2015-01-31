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
import com.left.addd.services.EntityManager;

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
	
	private List<StateChangedListener<GameModel>> listeners;
	
	EntityManager em = EntityManager.getInstance();

	public GameModel(int width, int height) {
		this(width, height, 0, true);
	}
	
	public GameModel(int width, int height, long timeInHours, boolean initializeTiles) {
		this.width = width;
		this.height = height;
		this.tiles = new Tile[width][height];
		if(initializeTiles) {
			int[][] testMap = new int[][]{
					{0,0,1,1,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,1,0,0,0,0,0,1,1,1,1,1,1},
					{0,0,1,1,1,1,0,1,0,1,0,0,0,0,0},
					{0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
					{0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
					{0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
					{0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
					{0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
					{0,0,0,0,1,1,1,1,1,1,1,1,1,1,1},
					{1,1,1,1,1,0,0,1,0,0,0,0,1,0,0},
					{1,0,0,0,1,0,0,0,0,0,0,0,1,0,0},
					{1,0,0,1,1,0,0,0,0,0,0,1,1,0,0},
					{1,0,0,0,1,1,1,1,1,1,1,1,1,0,0},
					{0,0,0,0,1,0,0,0,1,0,0,0,0,0,1},
					{1,1,1,1,1,0,0,0,1,1,1,1,1,1,1},
			};
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					if (testMap[i][j] == 1){
						tiles[i][j] = new Tile(this, i, j, new Network(NetworkType.ROAD, null, null, this.getTile(i, j-1).getNetwork(), this.getTile(i-1, j).getNetwork()));
						} else {
							tiles[i][j] = new Tile(this, i, j, null);
						}
					}
			}
		}
		
		this.time = new Time(timeInHours);
		
		Building testBuilding = new Building(Building.Type.HOUSE, tiles[12][0]);
		testBuilding.addMetadata("Name", "Hotel");
		testBuilding.addMetadata("Description", "This is where Alice and Chad go when they get it on");
		
		Building testBuilding2 = new Building(Building.Type.FACTORY, tiles[1][13]);
		testBuilding2.addMetadata("Name", "Bob's Workplace");
		testBuilding2.addMetadata("Description", "This is where Bob works while Alice cheats on him");
		
		Building testBuilding3 = new Building(Building.Type.SCHOOL, tiles[5][3]);
		testBuilding3.addMetadata("Name", "School");
		testBuilding3.addMetadata("Description", "This is Alice's school");
		
		Building testBuilding4 = new Building(Building.Type.LIBRARY, tiles[10][13]);
		testBuilding4.addMetadata("Name", "Library");
		testBuilding4.addMetadata("Description", "Students study here");
		
		NPC testEntity = new NPC(NPC.Type.STUDENT, tiles[0][2]);
		testEntity.addMetadata("Name", "Alice");
		testEntity.addMetadata("Description", "Alice is a homewrecker and gets with Bob and Chad");

		NPC testEntity2 = new NPC(NPC.Type.HERO, tiles[2][2]);
		testEntity2.addMetadata("Name", "Bob");
		testEntity2.addMetadata("Description", "Bob is a hardworking family man");
		testEntity.setTargetEntity(testEntity2);

		NPC testEntity3 = new NPC(NPC.Type.FACULTY, tiles[13][14]);
		testEntity3.addMetadata("Name", "Chad");
		testEntity3.addMetadata("Description", "Fucking Chad");
		
		NPC testEntity4 = new NPC(NPC.Type.FACULTY, tiles[2][7]);
		testEntity4.addMetadata("Name", "Bob's Bad Influence #1");
		testEntity4.addMetadata("Description", "Always distracts Bob");

		NPC testEntity5 = new NPC(NPC.Type.FACULTY, tiles[11][11]);
		testEntity5.addMetadata("Name", "Bob's Bad Influence #2");
		testEntity5.addMetadata("Description", "Always distracts Bob");

		NPC testEntity6 = new NPC(NPC.Type.POLICE, tiles[9][7]);
		testEntity6.addMetadata("Name", "POPO");
		testEntity6.addMetadata("Description", "Reminds Bob to be an upstanding, working citizen");
		
		NPC testEntity7 = new NPC(NPC.Type.FACULTY, tiles[11][3]);
		testEntity7.addMetadata("Name", "Some Random Dude");
		testEntity7.addMetadata("Description", "He looks a stoned");
		
		NPC testEntity8 = new NPC(NPC.Type.FACULTY, tiles[11][4]);
		testEntity8.addMetadata("Name", "Runner");
		testEntity8.addMetadata("Description", "He likes shorts. They're comfortable and easy to wear");

		testEntity.addObjective(testEntity2, testEntity3);
		testEntity.addObjective(testEntity3, testBuilding);
		testEntity2.addObjective(testEntity, testBuilding2);
		testEntity2.addObjective(testEntity4,  testEntity5);
		testEntity2.addObjective(testEntity5,  testEntity4);
		testEntity2.addObjective(testEntity6, testBuilding2);
		testEntity3.addObjective(testEntity,  testBuilding);
		testEntity8.setTargetEntity(testEntity7);
		testEntity8.addObjective(testEntity6, testEntity5);
		testEntity8.addObjective(testEntity5, testEntity7);
		testEntity8.addObjective(testEntity7, testEntity6);

		em.addEntity(testBuilding);
		em.addEntity(testBuilding2);
		em.addEntity(testBuilding3);
		em.addEntity(testBuilding4);
		em.addEntity(testEntity);
		em.addEntity(testEntity2);
		em.addEntity(testEntity3);
		em.addEntity(testEntity4);
		em.addEntity(testEntity5);
		em.addEntity(testEntity6);
		em.addEntity(testEntity7);
		em.addEntity(testEntity8);
		testEntity.move(Direction.NORTH);
		
		this.listeners = new ArrayList<StateChangedListener<GameModel>>();
	}
	
	public Tile getTileByEntityProperty(){
		return new Tile(this,-1,-1,null);
	}
	
	public Time getTime() {
		return time;
	}
	
	public List<Entity> getEntities() {
		return em.getEntities();
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
	
	public void addListener(StateChangedListener<GameModel> listener) {
		this.listeners.add(listener);
		listener.OnStateChanged(this);
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
		em.checkObjectivesAndUpdateTargets();
	}

	private void updateTiles(int ticks) {
		for(Tile[] ts: tiles) {
			for(Tile t: ts) {
				t.update(ticks);
			}
		}
	}
	
	private void updateEntities(int ticks) {
		for(Entity e : em.getEntities()) {
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
			gameModel.em.addEntity(entity);
		}
		

		return gameModel;
	}
}
