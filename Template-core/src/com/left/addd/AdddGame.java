package com.left.addd;

import static com.left.addd.utils.Log.log;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.left.addd.AdddGame.Screens;
import com.left.addd.model.GameModel;
import com.left.addd.screens.AbstractScreen;
import com.left.addd.screens.LoadScreen;
import com.left.addd.screens.MainMenuScreen;
import com.left.addd.screens.OptionsScreen;
import com.left.addd.screens.SplashScreen;
import com.left.addd.screens.GameScreen;
import com.left.addd.services.MusicManager;
import com.left.addd.services.PreferenceManager;
import com.left.addd.services.SoundManager;
import com.left.addd.services.GameSerializer;
import com.left.addd.utils.LoadingException;

public class AdddGame extends Game implements ApplicationListener {
	public static final boolean DEVMODE = true;

	private Screens nextScreen;
	private int nextScreenState;
	private PreferenceManager preferenceManager;
	private GameSerializer gameSerializer;
	private MusicManager musicManager;
	private SoundManager soundManager;

	public static enum Screens {
		SPLASH, MAINMENU, OPTIONS, LOAD, GAME;
	}

	public void setNextScreen(Screens s) {
		nextScreen = s;
	}
	
	public void setNextScreen(Screens s, int state) {
		nextScreen = s;
		nextScreenState = state;
	}
	
	/**
	 * It is safer to use setNextScreen(s);
	 */
	public void setScreen(Screens s) {
		AbstractScreen screen;
		switch(s) {
		case SPLASH:
			screen = new SplashScreen(this);
			break;
		case MAINMENU:
			screen = new MainMenuScreen(this);
			break;
		case OPTIONS:
			screen = new OptionsScreen(this);
			break;
		case LOAD:
			screen = new LoadScreen(this);
			break;
		case GAME:
			if(nextScreenState >= 0) {
				try {
					// Load game
					GameModel model = gameSerializer.load(nextScreenState);
					screen = new GameScreen(this, model);
				} catch (LoadingException e) {
					// TODO show a notification on the screen
					log("Failed to load game.");
					screen = new MainMenuScreen(this);
				}
			} else {
				screen = new GameScreen(this);
			}
			break;
		default:
			log("Screen not listed!");
			return;
		}
		assert (screen != null);
		setScreen(screen);
	}
	
	public PreferenceManager getPrefs() {
		return preferenceManager;
	}
	
	public GameSerializer getSaver() {
		return gameSerializer;
	}

	public MusicManager getMusic() {
		return musicManager;
	}

	public SoundManager getSound() {
		return soundManager;
	}

	@Override
	public void create() {
		log("Create Game");
		preferenceManager = new PreferenceManager();
		
		gameSerializer = new GameSerializer();

		musicManager = new MusicManager();
		musicManager.setEnabled(preferenceManager.isMusicOn());
		musicManager.setVolume(preferenceManager.getMusicVolume());

		soundManager = new SoundManager();
		soundManager.setEnabled(preferenceManager.isSoundOn());
		soundManager.setVolume(preferenceManager.getSoundVolume());

		nextScreen = null;
		nextScreenState = -1;
		setScreen(Screens.SPLASH);
	}

	@Override
	public void render() {
		super.render();

		if(nextScreen != null) {
			setScreen(nextScreen);
			nextScreen = null;
			nextScreenState = -1;
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
	/**
	 * Let UI elements be scaled by this value.
	 * This allows buttons to be big enough to touch.
	 * 
	 * @return Scaling factor
	 */
	public static float getUIScaling() {
		switch(Gdx.app.getType()) {
		case Desktop:
		case WebGL:
		case Applet:
			// don't need to scale
			return 1f;
		case Android:
		case iOS:
			// decrease resolution to keep UI elements big enough
			float density = Gdx.graphics.getDensity();
			if(density < 1) {
				return 1f;
			} else if(density < 2) {
				return 0.5f;
			} else {
				// That's a nice screen you got there!
				return 0.25f;
			}
		default:
			// maybe there's a new supported platform :D
			log("You reached unreachable code");
			return 1f;
		}
	}
}
