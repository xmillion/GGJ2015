package com.left.addd.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * TemplateModel is the model for this game. It represents the logic behind this game.
 * It does not know anything about how the representation is drawn, or how the player interacts with it.
 * Reference: https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class GameModel {
	
	private Time time;

	public GameModel() {
		this(0);
	}
	
	public GameModel(long timeInHours) {
		this.time = new Time(timeInHours);
	}
	
	public Time getTime() {
		return time;
	}

	public void update(float delta) {
		time.update(delta);
	}

	public void save(Json json) {
		json.writeObjectStart();
		json.writeValue("time", time.getTime());
		json.writeObjectEnd();
	}

	public static GameModel load(Json json, JsonValue jsonData) {
		long timeInHours = jsonData.getLong("time");
		GameModel templateModel = new GameModel(timeInHours);

		return templateModel;
	}
}
