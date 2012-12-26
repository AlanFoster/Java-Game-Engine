package tests;

import src.Configuration;
import engine.assets.GameGraphic;
import engine.assets.GameSound;
import engine.gui.GameButton;
import engine.gui.GameButtonGraphical;
import engine.gui.GameFont;
import engine.main.GameEngine;
import engine.main.GameLayer;
import engine.main.GameScreen;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;
import engine.misc.managers.GameSoundManager;

public class SoundTests extends GameScreen {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(SoundTests.class);

	GameLayer controls;

	/**
	 * List of asset names that are songs. Will be used to generate the buttons on this sound test page
	 */
	String[] songList = {
			"beep",
			"gameTrack",
			"explosion01",
			"bomb03",
			"niceday",
			"scars",
	};
	
	public SoundTests() {
		super("soundTests");
		
		controls = new GameLayer("soundControls");
		String buttonFont = Configuration.GUI.Fonts.BUTTON_FONT;

		int buttonWidth = 270;
		int buttonHeight = 40;
		
		GameButton stopSound = new GameButton("stopSound",
				GameAssetManager.getInstance().getClonedObject(GameFont.class, buttonFont),
				"stop sound", 350, 20, buttonWidth, buttonHeight) {
			@Override
			public void mousePressed() {
				super.mousePressed();
				stopSound();
			}
		};
		
		GameButton setRandomVolume = new GameButton("randomVolume",
				GameAssetManager.getInstance().getClonedObject(GameFont.class, buttonFont),
				"Volume", 620 + 20, 20, buttonWidth, buttonHeight) {
			@Override
			public void mousePressed() {
				super.mousePressed();
				GameSoundManager.getInstance().setMasterVolume((float) Math.random());
			}
		};
		
		controls.add(stopSound);
		controls.add(setRandomVolume);
	
		// Create the buttons that will play the sounds we need
		int posX = 50;
		int posY = 20;
		int verticalSpacing = 30;
		
		for(String songName : songList){ 
			GameButton button = new GameButton(songName,
					GameAssetManager.getInstance().getClonedObject(GameFont.class, buttonFont),
					songName, posX, posY, buttonWidth, buttonHeight) {
				@Override
				public void mousePressed() {
					super.mousePressed();
					playSound(getName());
				}
			};
			
			controls.add(button);
			posY += buttonHeight + verticalSpacing;
		}

		addGameLayer(controls);
	}

	public void playSound(String songName) {
		GameSoundManager.getInstance().playSound(GameAssetManager.getInstance().getObject(GameSound.class, songName));
	}

	public void stopSound() {
		GameSoundManager.getInstance().stopAllSounds();
	}
}
