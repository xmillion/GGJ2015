package com.left.addd.view;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.left.addd.AdddGame;

public class PannerMobile extends PannerAbstract {
	private static final float TWITCH_TOLERANCE = 5f / AdddGame.getUIScaling();
	
	private Vector2 initPos;
	private Vector2 prevPos;
	private Vector2 delta;
	private boolean isTwitching;
	private boolean isDragging;
	private boolean isMultiTouching;
	
	public PannerMobile(TextureAtlas atlas, Vector3 minBound, Vector3 maxBound) {
		super(atlas, minBound, maxBound);
		initPos = new Vector2();
		prevPos = new Vector2();
		delta = new Vector2();
	}
	
	// Input controls
	
	/**
	 * On mobiles, just touching does nothing.
	 * @param screenX Touch X coordinate
	 * @param screenY Touch Y coordinate
	 * @param pointer Which finger is touching
	 * @param button Irrelevant on mobiles (always Buttons.LEFT)
	 */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(pointer == 0) {
			initPos.x = screenX;
			initPos.y = viewCamera.viewportHeight - screenY;
			prevPos.x = initPos.x;
			prevPos.y = initPos.y;
		} else {
			isMultiTouching = true;
		}
		isTwitching = true;
		isDragging = false;
		return false;
	}

	/**
	 * On mobiles, just touching does nothing.
	 * @param screenX Mouse X coordinate
	 * @param screenY Mouse Y coordinate
	 * @param pointer Which finger is touching
	 * @param button Irrelevant on mobiles (always Buttons.LEFT)
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		isMultiTouching = false;
		return isDragging;
	}

	/**
	 * On mobiles, dragging will move the viewport by the same distance as the distance dragged.
	 * @param screenX Mouse X coordinate
	 * @param screenY Mouse Y coordinate
	 * @param pointer Which finger is touching
	 * @param button Irrelevant on mobiles (always Buttons.LEFT)
	 */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer, int button) {
		// Using first touch only.
		if(pointer != 0 || isMultiTouching) return false;
		// Ignores minor dragging
		if(isTwitching) {
			if(initPos.dst(screenX, viewCamera.viewportHeight - screenY) > TWITCH_TOLERANCE) {
				isTwitching = false;
			} else {
				return false;
			}
		}
		
		delta.x = prevPos.x - screenX;
		delta.y = prevPos.y + screenY - viewCamera.viewportHeight;
		delta.scl(AdddGame.getUIScaling());
		prevPos.x = screenX;
		prevPos.y = viewCamera.viewportHeight - screenY;
		
		pan(delta);
		isDragging = true;
		return true;
	}
}
