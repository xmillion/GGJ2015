package com.left.addd.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Any class that controls the game view camera must implement these methods.
 */
public interface Panner {
	// Direct panning
	public void pan(Vector2 delta);
	public void pan(Vector3 delta);
	public void pan(float x, float y, float z);
	// Input controls (touch event handling only)
	public boolean touchDown(int screenX, int screenY, int pointer, int button);
	public boolean touchUp(int screenX, int screenY, int pointer, int button);
	public boolean touchDragged(int screenX, int screenY, int pointer, int button);
	// Camera functions
	public OrthographicCamera getCamera();
	public void project(Vector3 vector);
	public void unproject(Vector3 vector);
	// Rendering
	public void render(float delta);
	public void resize(int width, int height);
}
