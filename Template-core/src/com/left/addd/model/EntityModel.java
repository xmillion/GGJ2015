package com.left.addd.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class EntityModel {
	
	private final long mId;
	private final String mName;
	
	/**
	 * Metadata of the entity (used to store strings, sprite dimension info etc)
	 */
	private final HashMap<String,Object> mMetadata;
	
	private HashMap<EntityState,EntityAction> mStateActionMap;
	
	/*
	 * Current state of the entity and its actions
	 */
	private EntityState mCurrentState;
	
	/**
	 * 
	 * @param id numerical id of the entity, this must be unique
	 * @param name name of the entity
	 * @param data stored metadata of the entity, it is a map of key value string pairs
	 */
	public EntityModel(long id, String name, HashMap<String,Object> metadata) {
		mId = id;
		mName = name;
		mMetadata = metadata;
	}
	
	public long getId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}
	
	public HashMap<String,Object> getMetadata() {
		return mMetadata;
	}
	
	public HashSet<String> getDataKeys() {
		return (HashSet<String>) mMetadata.keySet();
	}
	
	/**
	 * 
	 * @param key the key to query
	 * @return the matching value pair for the key, returns null if no such key exists
	 */
	public Object queryMetadata(String key) {
		if (mMetadata.containsKey(key)) {
			return mMetadata.get(key);
		}
		return null;
	}
	
	public void setCurrentState(EntityState es) {
		mCurrentState = es;
	}
	
	public EntityState getCurrentState() {
		return mCurrentState;
	}
	
	public void addToStateActionMap(EntityState es, EntityAction ea) {
		mStateActionMap.put(es,ea);
	}
	
	/**
	 * @return gets the next action based on input state (does not validate constraints)
	 */
	public EntityAction getNextAction(EntityState es) {
		return mStateActionMap.get(es);
	}
	
	/**
	 * Private class to keep track of the states of the entity
	 * @author kev
	 *
	 */
	public class EntityAction {

		private EntityState mTargetState;
		private int mSpeed;
				
		public EntityAction() {
			mTargetState = new EntityState();
			mSpeed = 0;
		}
		
		public EntityAction(EntityState es, int speed) {
			mTargetState = es;
			mSpeed = speed;
		}
		
		public EntityState getTargetState() {
			return mTargetState;
		}

		public void setTargetState(EntityState es) {
			mTargetState = es;
		}

		public int getSpeed() {
			return mSpeed;
		}

		public void setSpeed(int speed) {
			mSpeed = speed;
		}

	}
	
	/**
	 * Private class to keep track of the states of the entity
	 * @author kev
	 *
	 */
	public class EntityState {
		
		private int mX;
		private int mY;
		
		private EntitySatisfactionConstraints constraints;
		
		public EntityState() {
			mX = 0;
			mY = 0;
			constraints = new EntitySatisfactionConstraints();
		}
		
		public EntityState(int x, int y) {
			mX = x;
			mY = y;
			constraints = new EntitySatisfactionConstraints();
		}
		
		public void setX(int x) {
			mX = x;
		}
		
		public int getX() {
			return mX;
		}
		
		public void setY(int y) {
			mY = y;
		}
		
		public int getY() {
			return mY;
		}
		
		public EntitySatisfactionConstraints getConstraints() {
			return constraints;
		}
		
		public boolean isSatisfied() {
			return constraints.validateConstraints();
		}
		
		// If we plan to not only compare x,y's for state, we will need to change this
		// (and potentially serialize the state of its something fancy)
		public boolean equals(EntityState state) {
			if (mX == state.getX() &&
				mY == state.getY()) {
				return true;
			}
			return false;
		}
		
	}
	
	public class EntitySatisfactionConstraints {
		
		private HashMap<EntityModel,EntityState> modelStateMap;
		
		public EntitySatisfactionConstraints() {
			modelStateMap = new HashMap<EntityModel,EntityState>();
		}
		
		public void addConstraint(EntityModel em, EntityState es) {
			modelStateMap.put(em, es);
		}
		
		public boolean validateConstraints() {
			Set<EntityModel> keys = modelStateMap.keySet();
			for (EntityModel k : keys) {
				if(!modelStateMap.get(k).equals(k.getCurrentState()))
					return false;
			}
			return true;
		}
		
	}
	
}
