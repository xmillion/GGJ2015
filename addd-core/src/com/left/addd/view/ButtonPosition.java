package com.left.addd.view;

import com.badlogic.gdx.math.Vector2;

public class ButtonPosition {

	private final int width;
	private final int height;
	private Vector2 screenCoordinates;
	private Vector2 tileCoordinates;
	private int tileX;
	private int tileY;
	private boolean isTileValid;
	
	public ButtonPosition(int width, int height) {
		this(width, height, 0,0,0,0);
	}
	
	public ButtonPosition(int width, int height, float screenX, float screenY, float tileX, float tileY) {
		this.width = width;
		this.height = height;
		this.screenCoordinates = new Vector2(screenX, screenY);
		this.tileCoordinates = new Vector2(tileX, tileY);
		this.tileX = (int) tileX;
		this.tileY = (int) tileY;
		this.isTileValid = (0 <= tileX && tileX < width && 0 <= tileY && tileY < height);
	}
	
	public Vector2 getScreenCoordinates() {
		return screenCoordinates;
	}
	
	public float getScreenX() {
		return screenCoordinates.x;
	}
	
	public float getScreenY() {
		return screenCoordinates.y;
	}
	
	public Vector2 getTileCoordinates() {
		return tileCoordinates;
	}

	public float getTileX() {
		return tileCoordinates.x;
	}
	
	public float getTileY() {
		return tileCoordinates.y;
	}
	
	public int getX() {
		return tileX;
	}
	
	public int getY() {
		return tileY;
	}
	
	public boolean isTileCoordinateValid() {
		return isTileValid;
	}
	
	public void update(float screenX, float screenY, float tileX, float tileY) {
		this.screenCoordinates.set(screenX, screenY);
		this.tileCoordinates.set(tileX, tileY);
		this.tileX = (int) tileX;
		this.tileY = (int) tileY;
		this.isTileValid = (0 <= tileX && tileX < width && 0 <= tileY && tileY < height);
	}
}
