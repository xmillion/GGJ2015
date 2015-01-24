package com.left.addd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.left.addd.AdddGame;
import com.left.addd.AdddGame.Screens;
import com.left.addd.model.Time;
import com.left.addd.services.SoundManager.SoundList;
import com.left.addd.utils.DefaultButtonListener;
import com.left.addd.utils.Res;

/**
 * Manages the drawing and interaction of UI elements in the GridScreen.
 * Reference: https://github.com/libgdx/libgdx/tree/master/demos/very-angry-robots/very-angry-robots/src/com/badlydrawngames/veryangryrobots
 */
public class UIView implements Disposable {

	public enum State {
		MENU, PAUSED, RUNNING;
	}

	public enum Speed {
		PAUSE, NORMAL, FASTER, FASTEST;
	}
	// Operational State
	private State state;
	private Speed speed;

	protected static final float BUTTON_WIDTH = 150f;
	protected static final float BUTTON_HEIGHT = 30f;
	protected static final float BUTTON_SPACING = 10f;

	private final AdddGame game;
	private final GameView gridView;
	private final TextureAtlas atlas;
	private final Skin skin;
	private final Stage stage;

	// UI elements
	private TextButton menuButton;
	private Table pauseMenu;
	private Table saveMenu;
	private Table loadMenu;
	private Table timeTable;
	private Label date;

	private TextureRegionDrawable pauseBackground;
	private Image[] timeIcons;

	public UIView(AdddGame game, GameView gridView, TextureAtlas atlas, Skin skin) {
		this.game = game;
		this.gridView = gridView;
		this.atlas = atlas;
		this.skin = skin;
		this.stage = new Stage(new ScreenViewport());
		this.pauseBackground = new TextureRegionDrawable(atlas.findRegion(Res.PAUSE + "pausemenu"));

		createUI();
		setState(State.RUNNING, Speed.NORMAL);
	}

	public void createUI() {
		menuButton = new TextButton("Menu", skin);
		menuButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
		menuButton.addListener(new DefaultButtonListener() {
			@Override
			public void pressed(InputEvent event, float x, float y, int pointer, int button) {
				game.getSound().play(SoundList.CLICK);
				UIView.this.setState(State.MENU);
			}
		});

		stage.addActor(menuButton);
		stage.addActor(getTimeTable());
	}

	// Lazy load menus

	private Table getPauseMenu() {
		if(pauseMenu == null) {
			pauseMenu = new Table();
			pauseMenu.setBackground(pauseBackground);
			pauseMenu.setTouchable(Touchable.enabled);
			pauseMenu.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					// Do nothing but don't fall through
					return true;
				}
			});

			Label menuLabel = new Label("Game Paused", skin);
			TextButton saveButton = new TextButton("Save Game", skin);
			saveButton.addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					game.getSound().play(SoundList.CLICK);
					UIView.this.showSaveMenu();
				}
			});
			TextButton loadButton = new TextButton("Load Game", skin);
			loadButton.addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					game.getSound().play(SoundList.CLICK);
					UIView.this.showLoadMenu();
				}
			});
			TextButton exitButton = new TextButton("Exit to Main Menu", skin);
			exitButton.addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					game.getSound().play(SoundList.CLICK);
					game.setNextScreen(Screens.MAINMENU);
				}
			});
			TextButton closeButton = new TextButton("Resume", skin);
			closeButton.addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					game.getSound().play(SoundList.CLICK);
					UIView.this.setState(State.RUNNING);
				}
			});

			pauseMenu.add(menuLabel).spaceBottom(BUTTON_SPACING);
			pauseMenu.row();
			pauseMenu.add(saveButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).spaceBottom(BUTTON_SPACING);
			pauseMenu.row();
			pauseMenu.add(loadButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).spaceBottom(BUTTON_SPACING);
			pauseMenu.row();
			pauseMenu.add(exitButton).size(BUTTON_WIDTH, BUTTON_HEIGHT)
					.spaceBottom(BUTTON_SPACING * 2);
			pauseMenu.row();
			pauseMenu.add(closeButton).size(BUTTON_WIDTH, BUTTON_HEIGHT)
					.spaceBottom(BUTTON_SPACING);
			
			pauseMenu.pack();
			pauseMenu.setFillParent(true);
		}
		return pauseMenu;
	}

	private Table getSaveMenu() {
		if(saveMenu == null) {
			saveMenu = new Table();
			saveMenu.setBackground(pauseBackground);
			saveMenu.setTouchable(Touchable.enabled);
			saveMenu.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					// Do nothing but don't fall through
					return true;
				}
			});
			Label title = new Label("Save Game", skin);
			title.setAlignment(Align.center, Align.center);
			saveMenu.add(title).size(BUTTON_WIDTH, BUTTON_HEIGHT).spaceBottom(BUTTON_SPACING);
			saveMenu.row();

			for(int i = 0; i < game.getSaver().getNumSaveSlots(); i++) {
				TextButton button = new TextButton("Slot " + i, skin);
				final int saveSlot = i;
				button.addListener(new DefaultButtonListener() {
					@Override
					public void pressed(InputEvent event, float x, float y, int pointer, int button) {
						game.getSound().play(SoundList.CLICK);
						game.getSaver().save(gridView.getModel(), saveSlot);
						UIView.this.hideSaveMenu();
						UIView.this.setState(State.RUNNING);
					}
				});

				saveMenu.add(button).size(BUTTON_WIDTH * 2, BUTTON_HEIGHT).uniform().fill()
						.spaceBottom(BUTTON_SPACING);
				saveMenu.row();
			}

			TextButton backButton = new TextButton("Back", skin);
			backButton.addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					game.getSound().play(SoundList.CLICK);
					UIView.this.hideSaveMenu();
				}
			});
			saveMenu.add(backButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).uniform().fill()
					.spaceBottom(BUTTON_SPACING);
			
			saveMenu.pack();
			saveMenu.setFillParent(true);
		}
		return saveMenu;
	}

	private Table getLoadMenu() {
		if(loadMenu == null) {
			loadMenu = new Table();
			loadMenu.setBackground(pauseBackground);
			loadMenu.setTouchable(Touchable.enabled);
			loadMenu.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					// Do nothing but don't fall through
					return true;
				}
			});
			
			Label title = new Label("Load Game", skin);
			title.setAlignment(Align.center, Align.center);
			loadMenu.add(title).size(BUTTON_WIDTH, BUTTON_HEIGHT).spaceBottom(BUTTON_SPACING);
			loadMenu.row();

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

				loadMenu.add(button).size(BUTTON_WIDTH * 2, BUTTON_HEIGHT).uniform().fill()
						.spaceBottom(BUTTON_SPACING);
				loadMenu.row();
			}

			TextButton backButton = new TextButton("Back", skin);
			backButton.addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					game.getSound().play(SoundList.CLICK);
					UIView.this.hideLoadMenu();
				}
			});
			loadMenu.add(backButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).uniform().fill()
					.spaceBottom(BUTTON_SPACING);
			
			loadMenu.pack();
			loadMenu.setFillParent(true);
		}
		return loadMenu;
	}

	private Table getTimeTable() {
		if(timeTable == null) {
			timeTable = new Table();
			timeTable.setBackground(pauseBackground);
			timeTable.setTouchable(Touchable.enabled);
			timeTable.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					// Do nothing but don't fall through
					return true;
				}
			});
			timeTable.align(Align.bottom | Align.left);
			
			date = new Label("Date", skin);
			
			timeIcons = new Image[8];
			timeIcons[0] = new Image(atlas.findRegion(Res.TIME + "pauseoff"));
			timeIcons[1] = new Image(atlas.findRegion(Res.TIME + "pauseon"));
			timeIcons[2] = new Image(atlas.findRegion(Res.TIME + "playoff"));
			timeIcons[3] = new Image(atlas.findRegion(Res.TIME + "playon"));
			timeIcons[4] = new Image(atlas.findRegion(Res.TIME + "fasteroff"));
			timeIcons[5] = new Image(atlas.findRegion(Res.TIME + "fasteron"));
			timeIcons[6] = new Image(atlas.findRegion(Res.TIME + "fastestoff"));
			timeIcons[7] = new Image(atlas.findRegion(Res.TIME + "fasteston"));

			timeIcons[0].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// pause
					UIView.this.setState(State.PAUSED);
				}
			});
			timeIcons[1].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// unpause
					UIView.this.setState(State.RUNNING);
				}
			});
			timeIcons[2].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// play
					UIView.this.setState(State.RUNNING);
					UIView.this.setSpeed(Speed.NORMAL);
				}
			});
			timeIcons[3].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// already playing
					UIView.this.setState(State.RUNNING);
					UIView.this.setSpeed(Speed.NORMAL);
				}
			});
			timeIcons[4].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// play
					UIView.this.setState(State.RUNNING);
					UIView.this.setSpeed(Speed.FASTER);
				}
			});
			timeIcons[5].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// already faster
					UIView.this.setState(State.RUNNING);
					UIView.this.setSpeed(Speed.FASTER);
				}
			});
			timeIcons[6].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// play
					UIView.this.setState(State.RUNNING);
					UIView.this.setSpeed(Speed.FASTEST);
				}
			});
			timeIcons[7].addListener(new DefaultButtonListener() {
				@Override
				public void pressed(InputEvent event, float x, float y, int pointer, int button) {
					// already faster
					UIView.this.setState(State.RUNNING);
					UIView.this.setSpeed(Speed.FASTEST);
				}
			});
		}
		return timeTable;
	}

	public Stage getStage() {
		return stage;
	}

	public State getState() {
		return state;
	}

	public Speed getSpeed() {
		return speed;
	}

	private void setState(State state) {
		setState(state, speed);
	}

	private void setSpeed(Speed speed) {
		setState(state, speed);
	}

	private void setState(State state, Speed speed) {
		this.state = state;
		this.speed = speed;

		Table timeTable = getTimeTable();
		timeTable.clearChildren();
		timeTable.add(date).colspan(4).align(Align.left);
		timeTable.row();
		switch(this.state) {
		case MENU:
			showPauseMenu();
			timeTable.add(timeIcons[1]);
			break;
		case PAUSED:
			hidePauseMenu();
			timeTable.add(timeIcons[1]);
			break;
		case RUNNING:
			hidePauseMenu();
			timeTable.add(timeIcons[0]);
			break;
		}
		switch(this.speed) {
		case PAUSE:
			timeTable.add(timeIcons[2]);
			timeTable.add(timeIcons[4]);
			timeTable.add(timeIcons[6]);
			break;
		case NORMAL:
			timeTable.add(timeIcons[3]);
			timeTable.add(timeIcons[4]);
			timeTable.add(timeIcons[6]);
			break;
		case FASTER:
			timeTable.add(timeIcons[2]);
			timeTable.add(timeIcons[5]);
			timeTable.add(timeIcons[6]);
			break;
		case FASTEST:
			timeTable.add(timeIcons[2]);
			timeTable.add(timeIcons[4]);
			timeTable.add(timeIcons[7]);
			break;
		}
		timeTable.pack();
		timeTable.setPosition(0, 0);
	}
	
	private void showPauseMenu() {
		stage.addActor(getPauseMenu());
	}
	
	private void hidePauseMenu() {
		getPauseMenu().remove();
	}
	
	private void showSaveMenu() {
		hidePauseMenu();
		stage.addActor(getSaveMenu());
	}
	
	private void hideSaveMenu() {
		getSaveMenu().remove();
		showPauseMenu();
	}
	
	private void showLoadMenu() {
		hidePauseMenu();
		stage.addActor(getLoadMenu());
	}
	
	private void hideLoadMenu() {
		getLoadMenu().remove();
		showPauseMenu();
	}

	/**
	 * Render the UI, and only the UI.
	 */
	public void render(float delta) {
		// Get game info
		Time time = gridView.getModel().getTime();
		long hour = time.getHour();
		long day = time.getDay();
		date.setText("Day " + day + ", " + hour + ":00");
		
		// Update
		stage.act(delta);
		stage.draw();
	}

	public void resize(int width, int height) {
		Viewport viewport = stage.getViewport();
		viewport.update(width, height, true);
		
		// Shift everything back into the viewport.
		menuButton.setPosition(width - menuButton.getWidth(), 0);
		timeTable.setPosition(0, 0);
	}

	@Override
	public void dispose() {
		// may crash, comment out if so.
		// http://www.badlogicgames.com/forum/viewtopic.php?f=11&t=3624
		stage.clear();
		stage.dispose();
	}
}
