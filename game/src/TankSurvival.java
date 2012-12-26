package src;

import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import menu.MainMenu;

import tests.GUITests;
import tests.PathFindingTests;
import tests.SoundTests;
import world.WorldManager;
import engine.main.*;
import engine.misc.GameLogging;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.EntitySystem;
import eventhandling.IKeyboardEventable;

/**
 * Tank survival is a game which allows the user to explore a top down
 * scrollable world and survive as long as possible. This game will make use of
 * a component system which will allow for extreme levels of extensibility.
 * <p>
 * This game makes use of a custom, but generic, game engine which provides us
 * with more than enough functionality1 to make a game. This can be found under
 * the gameEngine package.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameEngine
 * @see GameScreen
 * @see GameLayer
 */
@SuppressWarnings("serial")
public class TankSurvival extends GameEngine implements IKeyboardEventable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(TankSurvival.class);

	/**
	 * Whether or not to disable certain shortcuts within our keyPressed method
	 */
	boolean disabledShortcuts = false;

	// ----------------------------------------------------------------
	// Game Screen names
	// ----------------------------------------------------------------
	/**
	 * As using Strings for game screen names are 'brittle', I have decided to
	 * try to include a more compile-time friendly way of stopping mistyped game
	 * screens.
	 * <p>
	 * These screen names will be used when adding a game screen to the
	 * {@link GameScreenManager}, and when the setGameScreen method is being
	 * called we will use these game screen names too.
	 * <p>
	 * Note:: I have simply added this as a design idea, there is no requirement
	 * to do this as part of the GameEngine.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	public static final class GameScreenNames {
		public final static String MAIN_MENU = "mainMenu";
		public final static String LEVEL_ONE = "levelOne";

		public final static String GUI_TESTS = "guiTests";
		public final static String SOUND_TESTS = "soundTests";
		public final static String PATHFINDING_TESTS = "pathFindingTests";
	}

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new instance of TankSurvival, which calls the super constructor
	 * of the GameEngine. Tank survival is a top down scroller EXAMPLE game, and
	 * is not fully completed to a production game's standard. However it does
	 * show how the {@link GameEngine} and {@link EntitySystem} may be used.
	 */
	public TankSurvival() {
		super();
		// Register for key events which get sent out to our IKeyboardEventable
		// overrides
		keyEventHandler.registerEvents(this);

		// Set the gamelogging detail
		GameLogging.setLoggingLevel(GameLogging.LoggingLevel.ALL);

		// Set the desired width, height, bit depth
		// Note :: Although you can change this, the main menu etc 'breaks'
		// (because of the graphics). Generally in most cases (unless i've
		// forgotten) i've made things dynamically move, IE for centering and
		// accommodating larger screen sizes, but the graphics are not 'scaled'
		gameSettings.gameWidth = 1024;
		gameSettings.gameHeight = 768;
		gameSettings.gameDepth = 32;

		// Load the required information
		loadRequiredAssets();
		addRequiredGameScreens();

		// if we're developing we don't bother starting FSEM. This allowed for
		// me to a lot of hotspot deployment for game modifications.
		developing = false;

		/*
		 * If FSEM required is true it will attempt to start FSEM, and if it
		 * fails it will start run the game without being in FSEM anymore. If it
		 * is false and FSEM wasn't successfully started, it will end the
		 * program.
		 */
		boolean FSEMRequired = false;

		// turn off shortcuts within the keyPressed method
		disabledShortcuts = true;

		// begin the game with the desired width, height, depth and FSEM
		// required as false. When FSEM required is false, it means if the GD
		// does not support our desired screen size then we can make the
		// decision to go to non-fsem mode
		startGame(gameSettings.gameWidth, gameSettings.gameHeight,
				gameSettings.gameDepth,
				FSEMRequired);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * <p>
	 * Loads in the required assets for this game. Currently it doesn't matter
	 * what order assets are loaded in. For instance the load assets method
	 * could be called at any point during the game. The asset manager should be
	 * accessed through its singleton as
	 * GameAssetManager.getInstance().getObject(Type.class, "assetName"); This
	 * method call return an object of type T, so there's no need to do
	 * typecasting etc.
	 * </p>
	 * 
	 * @see GameAssetManager
	 */
	public void loadRequiredAssets() {
		// Load all the asset details we need
		GameAssetManager.getInstance().loadAssetDetailsFromXml(
				// Unsorted asset list -- I plan to split these into different
				// files closer to the end deadline.
				"/files/assetlists/newAssetList.xml",
				// Fonts
				"/files/assetlists/fontAssets.xml",
				// level info -- not done yet really.
				"/files/assetlists/levelAssets.xml",
				// basic sounds info -- not done yet really.
				"/files/assetlists/soundAssets.xml",
				// Interface details like button graphics etc
				"/files/assetlists/gui.xml",
				// Misc graphics, currently only contains particles
				"/files/assetlists/misc.xml",
				// Tile sets for the world
				"/files/assetlists/tileSets.xml"
				);
	}

	/**
	 * Adds all of the required GameScreens to the gameScreenManager. A
	 * GameScreen can be defined as
	 */
	public void addRequiredGameScreens() {
		// Actual screens related to the game
		// as you can see, there isn't much of a game yet :)
		gameScreenManager.addGameScreen(GameScreenNames.MAIN_MENU, new MainMenu());
		gameScreenManager.addGameScreen(GameScreenNames.LEVEL_ONE, new WorldManager("/files/assetlists/levelDetails.xml", "levelOne"));

		// Game Screens for testing
		gameScreenManager.addGameScreen(GameScreenNames.GUI_TESTS, new GUITests());
		gameScreenManager.addGameScreen(GameScreenNames.SOUND_TESTS, new SoundTests());
		gameScreenManager.addGameScreen(GameScreenNames.PATHFINDING_TESTS, new PathFindingTests());

		// Set the default loading GameScreen
		gameScreenManager.setGameScreen(GameScreenNames.MAIN_MENU);
	}

	// ----------------------------------------------------------------
	// IKeyboardEventable Implementation
	// ----------------------------------------------------------------
	/**
	 * Contains a bunch of quick shortcuts. These are mostly for developing
	 * purposes and will be removed come 'game time'. It is here that we set the
	 * escape button to close the game.
	 */
	@Override
	public void keyPressed(KeyEvent e) {

		// If the option to disable all shortcuts is turned off then we will
		// ignore any of the shortcuts created. This will be set when the game
		// is actually 'finished' for release
		if (!disabledShortcuts) {
			// shortcut to the guiTests
			if (e.getKeyCode() == KeyEvent.VK_G) {
				gameScreenManager.setGameScreen(GameScreenNames.GUI_TESTS);
			}
		}

		// If the M key is pressed, we will go to the mainMenu, this is a
		// shortcut and wouldn't be added to the actual game when it is
		// 'released
		if (e.getKeyCode() == KeyEvent.VK_M) {
			gameScreenManager.setGameScreen(GameScreenNames.MAIN_MENU);
		}

		// By default we have added the logic that if the escape button is
		// pressed, then the game will end
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			running = false;
		}
	}

	// ----------------------------------------------------------------
	// Main
	// ----------------------------------------------------------------
	/**
	 * Creates the instance of the game Tank Survival
	 */
	public static void main(String[] args) {
		// http://www.minecraftforum.net/topic/257-opengl-in-java-to-increase-performance/
		// System.setProperty("sun.java2d.translaccel", "true");
		// System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("sun.java2d.ddforcevram", "true");

		new TankSurvival();
	}
}