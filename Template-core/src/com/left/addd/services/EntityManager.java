package com.left.addd.services;

import java.util.ArrayList;
import java.util.Set;

import com.left.addd.model.Entity;

public class EntityManager {
	
	private static ArrayList<Entity> entityPool;
	private static EntityManager manager;
	
	public EntityManager() {
		entityPool = new ArrayList<Entity>();
	}
	
	public static EntityManager getInstance() {
		if (manager == null) {
			manager = new EntityManager();
		}
		return manager;
	}
	
	public void addEntity(Entity em) {
		entityPool.add(em);
	}
	
	public void freeEntity(Entity em) {
		entityPool.remove(em);
	}
	
	public ArrayList<Entity> getEntities() {
		return entityPool;
	}
	
	public void checkObjectivesAndUpdateTargets() {
		for (Entity em : entityPool) {
			Set<Entity> objectives = em.getObjectives().keySet();
			for(Entity o : objectives) {
				if (checkAdjacency(em,o)) {
					em.setTargetEntity(o);
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
