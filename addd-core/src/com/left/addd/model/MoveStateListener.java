package com.left.addd.model;

/**
 * Interface to handle an NPC's move state.
 * @param <T> Class whose state we're listening to.
 */
public interface MoveStateListener<T> {
	
	/**
	 * Called when the object begins moving.
	 * @param changedObject Object that started moving.
	 */
	public void OnMoveStarted(T changedObject);
	
	/**
	 * Called when the object has finished moving.
	 * @param changedObject Object that finished moving.
	 */
	public void OnMoveCompleted(T changedObject);
}
