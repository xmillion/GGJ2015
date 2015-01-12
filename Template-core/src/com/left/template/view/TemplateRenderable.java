package com.left.template.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Represents all objects that can be rendered on the screen as Sprites.
 */
public interface TemplateRenderable {
	public void render(SpriteBatch batch, float delta);
}
