package engine.misc;

import engine.main.GameEngine;

/**
 * Stores all of the commonly used GameSettings. I have created this method so
 * that objects do need to have access to the GameEngine, which would be weird.
 * <p>
 * I have attempted to make this 'secure' by making the only getter method
 * require a time of GameEngine, which should mean that GameEngine is the only
 * object able to change these settings. And as we can't instantiate GameEngine
 * directly (As it's a singleton, and the only constructor is protected) I think
 * this covers us from any security problems, and most importantly it stops
 * idiots changing settings directly...
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GameSettings {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The singleton instance of the settings
	 */
	private static Settings instance;

	// ----------------------------------------------------------------
	// Constructor And Singleton method
	// ----------------------------------------------------------------
	/**
	 * Creates a new instance of the Settings class providing that the 'engine'
	 * object passed into it isn't null, and an instance doesn't already exist.
	 * 
	 * @param engine
	 *            Used for verification purposes. Only the GameEngine should be
	 *            changing the settings within this object.
	 */
	public GameSettings(GameEngine engine) {
		// Make sure someone smart hasn't done getInstance(null), and that an
		// instance doesn't exist already
		if (engine != null && instance == null) {
			instance = new Settings();
		}
	}

	/**
	 * Returns the current Settings instance. This can only be accessed by the
	 * GameEngine
	 * 
	 * @param engine
	 *            The GameEngien is required for compile time and run time
	 *            validation in order to retrieve this object.
	 * @return The current settings object which can be manipulated directly.
	 */
	public static Settings getInstance(GameEngine engine) {
		// Make sure someone smart hasn't done getInstance(null)
		if (engine == null) {
			return null;
		}
		return instance;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Get the current game width
	 * 
	 * @return The game's screen width in pixels
	 */
	public static int getGameWidth() {
		return instance.gameWidth;
	}

	/**
	 * Get the current game height
	 * 
	 * @return The game's screen height in pixels
	 */
	public static int getGameHeight() {
		return instance.gameHeight;
	}

	/**
	 * Get the current game bit depth
	 * 
	 * @return The game screen's bit depth when in FSEM
	 */
	public static int getGameBitDepth() {
		return instance.gameDepth;
	}

	// ----------------------------------------------------------------
	// The 'actual' settings object
	// ----------------------------------------------------------------
	/**
	 * Stores the actual Settings which can be manipulated directly with the
	 * public fields. This object reference should only be given to the
	 * GameEngine and no other object.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	public class Settings {
		/**
		 * Stores the current screens' width.
		 */
		public int gameWidth;
		/**
		 * Stores the current screen's height.
		 */
		public int gameHeight;
		/**
		 * Stores the current game bit depth.
		 */
		public int gameDepth;
	}
}
