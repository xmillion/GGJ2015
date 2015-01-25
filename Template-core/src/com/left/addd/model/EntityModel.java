package com.left.addd.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class EntityModel {
	
	private final long mId;
	private final Entity mEntity;
	private final String mName;
	
	/**
	 * Metadata of the entity (used to store strings, sprite dimension info etc)
	 */
	private final HashMap<String,Object> mMetadata;
	
	/*
	 * Current state of the entity and its actions
	 */
	private EntityState mCurrentState;
	
	private HashMap<EntityModel,EntityModel> mObjectives;
	
	private EntityModel mTargetEntity;
	
	/**
	 * 
	 * @param id numerical id of the entity, this must be unique
	 * @param name name of the entity
	 * @param data stored metadata of the entity, it is a map of key value string pairs
	 */
	public EntityModel(long id, Entity entity, String name, HashMap<String,Object> metadata) {
		mId = id;
		mEntity = entity;
		mName = name;
		mMetadata = metadata;
		mObjectives = new HashMap<EntityModel,EntityModel>();
		mTargetEntity = null;
	}
	
	public long getId() {
		return mId;
	}
	
	public Entity getEntity() {
		return mEntity;
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
	
	public EntityModel getTargetEntity() {
		return mTargetEntity;
	}
	
	public void setTargetEntity(EntityModel targetEntity) {
		mTargetEntity = targetEntity;
	}
	
	public HashMap<EntityModel,EntityModel> getObjectives() {
		return mObjectives;
	}
	
	/**
	 * Private class to keep track of the states of the entity
	 * @author kev
	 *
	 */
	public class EntityState {
		
		private int mX;
		private int mY;
		private Tile mNextTile;
		
		public EntityState() {
			mX = 0;
			mY = 0;
		}
		
		public EntityState(int x, int y) {
			mX = x;
			mY = y;
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
		
		public void setNextTile(Tile next) {
			mNextTile = next;
		}
		
		public Tile getNextTile() {
			return mNextTile;
		}
	}
	
}
