package com.left.addd.view;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.left.addd.utils.Res;

/**
 * Handles the camera logic for GameView on desktop computers and web browsers.
 */
public class PannerDesktop extends PannerAbstract {

	private SpriteBatch pannerBatch;
	private OrthographicCamera pannerCamera;
	private boolean visible;
	private Image anchorImage;
	private Image arrowImage;
	private Image lineImage;
	private Vector2 anchor;
	private Vector2 arrow;
	private Vector2 line;
	
	public PannerDesktop(TextureAtlas atlas, Vector3 minBound, Vector3 maxBound) {
		super(atlas, minBound, maxBound);
		
		this.pannerBatch = new SpriteBatch();
		this.pannerCamera = new OrthographicCamera();
		visible = false;
		anchorImage = new Image(atlas.findRegion(Res.PAN + "anchor"));
		anchorImage.setOrigin(anchorImage.getWidth() / 2, anchorImage.getHeight() / 2);
		arrowImage = new Image(atlas.findRegion(Res.PAN + "arrow"));
		arrowImage.setOrigin(arrowImage.getWidth(), arrowImage.getHeight() / 2);
		lineImage = new Image(atlas.findRegion(Res.PAN + "line"));
		lineImage.setOrigin(0, lineImage.getHeight() / 2);
		anchor = new Vector2();
		arrow = new Vector2();
		line = new Vector2();
	}
	
	// Input controls

	/**
	 * On desktops, right mouse button will start the panning process.
	 * @param screenX Mouse X coordinate
	 * @param screenY Mouse Y coordinate
	 * @param pointer Irrelevant on desktops (always 0)
	 * @param button Mouse button
	 */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(button != Buttons.RIGHT) return false;
		anchor.x = screenX;
		// Camera screen coordinates are inverted in the y-axis
		anchor.y = viewCamera.viewportHeight - screenY;
		this.visible = true;
		touchDragged(screenX, screenY, pointer, button);
		return true;
	}
	
	/**
	 * On desktops, right mouse button will stop the panning process.
	 * @param screenX Mouse X coordinate
	 * @param screenY Mouse Y coordinate
	 * @param pointer Irrelevant on desktops (always 0)
	 * @param button Mouse button
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(button != Buttons.RIGHT) return false;
		this.visible = false;
		line.x = 0;
		line.y = 0;
		if(anchor.epsilonEquals(screenX, viewCamera.viewportHeight - screenY, 4)) {
			// no panning happened
			return false;
		}
		return true;
	}
	
	/**
	 * On desktops, right mouse button will update the panning vector.
	 * @param screenX Mouse X coordinate
	 * @param screenY Mouse Y coordinate
	 * @param pointer Irrelevant on desktops (always 0)
	 * @param button Mouse button being held down.
	 */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer, int button) {
		if(button != Buttons.RIGHT) return false;
		arrow.x = screenX;
		// Camera screen coordinates are inverted in the y-axis
		arrow.y = viewCamera.viewportHeight - screenY;
		if(visible) {
			// calculate line while we're at it.
			line.x = arrow.x - anchor.x;
			line.y = arrow.y - anchor.y;
			line.scl(PAN_SCALE);
		}
		return true;
	}
	
	// Rendering
	
	/**
	 * Renders the Panner graphics. Panner uses its own SpriteBatch.
	 */
	@Override
	public void render(float delta) {
		if(visible) {
			pan(line);

			// If desktops end up having custom pixel densities, then this width/height will need to scale.
			float len = line.len() / PAN_SCALE;
			float angle = line.angle();
			anchorImage.setPosition(anchor.x - anchorImage.getWidth() / 2, anchor.y - anchorImage.getHeight() / 2);
			anchorImage.setRotation(angle);
			arrowImage.setPosition(arrow.x - arrowImage.getWidth(), arrow.y - arrowImage.getHeight() / 2);
			arrowImage.setRotation(angle);
			lineImage.setPosition(anchor.x, anchor.y - lineImage.getHeight() / 2);
			lineImage.setRotation(angle);
			lineImage.setScaleX((len- arrowImage.getWidth()) / lineImage.getWidth());

			pannerBatch.begin();
			anchorImage.draw(pannerBatch, 1f);
			lineImage.draw(pannerBatch, 1f);
			if(len > arrowImage.getWidth()) {
				arrowImage.draw(pannerBatch, 1f);
			}
			pannerBatch.end();
		}
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		pannerCamera.viewportWidth = width;
		pannerCamera.viewportHeight = height;
		pannerCamera.position.set(width / 2, height / 2, 0);
		// pannerCamera.position.set(-width / 2, -height / 2, 0);
		pannerCamera.update();
		pannerBatch.setProjectionMatrix(pannerCamera.combined);
	}
}
