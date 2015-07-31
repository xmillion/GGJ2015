package com.left.addd.model;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

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
	private EntityManager entityManager;

	/**
	 * For serializer use only! It's incomplete.
	 */
	private GameModel(long timeInMinutes) {
		this.time = new Time(timeInMinutes);
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
	 * @param timeInMinutes In game time
	 * @param tiles Pre-generated Tiles
	 * @param entities Pre-defined Entities
	 */
	public GameModel(long timeInMinutes, Tile[][] tiles, List<Entity> entities) {
		this(timeInMinutes, new TileManager(tiles), new EntityManager(entities));
	}
	
	/**
	 * Full constructor.
	 * @param timeInMinutes
	 * @param tileManager
	 * @param entityManager
	 */
	private GameModel(long timeInMinutes, TileManager tileManager, EntityManager entityManager) {
		this.time = new Time(timeInMinutes);
		this.tileManager = tileManager;
		this.entityManager = entityManager;
	}
	
	public GameModel(boolean oneTimeInitialize) {
		if (!oneTimeInitialize) {
			this.time = new Time(0);
			this.tileManager = new TileManager(40, 30, true);
			this.entityManager = new EntityManager();
			return;
		}
		
		this.time = new Time(0);
		
		// Tiles
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
		int width = testMap.length;
		int height = testMap[0].length;
		this.tileManager = new TileManager(width, height, false);
		Tile[][] tiles = tileManager.getTiles();
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				if (testMap[i][j] == 1) {
					tiles[i][j] = new Tile(tileManager, i, j, Tile.Type.ROAD);
				} else {
					tiles[i][j] = new Tile(tileManager, i, j, Tile.Type.EMPTY);
				}
			}
		}
		
		// Entities
		Entity alice = new NPC("Alice", "Alice is a homewrecker and gets with Bob and Chad", tiles[0][2], NPC.Type.STUDENT);
		Entity bob = new NPC("Bob", "Bob is a hardworking family man", tiles[2][2], NPC.Type.HERO);
		Entity chad = new NPC("Chad", "Fucking Chad", tiles[13][14], NPC.Type.FACULTY);
		Entity hotel = new Building("Hotel", "This is where Alice and Chad go when they get it on", tiles[12][0], Building.Type.HOUSE);
		Entity factory = new Building("Bob's Workplace", "This is where Bob works while Alice cheats on him", tiles[1][13], Building.Type.FACTORY);
		
		List<Entity> entities = Arrays.asList(
			hotel,
			factory,
			new Building("School", "This is Alice's school", tiles[5][3], Building.Type.SCHOOL),
			new Building("Library", "Students study here", tiles[10][13], Building.Type.LIBRARY),
			alice,
			bob,
			chad,
			new NPC("Bob's Bad Influence #1", "Always distracts Bob", tiles[2][7], NPC.Type.FACULTY),
			new NPC("Bob's Bad Influence #2", "Always distracts Bob", tiles[11][11], NPC.Type.FACULTY),
			new NPC("POPO", "Reminds Bob to be an upstanding, working citizen", tiles[9][7], NPC.Type.POLICE),
			new NPC("Some Random Dude", "He looks a stoned", tiles[11][3], NPC.Type.FACULTY),
			new NPC("Runner", "He likes shorts. They're comfortable and easy to wear", tiles[12][4], NPC.Type.FACULTY)
		);
		alice.addObjective(new Objective(bob));
		alice.addObjective(new Objective(chad));
		alice.addObjective(new Objective(hotel));
		
		bob.addObjective(new Objective(alice));
		bob.addObjective(new Objective(factory));
		
		// this isn't an actual cycle, I can't do stuff like that until predefined objectives are implemented.
		ArrayList<Objective> bobCycle = new ArrayList<Objective>(10);
		bobCycle.add(new Objective(entities.get(8)));
		bobCycle.add(new Objective(entities.get(7)));
		bobCycle.add(new Objective(entities.get(8)));
		bobCycle.add(new Objective(entities.get(7)));
		bobCycle.add(new Objective(entities.get(8)));
		bobCycle.add(new Objective(entities.get(7)));
		bobCycle.add(new Objective(entities.get(8)));
		bobCycle.add(new Objective(entities.get(7)));
		bobCycle.add(new Objective(entities.get(8)));
		bob.addObjective(new Objective(entities.get(7), null, null, bobCycle));
		// this might not work, until objective priorities have been implemented. Here we want the factory objective to go to the top of Bob's queue.
		bob.addObjective(new Objective(entities.get(9), null, null, Arrays.asList(new Objective(factory))));
		
		chad.addObjective(new Objective(alice));
		chad.addObjective(new Objective(hotel));
		
		ArrayList<Objective> runnerCycle = new ArrayList<Objective>(10);
		runnerCycle.add(new Objective(entities.get(9)));
		runnerCycle.add(new Objective(entities.get(8)));
		runnerCycle.add(new Objective(entities.get(10)));
		runnerCycle.add(new Objective(entities.get(9)));
		runnerCycle.add(new Objective(entities.get(8)));
		runnerCycle.add(new Objective(entities.get(10)));
		runnerCycle.add(new Objective(entities.get(9)));
		runnerCycle.add(new Objective(entities.get(8)));
		runnerCycle.add(new Objective(entities.get(10)));
		entities.get(11).addObjective(new Objective(entities.get(10), null, null, runnerCycle));
		
		this.entityManager = new EntityManager(entities);
	}
	
	public Time getTime() {
		return time;
	}
	
	public TileManager getTileManager() {
		return tileManager;
	}
	
	public int getWidth() {
		return tileManager.width;
	}
	
	public int getHeight() {
		return tileManager.height;
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
	
	public EntityManager getEntityManager() {
		return entityManager;
	}
	
	public List<Entity> getEntities() {
		return entityManager.getEntities();
	}
	
	public void update(float delta) {
		int ticks = time.update(delta);
		if (ticks > 0) {
			tileManager.update(ticks);
			entityManager.update(ticks);
		}
	}
	
	// *** Serialization ***
	
	public void save(Json json) {
		json.writeObjectStart();
		json.writeValue("time", time.getTime());
		tileManager.save(json);
		entityManager.save(json);
		json.writeObjectEnd();
	}

	public static GameModel load(JsonValue jsonData) {
		long timeInMinutes = jsonData.getLong("time");
		GameModel gameModel = new GameModel(timeInMinutes);
		gameModel.tileManager = TileManager.load(jsonData, gameModel);
		gameModel.entityManager = EntityManager.load(jsonData, gameModel);
		return gameModel;
	}
}
