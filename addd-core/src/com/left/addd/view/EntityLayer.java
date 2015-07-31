package com.left.addd.view;

import static com.left.addd.utils.Log.log;
import static com.left.addd.utils.Log.pCoords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.left.addd.model.Building;
import com.left.addd.model.EntityManager;
import com.left.addd.model.NPC;
import com.left.addd.model.Entity;
import com.left.addd.model.Tile;
import com.left.addd.model.TileManager;
import com.left.addd.utils.Res;

public class EntityLayer {

	private static final Color plainColor = new Color(1, 1, 1, 1);
	private static final Color hoverColor = new Color(0.6f, 0.6f, 1f, 1);
	private static final Color selectColor = new Color(0.7f, 1, 0.7f, 1);
	
	private final TileManager tileManager;
	private final EntityManager entityManager;
	private Entity hoveredEntity = null;
	private Entity selectedEntity = null;
	
	private Map<String, Image> entityImageCache;
	
	public EntityLayer(TextureAtlas atlas, TileManager tileManager, EntityManager entityManager) {
		this.tileManager = tileManager;
		this.entityManager = entityManager;
		
		entityImageCache = new HashMap<String, Image>();
		for(Building.Type type: Building.Type.values()) {
			Image image = new Image(new TextureRegionDrawable(atlas.findRegion(Res.ENTITIES + type.assetName)));
			entityImageCache.put(type.assetName, image);
		}
		for(NPC.Type type: NPC.Type.values()) {
			Image image = new Image(new TextureRegionDrawable(atlas.findRegion(Res.ENTITIES + type.assetName)));
			entityImageCache.put(type.assetName, image);
		}
	}
	
	private void renderEntity(SpriteBatch batch, float delta, Entity entity, Color color) {
		float tileX;
		float tileY;
		if (entity instanceof NPC) {
			NPC npc = (NPC) entity;
			npc.render(delta);
			tileX = npc.getTileCoordinate().x * Res.ENTITY_WIDTH;
			tileY = npc.getTileCoordinate().y * Res.ENTITY_HEIGHT;
		} else {
			Tile tile = entity.getTile();
			tileX = tile.x * Res.ENTITY_WIDTH;
			tileY = tile.y * Res.ENTITY_HEIGHT;
		}
		Image image = entityImageCache.get(entity.getAssetName());
		image.setPosition(tileX, tileY);
		image.setColor(color);
		image.draw(batch, 1f);
	}
	
	public void render(SpriteBatch batch, float delta) {
		for (Entity entity: entityManager.getEntities()) {
			renderEntity(batch, delta, entity, plainColor);
		}
		
		// redraw special entities
		if (hoveredEntity != null) {
			renderEntity(batch, 0, hoveredEntity, hoverColor);
		}
		
		if (selectedEntity != null) {
			renderEntity(batch, 0, selectedEntity, selectColor);
		}
	}

	public Entity findEntityInTarget(Vector2 tileCoordinates, int tileX, int tileY) {
		Entity foundEntity = null;
		
		// Find all entities involved with the target tile
		Tile targetTile = tileManager.getTile(tileX, tileY);
		List<NPC> candidateEntities = new ArrayList<NPC>();
		for (Entity entity: entityManager.getEntities()) {
			if (entity instanceof Building && entity.getTile().equals(targetTile)) {
				// Buildings don't move, so it must be completely covering the target tile. This is the target.
				return entity;
			} else if (entity instanceof NPC) {
				NPC npc = (NPC) entity;
				if (targetTile.equals(npc.getTile()) || targetTile.equals(npc.getNextTile())) {
					candidateEntities.add(npc);
				}
			}
		}
		
		// Do hit-testing with the candidate entities
		for (NPC npc: candidateEntities) {
			Rectangle rect = new Rectangle(npc.getTileCoordinate().x, npc.getTileCoordinate().y, Res.ENTITY_WIDTH, Res.ENTITY_HEIGHT);
			if (rect.contains(tileCoordinates)) {
				return npc;
			}
		}
		return foundEntity;
	}
	
	public void hoverEntity(Entity entity) {
		hoveredEntity = entity;
	}
	
	public void selectEntity(Entity entity) {
		selectedEntity = entity;
	}
	
	public void deselectAllEntities() {
		hoveredEntity = null;
		selectedEntity = null;
	}
}