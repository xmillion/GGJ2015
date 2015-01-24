package com.left.addd.view;

import static com.left.addd.utils.Log.log;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.left.addd.model.Entity;
import com.left.addd.model.Tile;
import com.left.addd.utils.Res;

public class EntitySprite implements TileRenderable {

	private final TextureAtlas atlas;
	private final Entity entity;
	
	//private final int FRAMES = 1;
	private float time;
	private float x;
	private float y;
	//private TextureRegion[] playerFrames;
	//private Animation idleAnimation;
	private TextureRegion currentFrame;
	private Image currentImage;
	
	public EntitySprite(TextureAtlas atlas, Entity entity) {
		this.atlas = atlas;
		this.entity = entity;
	}

	@Override
	public void create() {
		time = 0f;
		currentFrame = atlas.findRegion(Res.ENTITIES + "main");
		currentImage = new Image(new TextureRegionDrawable(currentFrame));
	}

	@Override
	public Image getImageForRender(float delta) {
		time += delta;
		Tile t = entity.getCurrentTile();
		x = t.x;
		y = t.y;
		currentImage.setPosition(x * Res.ENTITY_LENGTH, y * Res.ENTITY_LENGTH);
		return currentImage;
	}
	
	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getWidth() {
		return currentFrame.getRegionWidth();
	}

	@Override
	public float getHeight() {
		return currentFrame.getRegionHeight();
	}
}
