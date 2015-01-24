package com.left.addd.view;

import static com.left.addd.utils.Log.log;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.left.addd.AdddGame;
import com.left.addd.model.GameModel;
import com.left.addd.services.SoundManager.SoundList;
import com.left.addd.view.Panner;
import com.left.addd.view.PannerDesktop;
import com.left.addd.view.PannerMobile;
import com.left.addd.model.Building;
import com.left.addd.model.Direction;
import com.left.addd.model.Tile;
import com.left.addd.view.GameView;
import com.left.addd.view.PannerAbstract;
import com.left.addd.view.TileImageType;

/**
 * Manages the drawing of the model to the screen and player controls. Reference:
 * https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class GameView implements InputProcessor {
	public static final int TILE_LENGTH = 32;

	private final AdddGame game;
	private final GameModel gameModel;
	protected OrthographicCamera viewCamera;

	// Camera data
	private Panner panner;

	// I/O data
	private int buttonTouched;
	private Vector3 touchPoint;
	/** currentTileX and currentTileY are guaranteed to be within gameModel bounds */
	private int currentTileX;
	private int currentTileY;
	/** hoverX and hoverY are guaranteed to be within gameModel bounds when isHovering == true */
	private boolean isHovering;
	private int hoverX;
	private int hoverY;
	private Color hoverColor;

	private static final Color queryColor = new Color(0.8f, 0.8f, 1, 1);
	private static final Color buildColor = new Color(0.8f, 1, 0.8f, 0.8f);
	private static final Color blockColor = new Color(1, 0.7f, 0.7f, 0.6f);
	private static final Color blankColor = new Color(1, 1, 1, 1);

	// Tile rendering data
	private EntitySprite testEntity;
	
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

		Vector3 pannerMin = new Vector3((-3) * TILE_LENGTH, (-3) * TILE_LENGTH, 0);
		Vector3 pannerMax = new Vector3((gameModel.width + 3) * TILE_LENGTH, (gameModel.height + 3)
				* TILE_LENGTH, 0);
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

		buttonTouched = Buttons.LEFT;
		touchPoint = new Vector3();
		isHovering = false;
		
		testEntity = new EntitySprite(atlas, gameModel.getTestEntity());
		
		// Load all the tile images into cache
		this.tileImageCache = new EnumMap<TileImageType, Image>(TileImageType.class);
		for(TileImageType type: TileImageType.values()) {
			AtlasRegion region = atlas.findRegion(type.getFileName());
			if(region != null) {
				Image image = new Image(region);
				tileImageCache.put(type, image);
			}
		}

		this.tileImageTypes = new TileImageType[gameModel.width][gameModel.height];
		updateAllTiles();
		create();
	}

	// ********************
	// **** Internals *****
	// ********************

	public GameModel getModel() {
		return gameModel;
	}

	/**
	 * Calibrates currentTileX and currentTileY's values.
	 * 
	 * @param screenX Screen X coordinate from bottom left
	 * @param screenY Screen Y coordinate from bottom left
	 * @return true if currentTileX and currentTileY have been adjusted.
	 */
	private boolean setCurrentTileFromScreen(float screenX, float screenY) {
		touchPoint.set(screenX, screenY, 0);
		panner.unproject(touchPoint);
		int x = (int) touchPoint.x / TILE_LENGTH;
		int y = (int) touchPoint.y / TILE_LENGTH;
		if(0 <= x && x < gameModel.width && 0 <= y && y < gameModel.height) {
			currentTileX = x;
			currentTileY = y;
			return true;
		}
		return false;
	}

	/**
	 * Calibrates hoverX and hoverY values.
	 * 
	 * @param screenX Screen X coordinate from bottom left
	 * @param screenY Screen Y coordinate from bottom left
	 * @return true if hoverX and hoverY have been adjusted.
	 */
	private boolean setHoverTileFromScreen(float screenX, float screenY) {
		touchPoint.set(screenX, screenY, 0);
		panner.unproject(touchPoint);
		int x = (int) touchPoint.x / TILE_LENGTH;
		int y = (int) touchPoint.y / TILE_LENGTH;
		if(0 <= x && x < gameModel.width && 0 <= y && y < gameModel.height) {
			hoverX = x;
			hoverY = y;
			return true;
		}
		return false;
	}

	// ********************
	// *** Camera Tools ***
	// ********************

	/**
	 * Checks for arrow keys being pressed, and pans accordingly. TODO Merge this into PannerDesktop?
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
		log("GameView", "KeyDown " + keycode);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		log("GameView", "KeyUp " + keycode);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		log("GameView", "KeyTyped " + character + " 0x" + Integer.toHexString(character));
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		buttonTouched = button;
		if(panner.touchDown(screenX, screenY, pointer, button)) {
			// Panning
			return true;
		}
		// TODO click hit detection. Do this on touchDown or touchUp?
		// 1. Find the intent of the user... selecting a character or a tile?
		// 2. Find the target (character, or the tile underneath)
		// 3. Call the appropriate view classes to handle the selection

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(panner.touchUp(screenX, screenY, pointer, button)) {
			// Panning
			return true;
		}

		if(button == Buttons.RIGHT) {
			// Deselect
			return true;
		} else if(setCurrentTileFromScreen(screenX, screenY)) {
			// Interact with tile
			touchTile();
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// Panning
		if(panner.touchDragged(screenX, screenY, pointer, buttonTouched)) {
			return true;
		}
		// TODO draggable functions with Tiles
		// return true if meaningful action is taken.
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		isHovering = setHoverTileFromScreen(screenX, screenY);
		if(isHovering) {
			hoverTile();
		}
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
	 * Tells the GameModel to perform an action at the given Tile coordinate.
	 */
	private void touchTile() {
		game.getSound().play(SoundList.CLICK);

		// Update all appropriate tiles
		updateTile(currentTileX, currentTileY);
	}

	/**
	 * Updates the graphics on the currently hovering tile.
	 */
	private void hoverTile() {
		hoverColor = queryColor;
	}

	/**
	 * Call this to update the Tile View for the given Tile coordinate. Note: only tile.x and tile.y are used.
	 * 
	 * @param tile Coordinate of Tile to update.
	 */
	private void updateTile(int x, int y) {
		Tile tile = gameModel.getTile(x, y);
		if(tile.hasNetwork()) {
			// Update tile and NESW neighbours
			setTileImageType(tile);
			setTileImageType(tile.getNeighbour(Direction.NORTH));
			setTileImageType(tile.getNeighbour(Direction.EAST));
			setTileImageType(tile.getNeighbour(Direction.SOUTH));
			setTileImageType(tile.getNeighbour(Direction.WEST));
		} else if(tile.hasBuilding()) {
			// Update entire building
			Building b = tile.getBuilding();
			for(int i = 0; i < b.getWidth(); i++) {
				for(int j = 0; j < b.getHeight(); j++) {
					setTileImageType(gameModel.getTile(b.getOriginX() + i, b.getOriginY() + j));
				}
			}
		} else {
			setTileImageType(tile);
		}
	}

	/**
	 * Update all the tile views.
	 */
	private void updateAllTiles() {
		for(int i = 0; i < gameModel.width; i++) {
			for(int j = 0; j < gameModel.height; j++) {
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
		tileImageTypes[tile.x][tile.y] = TileImageType.getImageFromTile(tile);
	}

	// ********************
	// **** Rendering *****
	// ********************
	
	public void create() {
		testEntity.create();
	}

	public void render(SpriteBatch batch, float delta) {
		// Pan camera
		panKeyboard();

		batch.setProjectionMatrix(panner.getCamera().combined);
		batch.begin();
		renderTiles(batch, delta);
		renderHover(batch, delta);
		renderEntities(batch, delta);
		batch.end();

		panner.render(delta);

		// Draw debug stuff (gridlines?)
	}

	private void renderTiles(SpriteBatch batch, float delta) {
		for(int i = 0; i < gameModel.width; i++) {
			for(int j = 0; j < gameModel.height; j++) {
				Image image = tileImageCache.get(tileImageTypes[i][j]);
				if(image != null) {
					image.setPosition(i * GameView.TILE_LENGTH, j * GameView.TILE_LENGTH);
					image.draw(batch, 1f);
				}
			}
		}
	}

	private void renderHover(SpriteBatch batch, float delta) {
		if(isHovering) {
			Image image;
			Tile tile = gameModel.getTile(hoverX, hoverY);
			int x, y;

			if(tile.hasBuilding()) {
				Building b = tile.getBuilding();
				x = b.getOriginX();
				y = b.getOriginY();
			} else {
				x = tile.x;
				y = tile.y;
			}

			image = tileImageCache.get(tileImageTypes[x][y]);
			if(image != null) {
				image.setPosition(x * GameView.TILE_LENGTH, y * GameView.TILE_LENGTH);
				image.setColor(hoverColor);
				image.draw(batch, 1f);
				image.setColor(blankColor);
			}
		}
	}
	
	private void renderEntities(SpriteBatch batch, float delta) {
		testEntity.getImageForRender(delta).draw(batch, 1f);
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
