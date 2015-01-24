package com.left.template.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.left.addd.AdddGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Grid";
		config.width = 800;
		config.height = 600;
		config.resizable = true;
		new LwjglApplication(new AdddGame(), config);
	}
}
