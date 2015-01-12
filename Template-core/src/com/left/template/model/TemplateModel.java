package com.left.template.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * TemplateModel is the model for this game. It represents the logic behind this game.
 * It does not know anything about how the representation is drawn, or how the player interacts with it.
 * Reference: https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class TemplateModel {
	
	private Time time;

	public TemplateModel() {
		this(0);
	}
	
	public TemplateModel(long timeInHours) {
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

	public static TemplateModel load(Json json, JsonValue jsonData) {
		long timeInHours = jsonData.getLong("time");
		TemplateModel templateModel = new TemplateModel(timeInHours);

		return templateModel;
	}
}
