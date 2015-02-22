package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.StringBuilder;
import com.left.addd.model.GameModel;
import com.left.addd.model.Tile;
import com.left.addd.model.Time;

/**
 * GameModel is the model for this game. It represents the logic behind this game.
 * It does not know anything about how the representation is drawn, or how the player interacts with it.
 * Reference: https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class GameModel {
	private Time time;
	private TileManager tileManager;
	private EntityManager em;

	/**
	 * For serializer use only! It's incomplete.
	 */
	private GameModel(long timeInHours) {
		this.time = new Time(timeInHours);
	}
	
	/**
	 * New GameModel for a new game
	 * @param width
	 * @param height
	 */
	public GameModel(int width, int height) {
		this(0, new TileManager(width, height, true), new EntityManager());
	}
	
	/**
	 * Generate a GameModel with predefined parts.
	 * @param timeInHours In game time
	 * @param tiles Pre-generated Tiles
	 * @param entities Pre-defined Entities
	 */
	public GameModel(long timeInHours, Tile[][] tiles, List<Entity> entities) {
		this(timeInHours, new TileManager(tiles), new EntityManager(entities));
	}
	
	/**
	 * Full constructor.
	 * @param timeInHours
	 * @param tileManager
	 * @param entityManager
	 */
	private GameModel(long timeInHours, TileManager tileManager, EntityManager entityManager) {
		this.time = new Time(timeInHours);
		this.tileManager = tileManager;
		this.em = entityManager;
	}
	
	// TODO replicate this info in json format
	/*
	public GameModel(int width, int height, long timeInHours, boolean initializeTiles) {
		this.width = width;
		this.height = height;
		this.tiles = new Tile[width][height];
		this.em = new EntityManager();
		
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
	}
	*/
	
	public Time getTime() {
		return time;
	}
	
	public TileManager getTileManager() {
		return tileManager;
	}
	
	public Tile[][] getTiles() {
		return tileManager.getTiles();
	}
	
	public Tile getTile(int x, int y) {
		return tileManager.getTile(x, y);
	}

	/**
	 * Provides information on the given Tile.
	 * 
	 * @param x
	 * @param y
	 * @return Tile info
	 */
	public String query(int x, int y) {
		return tileManager.query(x, y);
	}
	
	public List<Entity> getEntities() {
		return em.getEntities();
	}
	
	public void update(float delta) {
		int ticks = time.update(delta);
		tileManager.update(ticks);
		em.update(ticks);
	}
	
	// *** Serialization ***
	
	public void save(Json json) {
		json.writeObjectStart("game");
		json.writeValue("time", time.getTime());
		tileManager.save(json);
		em.save(json);
		json.writeObjectEnd();
	}

	public static GameModel load(JsonValue jsonData) {
		JsonValue gameJson = jsonData.get("game");
		long timeInHours = gameJson.getLong("time");
		GameModel gameModel = new GameModel(timeInHours);
		gameModel.tileManager = TileManager.load(gameJson, gameModel);
		gameModel.em = EntityManager.load(gameJson, gameModel);
		return gameModel;
	}
}
