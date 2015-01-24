package com.left.addd.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents all objects that can be rendered on the screen as Sprites.
 */
public interface GameRenderable {
	/**
	 * Allow the renderable object to generate its graphics.
	 */
	public void create();
	
	/**
	 * Returns the TextureRegion that will be rendered on the screen.
	 * @return
	 */
	public TextureRegion getTextureForRender(float delta);
	
	/**
	 * Returns the width this renderable object will take up
	 * @return
	 */
	public float getWidth();
	
	/**
	 * Returns the height this renderable object will take up
	 * @return
	 */
	public float getHeight();
}
