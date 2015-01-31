package com.left.addd.utils;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class TexturePackerTool {
	private static final String RAW_DIR = "raw/";
	private static final String ASSETS_DIR = "../Template-android/assets/";
	
	public static void main(String[] args) {
		String inputDir = RAW_DIR;
		String outputDir = ASSETS_DIR + Res.IMAGE;
		String outputFileName = "textures";
		
		System.out.println("Packing images from " + inputDir + " to " + outputDir);
		TexturePacker.process(inputDir, outputDir, outputFileName);
		
		// command line:
		// java -cp gdx.jar;gdx-tools.jar com.badlogic.gdx.tools.imagepacker.TexturePacker2
		// [inputDir] [outputDir] [outputFile]
	}
}
