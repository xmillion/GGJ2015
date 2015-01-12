package com.left.template.screens;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.left.template.utils.Log.log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.left.template.TemplateGame;
import com.left.template.TemplateGame.Screens;
import com.left.template.services.MusicManager.Playlist;
import com.left.template.services.SoundManager.SoundList;
import com.left.template.utils.DefaultButtonListener;

/**
 * Main Menu.
 * TableLayout Editor (Java Web-Start):
 * http://table-layout.googlecode.com/svn/wiki/jws/editor.jnlp
 *
 */
public class MainMenuScreen extends AbstractMenuScreen {

	public MainMenuScreen(TemplateGame game) {
		super(game);
	}

	@Override
	public void show() {
		super.show();
		Skin skin = super.getSkin();
		
		Label title = new Label("Sample Game", getSkin());
		title.setAlignment(Align.center, Align.center);
		TextButton startButton = new TextButton("New Game", skin);
		startButton.addListener(new DefaultButtonListener() {
			@Override
			public void pressed(InputEvent event, float x, float y, int pointer, int button) {
				game.getSound().play(SoundList.CLICK);
				stage.getRoot().addAction(sequence(fadeOut(0.15f), run(new Runnable() {
					public void run() {
						game.setNextScreen(Screens.TEMPLATEGAME);
					}
				})));
			}
		});
		TextButton loadButton = new TextButton("Load", skin);
		loadButton.addListener(new DefaultButtonListener() {
			@Override
			public void pressed(InputEvent event, float x, float y, int pointer, int button) {
				game.getSound().play(SoundList.CLICK);
				game.setNextScreen(Screens.LOAD);
			}
		});
		TextButton optionsButton = new TextButton("Options", skin);
		optionsButton.addListener(new DefaultButtonListener() {
			@Override
			public void pressed(InputEvent event, float x, float y, int pointer, int button) {
				game.getSound().play(SoundList.CLICK);
				game.setNextScreen(Screens.OPTIONS);
			}
		});
		TextButton exitButton = new TextButton("Exit", skin);
		exitButton.addListener(new DefaultButtonListener() {
			@Override
			public void pressed(InputEvent event, float x, float y, int pointer, int button) {
				log("Goodbye");
				game.getSound().play(SoundList.CLICK);
				stage.getRoot().addAction(sequence(fadeOut(0.1f), run(new Runnable() {
					public void run() {
						Gdx.app.exit();
					}
				})));
			}
		});
		
		Table table = super.getTable();
		table.add(title).size(BUTTON_WIDTH, BUTTON_HEIGHT).spaceBottom(SPACING);
		table.row();
		table.add(startButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).uniform().fill().spaceBottom(SPACING);
		table.row();
		table.add(loadButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).uniform().fill().spaceBottom(SPACING);
		table.row();
		table.add(optionsButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).uniform().fill().spaceBottom(SPACING);
		table.row();
		table.add(exitButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).uniform().fill().spaceBottom(SPACING);
		table.pack();

		game.getMusic().play(Playlist.MENU);
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		width *= TemplateGame.getUIScaling();
		height *= TemplateGame.getUIScaling();
		Table table = super.getTable();
		table.setPosition(width - table.getWidth() - SPACING, height - table.getHeight() - SPACING);
	}
}
