package com.left.addd.services;

import static com.left.addd.utils.Log.log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.left.addd.AdddGame;
import com.left.addd.model.GameModel;
import com.left.addd.utils.LoadingException;

public class GameSerializer {

	private final int numSlots = 3;
	private Json json;

	public GameSerializer() {
		this.json = new Json();
		json.setSerializer(GameModel.class, new Json.Serializer<GameModel>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void write(Json json, GameModel gridModel, Class knownType) {
				gridModel.save(json);
			}

			@SuppressWarnings("rawtypes")
			@Override
			public GameModel read(Json json, JsonValue jsonData, Class type) {
				return GameModel.load(json, jsonData);
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
	public void save(GameModel templateModel, int slot) {
		FileHandle saveFile = getSaveFileHandle(slot);
		log("Save", "Saving to " + saveFile.path());

		if(saveFile.exists()) {
			// TODO throw an overwrite exception and let caller handle it.
		}

		String data;
		if(AdddGame.DEVMODE) {
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
	public GameModel load(int slot) throws LoadingException {
		try {
			FileHandle saveFile = getSaveFileHandle(slot);
			log("Load", "Loading from " + saveFile.path());
			String data = saveFile.readString().trim();
			if(data.matches("^[A-Za-z0-9/+=]+$")) {
				log("Load", "File is base64 encoded");
				data = Base64Coder.decodeString(data);
			}
			return json.fromJson(GameModel.class, data);
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
		GameSerializer serializer = new GameSerializer();
		GameModel model = new GameModel();

		String saveData = serializer.json.prettyPrint(model);
		System.out.println(saveData);

		GameModel load = serializer.json.fromJson(GameModel.class, saveData);
		System.out.println(load.toString());
	}
}
