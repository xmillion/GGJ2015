package com.left.addd.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.left.addd.utils.Utils;

/**
 * Handles the camera logic for GridView.
 * Includes the graphics that appear during scrolling.
 */
public abstract class PannerAbstract implements Panner {

	public static final float DEFAULT_PAN = 3f;
	protected static final float PAN_SCALE = 0.05f;
	protected final Vector3 MIN_BOUND;
	protected final Vector3 MAX_BOUND;

	// GridView's camera
	protected OrthographicCamera viewCamera;
	private BoundingBox bounds;

	public PannerAbstract(TextureAtlas atlas, Vector3 minBound, Vector3 maxBound) {
		MIN_BOUND = minBound;
		MAX_BOUND = maxBound;
		viewCamera = new OrthographicCamera();
	}

	// Panning manually

	/**
	 * Translates the camera in the X and Y dimensions. Does bounds checking.
	 * 
	 * @param delta Amount to move camera by.
	 */
	@Override
	public void pan(Vector2 delta) {
		// keep camera within borders
		float x = Utils.between(viewCamera.position.x + delta.x, bounds.min.x, bounds.max.x);
		float y = Utils.between(viewCamera.position.y + delta.y, bounds.min.y, bounds.max.y);
		viewCamera.position.set(x, y, 0);
		viewCamera.update();
	}

	/**
	 * Translates the camera in all 3 dimensions. Does bounds checking.
	 * 
	 * @param delta Amount to move camera by.
	 */
	@Override
	public void pan(Vector3 delta) {
		// keep camera within borders
		float x = Utils.between(viewCamera.position.x + delta.x, bounds.min.x, bounds.max.x);
		float y = Utils.between(viewCamera.position.y + delta.y, bounds.min.y, bounds.max.y);
		float z = Utils.between(viewCamera.position.z + delta.z, bounds.min.z, bounds.max.z);
		viewCamera.position.set(x, y, z);
		viewCamera.update();
	}

	/**
	 * Translates the camera in all 3 dimensions. Does bounds checking.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public void pan(float x, float y, float z) {
		// keep camera within borders
		x = Utils.between(viewCamera.position.x + x, bounds.min.x, bounds.max.x);
		y = Utils.between(viewCamera.position.y + y, bounds.min.y, bounds.max.y);
		z = Utils.between(viewCamera.position.z + z, bounds.min.z, bounds.max.z);
		viewCamera.position.set(x, y, z);
		viewCamera.update();
	}

	// Input controls
	
	public abstract boolean touchDown(int screenX, int screenY, int pointer, int button);
	public abstract boolean touchUp(int screenX, int screenY, int pointer, int button);
	public abstract boolean touchDragged(int screenX, int screenY, int pointer, int button);

	// Camera functions

	@Override
	public OrthographicCamera getCamera() {
		return viewCamera;
	}

	@Override
	public void project(Vector3 vector) {
		viewCamera.project(vector);
	}

	@Override
	public void unproject(Vector3 vector) {
		viewCamera.unproject(vector);
	}

	// Rendering

	/**
	 * Override if there are graphics that need to be rendered.
	 * Panner should use its own SpriteBatch.
	 */
	@Override
	public void render(float delta) {
	}

	@Override
	public void resize(int width, int height) {
		// width & height are already scaled.
		viewCamera.viewportWidth = width;
		viewCamera.viewportHeight = height;
		viewCamera.position.set(0, 0, 0);
		viewCamera.update();

		Vector3 min = MIN_BOUND.cpy();
		Vector3 max = new Vector3(MAX_BOUND.x - width, MAX_BOUND.y - height, 0);
		viewCamera.project(min, 0, 0, width, height);
		viewCamera.project(max, 0, 0, width, height);
		bounds = new BoundingBox(min, max);
		// do a pan to reset camera position
		pan(min);
	}
}
