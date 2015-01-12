package com.left.template.services;

import static com.left.template.utils.Log.log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.template.TemplateGame;
import com.left.template.model.TemplateModel;
import com.left.template.utils.LoadingException;

public class TemplateSerializer {

	private final int numSlots = 3;
	private Json json;

	public TemplateSerializer() {
		this.json = new Json();
		json.setSerializer(TemplateModel.class, new Json.Serializer<TemplateModel>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void write(Json json, TemplateModel gridModel, Class knownType) {
				gridModel.save(json);
			}

			@SuppressWarnings("rawtypes")
			@Override
			public TemplateModel read(Json json, JsonValue jsonData, Class type) {
				return TemplateModel.load(json, jsonData);
			}
		});
	}
	
	private FileHandle getSaveFileHandle(int slot) {
		if(slot < 0 || slot >= numSlots) {
			throw new IllegalArgumentException("No such save slot");
		}
		
		return Gdx.files.local("data/save" + slot + ".json");
	}
	
	public int getNumSaveSlots() {
		return numSlots;
	}

	/**
	 * Serializes the gridModel into a json file.
	 * @param templateModel Model to save
	 */
	public void save(TemplateModel templateModel, int slot) {
		FileHandle saveFile = getSaveFileHandle(slot);
		log("Save", "Saving to " + saveFile.path());

		if(saveFile.exists()) {
			// TODO throw an overwrite exception and let caller handle it.
		}

		String data;
		if(TemplateGame.DEVMODE) {
			data = json.prettyPrint(templateModel);
		} else {
			data = Base64Coder.encodeString(json.toJson(templateModel));
		}

		saveFile.writeString(data, false);
	}

	/**
	 * Loads a saved TemplateModel.
	 * @return The saved TemplateModel. Throws exceptions if file not found,
	 *  file not readable, or no such slot.
	 */
	public TemplateModel load(int slot) throws LoadingException {
		try {
			FileHandle saveFile = getSaveFileHandle(slot);
			log("Load", "Loading from " + saveFile.path());
			String data = saveFile.readString().trim();
			if(data.matches("^[A-Za-z0-9/+=]+$")) {
				log("Load", "File is base64 encoded");
				data = Base64Coder.decodeString(data);
			}
			return json.fromJson(TemplateModel.class, data);
		} catch(IllegalArgumentException e) {
			throw new LoadingException(e.getMessage());
		} catch(GdxRuntimeException e) {
			throw new LoadingException(e.getMessage());
		}
	}

	/**
	 * Test code. Remember to use System.out.println instead of Gdx.app.log
	 * if testing outside of libGDX
	 */
	protected static void testme() {
		TemplateSerializer serializer = new TemplateSerializer();
		TemplateModel model = new TemplateModel();

		String saveData = serializer.json.prettyPrint(model);
		System.out.println(saveData);

		TemplateModel load = serializer.json.fromJson(TemplateModel.class, saveData);
		System.out.println(load.toString());
	}
}
