package com.left.addd.view;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Represents all objects that can be rendered on the screen as Sprites.
 */
public interface TileRenderable {
	/**
	 * Allow the renderable object to generate its graphics.
	 */
	public void create();
	
	/**
	 * Returns the TextureRegion that will be rendered on the screen.
	 * This is equivalent to render().
	 * @return
	 */
	public Image getImageForRender(float delta);
	
	/**
	 * Returns the X coordinate relative to the grid.
	 * @return
	 */
	public float getX();
	
	/**
	 * Returns the Y coordinate relative to the grid.
	 * @return
	 */
	public float getY();
	
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
