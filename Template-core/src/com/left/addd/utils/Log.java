package com.left.addd.utils;

import com.badlogic.gdx.Gdx;

public class Log {
	private static final String TAG_MAIN = "GridGame";

	public static void l(String message) {
		log(TAG_MAIN, message);
	}
	public static void log(String message) {
		log(TAG_MAIN, message);
	}
	public static void log(String tag, String message) {
		Gdx.app.log(tag, message);
	}
	
	public static String pCoords(float x, float y) {
		return "(" + x + ", " + y + ")";
	}
	
	public static String pCoords(int x, int y) {
		return "(" + x + ", " + y + ")";
	}
}
