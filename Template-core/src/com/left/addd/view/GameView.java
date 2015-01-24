package com.left.addd.view;

import static com.left.addd.utils.Log.log;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.left.addd.AdddGame;
import com.left.addd.model.GameModel;
import com.left.addd.services.SoundManager.SoundList;

/**
 * Manages the drawing of the Grid to the screen and player controls.
 * Reference: https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class GameView implements InputProcessor {
	private final AdddGame game;
	private final GameModel gridModel;
	protected OrthographicCamera viewCamera;

	// ********************
	// *** Constructor ****
	// ********************

	public GameView(AdddGame game, GameModel model, TextureAtlas atlas) {
		this.game = game;
		this.gridModel = model;
		this.viewCamera = new OrthographicCamera();
	}

	// ********************
	// **** Internals *****
	// ********************

	public GameModel getModel() {
		return gridModel;
	}

	// ********************
	// * Input Processing *
	// ********************

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		log("TemplateView", "KeyTyped " + character + " 0x" + Integer.toHexString(character));
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		game.getSound().play(SoundList.CLICK);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// return true if meaningful action is taken.
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	// ********************
	// **** Rendering *****
	// ********************
	private PlayerSprite player;
	private EnemySprite enemy;

	public void create() {
		player = new PlayerSprite();
		player.create();
		enemy = new EnemySprite();
		enemy.create();
	}

	public void render(SpriteBatch batch, float delta) {
		batch.setProjectionMatrix(viewCamera.combined);
		batch.begin();
		// *** RENDERING STUFF GOES HERE ***
		batch.draw(player.getTextureForRender(delta), -256, 0);
		batch.draw(enemy.getTextureForRender(delta), 128, 0);
		batch.end();
	}

	public void resize(int width, int height) {
		// width & height are already scaled.
		viewCamera.viewportWidth = width;
		viewCamera.viewportHeight = height;
		viewCamera.position.set(0, 0, 0);
		viewCamera.update();
	}
}
