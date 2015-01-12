package com.left.template.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.left.template.TemplateGame;
import com.left.template.TemplateGame.Screens;
import com.left.template.services.SoundManager.SoundList;
import com.left.template.utils.DefaultButtonListener;

public class LoadScreen extends AbstractMenuScreen {

	public LoadScreen(TemplateGame game) {
		super(game);
	}

	@Override
	public void show() {
		super.show();
		Skin skin = super.getSkin();
		Table table = super.getTable();

		Label title = new Label("Load Game", skin);
		title.setAlignment(Align.center, Align.center);
		table.add(title).size(BUTTON_WIDTH, BUTTON_HEIGHT).spaceBottom(SPACING);
		table.row();

		for(int i = 0; i < game.getSaver().getNumSaveSlots(); i++) {
			TextButton button = new TextButton("Slot " + i, skin);
			final int saveSlot = i;
			button.addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					game.getSound().play(SoundList.CLICK);
					game.setNextScreen(Screens.TEMPLATEGAME, saveSlot);
				}
			});

			table.add(button).size(BUTTON_WIDTH * 2, BUTTON_HEIGHT).uniform().fill()
					.spaceBottom(SPACING);
			table.row();
		}

		TextButton backButton = new TextButton("Back", skin);
		backButton.addListener(new DefaultButtonListener() {
			@Override
			public void pressed(InputEvent event, float x, float y, int pointer, int button) {
				game.getSound().play(SoundList.CLICK);
				game.setNextScreen(Screens.MAINMENU);
			}
		});
		table.add(backButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).uniform().fill()
				.spaceBottom(SPACING);
		table.pack();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		width *= TemplateGame.getUIScaling();
		height *= TemplateGame.getUIScaling();
		Table table = super.getTable();
		table.setPosition((width - table.getWidth()) / 2, (height - table.getHeight()) / 2);
	}
}
