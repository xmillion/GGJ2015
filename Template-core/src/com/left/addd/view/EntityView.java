package com.left.addd.view;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.left.addd.model.Building;
import com.left.addd.model.NPC;
import com.left.addd.model.Entity;
import com.left.addd.model.StateChangedListener;
import com.left.addd.model.Tile;
import com.left.addd.model.Time;
import com.left.addd.utils.Res;

public class EntityView implements StateChangedListener<Entity> {

	private final TextureAtlas atlas;
	
	private static final Color plainColor = new Color(1, 1, 1, 1);
	private static final Color highlightColor = new Color(0.7f, 1, 0.7f, 1);
	
	private Map<Entity, EntityRenderer> entityMap;
	
	public EntityView(TextureAtlas atlas) {
		this.atlas = atlas;
		entityMap = new HashMap<Entity, EntityRenderer>();
	}
	
	public void render(SpriteBatch batch, float delta) {
		for (Entity key: entityMap.keySet()) {
			EntityRenderer e = entityMap.get(key);
			e.getImageForRender(delta).draw(batch, 1f);
		}
	}
	
	public Entity selectEntityInTarget(float targetX, float targetY) {
		Entity selectedEntity = null;
		boolean targetFound = false;
		for (Entity key: entityMap.keySet()) {
			EntityRenderer e = entityMap.get(key);
			if (!targetFound && e.isInRect(targetX, targetY)) {
				e.setColor(highlightColor);
				selectedEntity = key;
				targetFound = true;
			} else {
				e.setColor(plainColor);
			}
		}
		return selectedEntity;
	}
	
	public void deselectAllEntities() {
		for (Entity key: entityMap.keySet()) {
			EntityRenderer e = entityMap.get(key);
			e.setColor(plainColor);
		}
	}
	
	@Override
	public void OnStateChanged(Entity entity) {
		// TODO Auto-generated method stub
		if (entity instanceof NPC) {
			// check if it's starting to move or done moving
			NPC cEntity = (NPC) entity;
			Tile current = cEntity.getCurrentTile();
			Tile next = cEntity.getNextTile();
			if (current.equals(next)) {
				// entity is no longer moving
				log("Entity " + entity.getMetadata().get("Name") + " no longer moving" + pCoords(current));
				if (entityMap.containsKey(entity)) {
					EntityRenderer e = entityMap.get(entity);
					e.end(current.x, current.y);
				} else {
					Image image = new Image(new TextureRegionDrawable(atlas.findRegion(Res.ENTITIES + cEntity.getType().assetName)));
					EntityRenderer e = new NPCRenderer(image);
					e.end(current.x, current.y);
					entityMap.put(entity, e);
				}
			} else {
				// entity is now moving
				log("Entity " + entity.getMetadata().get("Name") + " now moving" + pCoords(current));
				if (entityMap.containsKey(entity)) {
					EntityRenderer e = entityMap.get(entity);
					e.start(current.x, current.y, next.x, next.y, Time.getRealTimeFromTicks(cEntity.getMoveDuration()));
				} else {
					Image image = new Image(new TextureRegionDrawable(atlas.findRegion(Res.ENTITIES + cEntity.getType().assetName)));
					EntityRenderer e = new NPCRenderer(image);
					e.start(current.x, current.y, next.x, next.y, Time.getRealTimeFromTicks(cEntity.getMoveDuration()));
					entityMap.put(entity, e);
				}
			}
		} else if (entity instanceof Building) {
			log ("Building " + entity.getMetadata().get("Name") + " at " + pCoords(entity.getCurrentTile()));
			Building bEntity = (Building) entity;
			Tile current = bEntity.getCurrentTile();
			Image image = new Image(new TextureRegionDrawable(atlas.findRegion(Res.ENTITIES + bEntity.getType().assetName)));
			BuildingRenderer br = new BuildingRenderer(current.x, current.y, image);
			entityMap.put(entity, br);
		}
	}
	
	private abstract class EntityRenderer implements TileRenderable {
		protected Vector2 current;
		protected Image image;
		
		public EntityRenderer(Image image) {
			this.current = new Vector2();
			this.image = image;
		}
		
		public abstract void start(float startX, float startY, float endX, float endY, float duration);
		
		public abstract void end(float endX, float endY);
		
		public void setColor(Color c) {
			image.setColor(c);
		}
		
		public boolean isInRect(float targetX, float targetY) {
			return (targetX >= current.x && targetY >= current.y && targetX < current.x + image.getImageWidth() / Res.TILE_LENGTH && targetY < current.y + image.getImageHeight() / Res.TILE_LENGTH);
		}

		@Override
		public void create() {
			// why is this part of the interface
		}

		@Override
		public abstract Image getImageForRender(float delta);

		@Override
		public float getX() {
			return current.x;
		}

		@Override
		public float getY() {
			return current.y;
		}

		@Override
		public float getWidth() {
			return image.getImageWidth();
		}

		@Override
		public float getHeight() {
			return image.getImageHeight();
		}
	}
	
	private class NPCRenderer extends EntityRenderer {
		private final Vector2 start;
		private final Vector2 end;
		private float time;
		private float duration;
		private boolean isMoving;
		
		public NPCRenderer(Image image) {
			super(image);
			this.start = new Vector2();
			this.end = new Vector2();
			this.isMoving = false;
		}
		
		public void start(float startX, float startY, float endX, float endY, float duration) {
			this.start.set(startX, startY);
			this.end.set(endX, endY);
			this.current.set(startX, startY);
			time = 0;
			this.duration = duration;
			isMoving = true;
		}
		
		public void end(float endX, float endY) {
			this.start.set(endX, endY);
			this.end.set(endX, endY);
			this.current.set(endX, endY);
			isMoving = false;
		}

		@Override
		public Image getImageForRender(float delta) {
			if (isMoving) {
				time += delta;
				float progress = time / duration;
				if (progress > 1) {
					progress = 1;
				}
				current.set(Interpolation.linear.apply(start.x, end.x, progress), Interpolation.linear.apply(start.y, end.y, progress));
			}
			image.setPosition(current.x * Res.ENTITY_LENGTH, current.y * Res.ENTITY_LENGTH);
			return image;
		}
	}
	
	private class BuildingRenderer extends EntityRenderer {
		public BuildingRenderer(float x, float y, Image image) {
			super(image);
			start(x, y, x, y, 0);
		}

		@Override
		public Image getImageForRender(float delta) {
			return image;
		}

		@Override
		public void start(float startX, float startY, float endX, float endY, float duration) {
			current.set(startX, startY);
			image.setPosition(current.x * Res.ENTITY_LENGTH, current.y * Res.ENTITY_LENGTH);
		}

		@Override
		public void end(float endX, float endY) {
			//current.set(endX, endY);
		}
	}
}