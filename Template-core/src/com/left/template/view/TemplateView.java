package com.left.template.view;

import static com.left.template.utils.Log.log;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.left.template.TemplateGame;
import com.left.template.model.TemplateModel;
import com.left.template.utils.Res;

/**
 * Manages the drawing of the Grid to the screen and player controls.
 * Reference: https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class TemplateView implements InputProcessor, TemplateRenderable {

	public static final int TILE_LENGTH = 32;

	private final TemplateGame game;
	private final TemplateModel gridModel;
	
	protected OrthographicCamera viewCamera;

	// ********************
	// *** Constructor ****
	// ********************

	public TemplateView(TemplateGame game, TemplateModel model, TextureAtlas atlas) {
		this.game = game;
		this.gridModel = model;
		this.viewCamera = new OrthographicCamera();
	}

	// ********************
	// **** Internals *****
	// ********************

	public TemplateModel getModel() {
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
	
	private Texture texture = new Texture(Res.UI + "background.png");

	@Override
	public void render(SpriteBatch batch, float delta) {

		batch.setProjectionMatrix(viewCamera.combined);
		batch.begin();
		
		// *** RENDERING STUFF GOES HERE ***
		batch.draw(texture, 0, 0);
		batch.end();
	}

	public void resize(int width, int height) {
		// width & height are already scaled.
		viewCamera.viewportWidth = width;
		viewCamera.viewportHeight = height;
		viewCamera.position.set(0, 0, 0);
		viewCamera.update();

		//Vector3 min = MIN_BOUND.cpy();
		//Vector3 max = new Vector3(MAX_BOUND.x - width, MAX_BOUND.y - height, 0);
		//viewCamera.project(min, 0, 0, width, height);
		//viewCamera.project(max, 0, 0, width, height);
		//bounds = new BoundingBox(min, max);
		
	}
}
