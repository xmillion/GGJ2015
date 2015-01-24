package com.left.addd.view;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.left.addd.model.Entity;
import com.left.addd.model.Tile;
import com.left.addd.model.Time;
import com.left.addd.utils.Res;

public class EntitySprite implements TileRenderable {

	private final TextureAtlas atlas;
	private final Entity entity;

	// private final int FRAMES = 1;
	private float time;

	// (x,y) relative to tiles
	private int startX;
	private int startY;
	private float currentX;
	private float currentY;
	private float endX;
	private float endY;
	private boolean isMoving;
	
	// private TextureRegion[] playerFrames;
	// private Animation idleAnimation;
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
		if (entity.triggerMoveCompleted()) {
			OnMoveCompleted();
		}
		if (entity.triggerMoveStarted()) {
			OnMoveStarted();
		}
		
		
		if (isMoving) {
			time += delta;
			final float progress = time / Time.getRealTimeFromTicks(entity.getMoveDuration());
			if (progress > 1f) {
				OnMoveCompleted();
			}
			currentX = Interpolation.linear.apply(startX, endX, progress);
			currentY = Interpolation.linear.apply(startY, endY, progress);
		}
		
		currentImage.setPosition(currentX * Res.ENTITY_LENGTH, currentY * Res.ENTITY_LENGTH);
		return currentImage;
	}
	
	private void OnMoveStarted() {
		log("move started");
		time = 0;
		Tile current = entity.getCurrentTile();
		Tile next = entity.getNextTile();
		startX = current.x;
		startY = current.y;
		currentX = current.x;
		currentY = current.y;
		endX = next.x;
		endY = next.y;
		isMoving = true;
	}
	
	private void OnMoveCompleted() {
		log("Move completed");
		time = 0;
		Tile current = entity.getCurrentTile();
		startX = current.x;
		startY = current.y;
		currentX = current.x;
		currentY = current.y;
		endX = current.x;
		endY = current.y;
		isMoving = false;
	}

	@Override
	public float getX() {
		return currentX;
	}

	@Override
	public float getY() {
		return currentY;
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
