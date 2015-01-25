package com.left.addd.view;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.left.addd.model.Entity;
import com.left.addd.model.StateChangedListener;
import com.left.addd.model.Tile;
import com.left.addd.model.Time;
import com.left.addd.utils.Res;

public class EntitySprite implements TileRenderable, StateChangedListener {

	private final TextureAtlas atlas;
	private final Entity entity;
	
	private static final Color plainColor = new Color(1, 1, 1, 1);
	private static final Color highlightColor = new Color(0.7f, 1, 0.7f, 1);

	// private final int FRAMES = 1;
	private float time;

	// (x,y) relative to tiles
	private int startX;
	private int startY;
	private float currentX;
	private float currentY;
	private int endX;
	private int endY;
	private boolean isMoving;
	
	// private TextureRegion[] playerFrames;
	// private Animation idleAnimation;
	private TextureRegion currentFrame;
	private Image currentImage;

	public EntitySprite(TextureAtlas atlas, Entity entity) {
		this.atlas = atlas;
		this.entity = entity;
		entity.addStateChangedListener(this);
	}

	@Override
	public void create() {
		time = 0f;
		currentFrame = atlas.findRegion(Res.ENTITIES + "main");
		currentImage = new Image(new TextureRegionDrawable(currentFrame));
	}

	@Override
	public Image getImageForRender(float delta) {
		if (isMoving) {
			time += delta;
			final float progress = time / Time.getRealTimeFromTicks(entity.getMoveDuration());
			if (progress > 1f) {
				// something's wrong
				log("progress=" + progress);
			}
			currentX = Interpolation.linear.apply(startX, endX, progress);
			currentY = Interpolation.linear.apply(startY, endY, progress);
		}
		
		currentImage.setPosition(currentX * Res.ENTITY_LENGTH, currentY * Res.ENTITY_LENGTH);
		return currentImage;
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
	
	public Entity getEntity() {
		return entity;
	}
	
	public void select() {
		currentImage.setColor(highlightColor);
	}
	
	public void deselect() {
		currentImage.setColor(plainColor);
	}

	@Override
	public void OnStateChanged() {
		Tile current = entity.getCurrentTile();
		Tile next = entity.getNextTile();
		if (current.equals(next)) {
			// entity is no longer moving
			log("no longer moving" + pCoords(current));
			time = 0;
			startX = current.x;
			startY = current.y;
			currentX = current.x;
			currentY = current.y;
			endX = current.x;
			endY = current.y;
			isMoving = false;
		} else {
			// entity is now moving
			log("now moving" + pCoords(current));
			time = 0;
			startX = current.x;
			startY = current.y;
			currentX = current.x;
			currentY = current.y;
			endX = next.x;
			endY = next.y;
			isMoving = true;
		}
	}
}
