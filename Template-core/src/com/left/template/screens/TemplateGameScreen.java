package com.left.template.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.left.template.TemplateGame;
import com.left.template.model.TemplateModel;
import com.left.template.services.MusicManager.Playlist;
import com.left.template.view.TemplateView;
import com.left.template.view.UIView;

public class TemplateGameScreen extends AbstractScreen {
	private TemplateModel templateModel;
	private TemplateView templateView;
	private UIView uiView;

	public TemplateGameScreen(TemplateGame game) {
		this(game, new TemplateModel());
	}
	
	public TemplateGameScreen(TemplateGame game, TemplateModel model) {
		super(game);
		
		templateModel = model;
		templateView = new TemplateView(game, templateModel, getAtlas());
		templateView.create();
		uiView = new UIView(game, templateView, getAtlas(), getSkin());
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
		inputMux.addProcessor(templateView);
		Gdx.input.setInputProcessor(inputMux);
	}

	@Override
	public void render(float delta) {
		SpriteBatch batch = getBatch();
		
		if(uiView.getState() == UIView.State.RUNNING) {
			switch(uiView.getSpeed()) {
			case PAUSE:
				break;
			case NORMAL:
				templateModel.update(delta);
				break;
			case FASTER:
				// TODO set actual speeds
				templateModel.update(delta * 2);
				break;
			case FASTEST:
				templateModel.update(delta * 5);
				break;
			}
		}

		// Draw the views
		Gdx.gl.glClearColor(0.2f, 0f, 0.4f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		templateView.render(batch, delta);
		uiView.render(delta);

		// TODO Check for game over condition
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		templateView.resize(width, height);
		uiView.resize(width, height);
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
