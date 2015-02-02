package com.left.addd.model;

import java.util.ArrayList;
import java.util.Set;

public class EntityManager {
	
	private ArrayList<Entity> entities;
	
	public EntityManager() {
		entities = new ArrayList<Entity>();
	}
	
	public void addEntity(Entity em) {
		entities.add(em);
	}
	
	public void freeEntity(Entity em) {
		entities.remove(em);
	}
	
	public ArrayList<Entity> getEntities() {
		return entities;
	}
	
	public void checkObjectivesAndUpdateTargets() {
		for (Entity em : entities) {
			Set<Entity> objectives = em.getObjectives().keySet();
			for(Entity o : objectives) {
				if (checkAdjacency(em,o)) {
					em.setTargetEntity(em.getObjectives().get(o));
					break;
				}
			}
		}
	}
	
	private boolean checkAdjacency(Entity e1, Entity e2) {
		int e1_x = e1.getCurrentTile().x;
		int e1_y = e1.getCurrentTile().y;
		
		int e2_x = e2.getCurrentTile().x;
		int e2_y = e2.getCurrentTile().y;
		
		if ((e1_x + 1 == e2_x || e1_x - 1 == e2_x || e1_x == e2_x) &&
			(e1_y + 1 == e2_y || e1_y - 1 == e2_y || e1_y == e2_y)) {
			return true;
		}
		return false;
	}
}
