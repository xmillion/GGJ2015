package com.left.addd.model;

public interface StateChangedListener<T> {

	/**
	 * When the model's state changes, this should get called.
	 */
	public void OnStateChanged(T changedObject);
}
