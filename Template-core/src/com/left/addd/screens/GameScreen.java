package com.left.addd.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.left.addd.AdddGame;
import com.left.addd.model.GameModel;
import com.left.addd.services.MusicManager.Playlist;
import com.left.addd.view.GameView;
import com.left.addd.view.UIView;

public class GameScreen extends AbstractScreen {
	private GameModel gameModel;
	private GameView gameView;
	private UIView uiView;

	public GameScreen(AdddGame game) {
		this(game, new GameModel(15, 15));
	}
	
	public GameScreen(AdddGame game, GameModel model) {
		super(game);
		
		gameModel = model;
		gameView = new GameView(game, gameModel, getAtlas());
		uiView = new UIView(game, gameView, getAtlas(), getSkin());
	}

	@Override
	public boolean isGameScreen() {
		return true;
	}

	@Override
	public void show() {
		super.show();
		game.getMusic().play(Playlist.ACTION);

		// UI gets first priority on input events
		InputMultiplexer inputMux = new InputMultiplexer();
		inputMux.addProcessor(uiView.getStage());
		inputMux.addProcessor(gameView);
		Gdx.input.setInputProcessor(inputMux);
	}

	@Override
	public void render(float delta) {
		SpriteBatch batch = getBatch();
		// Draw the views
		Gdx.gl.glClearColor(0.2f, 0f, 0.4f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if(uiView.getState() == UIView.State.RUNNING) {
			switch(uiView.getSpeed()) {
			case PAUSE:
				gameView.render(batch, delta);
				break;
			case NORMAL:
				gameModel.update(delta);
				gameView.render(batch, delta);
				break;
			case FASTER:
				// TODO set actual speeds
				gameModel.update(delta * 2);
				gameView.render(batch, delta * 2);
				break;
			case FASTEST:
				gameModel.update(delta * 5);
				gameView.render(batch, delta * 5);
				break;
			}
		} else {
			gameView.render(batch, delta);
		}
		
		uiView.render(delta);

		// TODO Check for game over condition
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		gameView.resize(width, height);
		uiView.resize(width, height);
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
