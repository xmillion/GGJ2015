package com.left.addd.services;

import java.util.ArrayList;
import java.util.Set;

import com.left.addd.model.EntityModel;
import com.left.addd.model.EntityModel.EntityState;

public class EntityManager {
	
	private static ArrayList<EntityModel> entityPool;
	private static EntityManager manager;
	
	public EntityManager() {
		entityPool = new ArrayList<EntityModel>();
	}
	
	public static EntityManager getInstance() {
		if (manager == null) {
			manager = new EntityManager();
		}
		return manager;
	}
	
	public static void addEntity(EntityModel em) {
		entityPool.add(em);
	}
	
	public static void freeEntity(EntityModel em) {
		entityPool.remove(em);
	}
	
	public static void checkObjectivesAndUpdateTargets() {
		for (EntityModel em : entityPool) {
			Set<EntityModel> objectives = em.getObjectives().keySet();
			for(EntityModel o : objectives) {
				if (checkAdjacency(em,o)) {
					em.setTargetEntity(o);
					break;
				}
			}
		}
	}
	
	private static boolean checkAdjacency(EntityModel e1, EntityModel e2) {
		int e1_x = e1.getCurrentState().getX();
		int e1_y = e1.getCurrentState().getY();
		
		int e2_x = e2.getCurrentState().getX();
		int e2_y = e2.getCurrentState().getY();
		
		if ((e1_x + 1 == e2_x || e1_x - 1 == e2_x || e1_x == e2_x) &&
			(e1_y + 1 == e2_y || e1_y - 1 == e2_y || e1_y == e2_y)) {
			return true;
		}
		return false;
	}
	
	

}
