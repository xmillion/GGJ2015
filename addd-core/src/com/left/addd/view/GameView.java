package com.left.addd.view;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import com.left.addd.model.*;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.left.addd.AdddGame;
import com.left.addd.model.GameModel;
import com.left.addd.services.SoundManager.SoundList;
import com.left.addd.utils.Res;
import com.left.addd.view.Panner;
import com.left.addd.view.PannerDesktop;
import com.left.addd.view.PannerMobile;
import com.left.addd.model.Direction;
import com.left.addd.model.Entity;
import com.left.addd.model.Tile;
import com.left.addd.view.GameView;
import com.left.addd.view.PannerAbstract;
import com.left.addd.view.TileImageType;

/**
 * Manages the drawing of the model to the screen and player controls. Reference:
 * https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class GameView implements InputProcessor {
	public static final int TILE_WIDTH = Res.TILE_WIDTH;
	public static final int TILE_HEIGHT = Res.TILE_HEIGHT;
	/** Defined by the number of {@link Buttons} constants. It's one higher than the biggest value constant there. */
	public static final int HOVER = 5;

	private final AdddGame game;
	private final GameModel gameModel;
	protected OrthographicCamera viewCamera;

	// Camera data
	private Panner panner;

	// I/O data
	/**
	 * Last pressed data for each mouse button, plus hover.<br>
	 * The indexes are defined by {@link Buttons}, and hover by GameView.HOVER.
	 */
	private ButtonPosition[] coordinates;
	/** The last {@link Buttons} that was pressed. */
	private int buttonTouched;
	/** Don't use. */
	private Vector3 touchPoint;
	
	private Vector3 tooltip;
	private Entity tooltipEntity;
	
	// Rendering data
	private Color hoverColor;
	private static final Color plainColor = new Color(1, 1, 1, 1);
	private static final Color highlightColor = new Color(0.7f, 1, 0.7f, 1);
	private static final Color selectColor = new Color(0.7f, 1, 0.7f, 1);
	private static final Color queryColor = new Color(1, 0.7f, 1, 1);

	private EntityLayer entityLayer;

	// Assets
	private final Map<TileImageType, Image> tileImageCache;
	private final TileImageType[][] tileImageTypes;

	// ********************
	// *** Constructor ****
	// ********************

	public GameView(AdddGame game, GameModel model, TextureAtlas atlas) {
		this.game = game;
		this.gameModel = model;
		this.viewCamera = new OrthographicCamera();

		Vector3 pannerMin = new Vector3((-3) * TILE_WIDTH, (-3) * TILE_HEIGHT, 0);
		Vector3 pannerMax = new Vector3((gameModel.getWidth() + 3) * TILE_WIDTH, (gameModel.getHeight() + 3)
				* TILE_HEIGHT, 0);
		switch(Gdx.app.getType()) {
		case Applet:
		case Desktop:
		case HeadlessDesktop:
		case WebGL:
			this.panner = new PannerDesktop(atlas, pannerMin, pannerMax);
			break;
		case Android:
		case iOS:
			this.panner = new PannerMobile(atlas, pannerMin, pannerMax);
			break;
		}

		coordinates = new ButtonPosition[HOVER + 1];
		for (int i=0; i<coordinates.length; i++) {
			coordinates[i] = new ButtonPosition(gameModel.getWidth(), gameModel.getHeight()); 
		}
		buttonTouched = Buttons.LEFT;
		touchPoint = new Vector3();
		
		tooltip = new Vector3();
		tooltipEntity = null;
		
		hoverColor = plainColor;

		entityLayer = new EntityLayer(atlas, gameModel.getTileManager(), gameModel.getEntityManager());

		// Load all the tile images into cache
		this.tileImageCache = new EnumMap<TileImageType, Image>(TileImageType.class);
		for(TileImageType type: TileImageType.values()) {
			AtlasRegion region = atlas.findRegion(type.getFileName());
			if(region != null) {
				Image image = new Image(region);
				tileImageCache.put(type, image);
			}
		}

		this.tileImageTypes = new TileImageType[gameModel.getWidth()][gameModel.getHeight()];
		updateAllTiles();
	}

	// ********************
	// **** Internals *****
	// ********************

	public GameModel getModel() {
		return gameModel;
	}
	
	public Tile getLastPressedTile() {
		int x = coordinates[buttonTouched].getX();
		int y = coordinates[buttonTouched].getY();
		return gameModel.getTile(x, y);
	}

	// ********************
	// ****** Camera ******
	// ********************

	/**
	 * Checks for arrow keys being pressed, and pans accordingly.
	 * Should this be moved into PannerDesktop?
	 */
	private void panKeyboard() {
		if(Gdx.input.isKeyPressed(Keys.UP)) {
			panner.pan(0, PannerAbstract.DEFAULT_PAN, 0);
		} else if(Gdx.input.isKeyPressed(Keys.DOWN)) {
			panner.pan(0, -PannerAbstract.DEFAULT_PAN, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.LEFT)) {
			panner.pan(-PannerAbstract.DEFAULT_PAN, 0, 0);
		} else if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
			panner.pan(PannerAbstract.DEFAULT_PAN, 0, 0);
		}
	}

	// ********************
	// * Input Processing *
	// ********************

	@Override
	public boolean keyDown(int keycode) {
		//log("GameView", "KeyDown " + keycode);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		//log("GameView", "KeyUp " + keycode);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		//log("GameView", "KeyTyped " + character + " 0x" + Integer.toHexString(character));
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		//log("GameView", "touchDown " + pCoords(screenX, screenY) + " pointer=" + pointer + " button=" + button);
		buttonTouched = button;
		if(panner.touchDown(screenX, screenY, pointer, button)) {
			// Panning
			return true;
		}
		
		// We otherwise don't do anything in touchDown, all the game interaction is done on touchUp.
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		//log("GameView", "touchUp " + pCoords(screenX, screenY) + " pointer=" + pointer + " button=" + button);
		if(panner.touchUp(screenX, screenY, pointer, button)) {
			// Panning
			return true;
		}
		
		// Update I/O data
		buttonTouched = button;
		touchPoint.set(screenX, screenY, 0);
		panner.unproject(touchPoint);
		coordinates[button].update(screenX, screenY, touchPoint.x / TILE_WIDTH, touchPoint.y / TILE_HEIGHT);
		boolean positionIsValidTile = coordinates[button].isTileCoordinateValid();
		
		// Perform actions
		switch(button) {
		case Buttons.LEFT:
			if (positionIsValidTile) {
				game.getSound().play(SoundList.CLICK);
				
				Entity entity = entityLayer.findEntityInTarget(coordinates[button].getTileCoordinates(), coordinates[button].getX(), coordinates[button].getY());
				if (entity != null) {
					// Interact with the entity
					tooltipEntity = entity;
					// If there are click functions in Entity, create them in Entity and call them here.
					entityLayer.selectEntity(entity);
				} else {
					// Interact with the tile
					Tile t = gameModel.getTile(coordinates[button].getX(), coordinates[button].getY());
					// If there are click functions in Tile, create them in Tile, and call them here.
				}
			}
			break;
		case Buttons.RIGHT:
			entityLayer.deselectAllEntities();
			tooltipEntity = null;
			break;
		case Buttons.MIDDLE:
		case Buttons.BACK:
		case Buttons.FORWARD:
		}

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// Panning
		if(panner.touchDragged(screenX, screenY, pointer, buttonTouched)) {
			return true;
		}
		// If there are draggable functions with Tiles, create the method in Tile, and call them here.
		// return true if meaningful action is taken.
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// Update I/O data
		touchPoint.set(screenX, screenY, 0);
		panner.unproject(touchPoint);
		coordinates[HOVER].update(screenX, screenY, touchPoint.x / TILE_WIDTH, touchPoint.y / TILE_HEIGHT);
		if(coordinates[HOVER].isTileCoordinateValid()) {
			Entity entity = entityLayer.findEntityInTarget(coordinates[HOVER].getTileCoordinates(), coordinates[HOVER].getX(), coordinates[HOVER].getY());
			if (entity != null) {
				// Interact with the entity
				tooltipEntity = entity;
				// If there are hover functions with Entities, create the method in Entity, and call them here.
				entityLayer.hoverEntity(entity);
			} else {
				// Interact with the tile
				Tile t = gameModel.getTile(coordinates[HOVER].getX(), coordinates[HOVER].getY());
				// If there are hover functions with Tiles, create the method in Tile, and call them here.
				hoverColor = highlightColor;
			}
		}

		tooltip.set(screenX, screenY, 0);
		panner.unproject(tooltip);

		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	// ********************
	// * Tile Processing **
	// ********************

	/**
	 * Call this to update the Tile View for the given Tile coordinate. Note: only tile.x and tile.y are used.
	 *
	 * @param tile Coordinate of Tile to update.
	 */
	private void updateTile(int x, int y) {
		Tile tile = gameModel.getTile(x, y);
		if (tile.isNetwork() || tile.isDynamic()) {
			// Update tile and its neighbours
			setTileImageType(tile);
			setTileImageType(tile.tryGetNeighbour(Direction.NORTH));
			setTileImageType(tile.tryGetNeighbour(Direction.EAST));
			setTileImageType(tile.tryGetNeighbour(Direction.SOUTH));
			setTileImageType(tile.tryGetNeighbour(Direction.WEST));
		} else {
			// Update just tile.
			setTileImageType(tile);
		}
	}

	/**
	 * Update all the tile views.
	 */
	private void updateAllTiles() {
		for(int i = 0; i < gameModel.getWidth(); i++) {
			for(int j = 0; j < gameModel.getHeight(); j++) {
				setTileImageType(gameModel.getTile(i, j));
			}
		}
	}

	/**
	 * Updates the TileImageType for the given Tile coordinate.
	 *
	 * @param x
	 * @param y
	 */
	private void setTileImageType(Tile tile) {
		if(Tile.isDummyTile(tile)) {
			return;
		}
		tileImageTypes[tile.x][tile.y] = TileImageType.getImageTypeFromTile(tile);
	}

	// ********************
	// **** Rendering *****
	// ********************

	public void render(SpriteBatch batch, float delta) {
		// Pan camera
		panKeyboard();

		batch.setProjectionMatrix(panner.getCamera().combined);
		batch.begin();
		renderTiles(batch, delta);
		renderHover(batch, delta);
		entityLayer.render(batch, delta);
		renderTooltip(batch);
		batch.end();

		panner.render(delta);

		// Draw debug stuff (gridlines?)
	}

	private void renderTiles(SpriteBatch batch, float delta) {
		// TODO refactor into a TileLayer class
		for(int i = 0; i < gameModel.getWidth(); i++) {
			for(int j = 0; j < gameModel.getHeight(); j++) {
				Image image = tileImageCache.get(tileImageTypes[i][j]);
				if(image != null) {
					image.setPosition(i * GameView.TILE_WIDTH, j * GameView.TILE_HEIGHT);
					image.draw(batch, 1f);
				}
			}
		}
	}

	private void renderHover(SpriteBatch batch, float delta) {
		// TODO refactor into a TileLayer class
		if(coordinates[HOVER].isTileCoordinateValid()) {
			Image image;
			Tile tile = gameModel.getTile(coordinates[HOVER].getX(), coordinates[HOVER].getY());
			int x = tile.x;
			int y = tile.y;
			image = tileImageCache.get(tileImageTypes[x][y]);
			if(image != null) {
				image.setPosition(x * GameView.TILE_WIDTH, y * GameView.TILE_HEIGHT);
				image.setColor(hoverColor);
				image.draw(batch, 1f);
				image.setColor(plainColor);
			}
		}
	}

	private void renderTooltip(SpriteBatch batch) {
		// TODO refactor into a TooltipLayer class
		BitmapFont font = new BitmapFont();
		font.setColor(Color.DARK_GRAY);
		final float tooltipOffset = 10;
		final float lineHeight = 16;
		if (tooltipEntity != null) {
			if (tooltipEntity instanceof Building) {
				Building building = (Building) tooltipEntity;
				
				String name = building.getName() + " ID: " + building.id;
				String description = building.getDescription();
				// TODO inventory
				
				// tooltip is above the mouse, so lines are reversed.
				font.draw(batch, name, tooltip.x + tooltipOffset, tooltip.y + tooltipOffset + lineHeight);
				font.draw(batch, description, tooltip.x + tooltipOffset, tooltip.y + tooltipOffset);
			} else if (tooltipEntity instanceof NPC) {
				NPC npc = (NPC) tooltipEntity;
				Objective objective = npc.getCurrentObjective();
				Direction dir = npc.getMoveDirection();
				
				String name = npc.getName();
				String description = npc.getDescription();
				String target = (objective == null) ? "No target" : "Target: " + objective.getTarget().getName();
				String direction = (dir == null) ? "" : "Going " + dir.name();
				// TODO inventory
				
				font.draw(batch, name, tooltip.x + tooltipOffset, tooltip.y + tooltipOffset + lineHeight * 3);
				font.draw(batch, description, tooltip.x + tooltipOffset, tooltip.y + tooltipOffset + lineHeight * 2);
				font.draw(batch, target, tooltip.x + tooltipOffset, tooltip.y + tooltipOffset + lineHeight);
				font.draw(batch, direction, tooltip.x + tooltipOffset, tooltip.y + tooltipOffset);
			}
		}
	}

	public void resize(int width, int height) {
		panner.resize(width, height);

		// width & height are already scaled.
		viewCamera.viewportWidth = width;
		viewCamera.viewportHeight = height;
		viewCamera.position.set(0, 0, 0);
		viewCamera.update();
	}
}
