package com.left.addd.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.left.addd.utils.Res;

public class EnemySprite implements GameRenderable {

	private final int FRAMES = 6;
	private float time;
	private TextureRegion[] enemyFrames;
	private Animation idleAnimation;
	private TextureRegion currentFrame;

	@Override
	public void create() {
		time = 0f;

		Texture sheet = new Texture(Res.IMAGE + "enemy.png");
		TextureRegion[][] region = TextureRegion.split(sheet, sheet.getWidth() / FRAMES,
				sheet.getHeight());
		this.enemyFrames = new TextureRegion[FRAMES];
		int index = 0;
		for(int i = 0; i < FRAMES; i++, index++) {
			// only 1 row for this one
			enemyFrames[index] = region[0][i];
		}
		this.idleAnimation = new Animation(0.2f, enemyFrames);
	}

	@Override
	public TextureRegion getTextureForRender(float delta) {
		time += delta;
		currentFrame = idleAnimation.getKeyFrame(time, true);
		return currentFrame;
	}

	@Override
	public float getWidth() {
		return currentFrame.getRegionWidth();
	}

	@Override
	public float getHeight() {
		return currentFrame.getRegionHeight();
	}
}
