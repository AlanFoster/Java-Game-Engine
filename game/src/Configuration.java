package src;

import engine.misc.GameLogging;

/**
 * <p>
 * This class is offers a central location for changing the values of
 * configuration extremely fast. With the ability to change these values there
 * is now the potential for custom graphical interfaces with this configuration
 * too - Which I think would be appreciated by both potential end user and
 * developer.
 * </p>
 * <p>
 * I believe that the main reason for this class is that strings are immutable
 * in java. I <b>believe</b> that Java makes an attempt to intern Strings by
 * placing them in permgen space. I <b>believe</b> permgen is not trawled by
 * garbage collection, as it is likely the strings may be needed again. This
 * game isn't really a huge program, and this is most likely an unnecessary
 * precaution, but I'd like to think it is a good early design decision.
 * </p>
 * <p>
 * It should mentioned that all of these fields are public for quick access, so
 * there are no accessor methods. As these strings are final fields, there is
 * won't be the possibility to change them at run time however. This won't be a
 * problem, as if there is a need to completely change all of the GUI features
 * we could possible create a 'current style' object, which will symbol point to
 * an anonymous class and have all of the same fields that we would want access
 * to. It'll be easy enough to implement a simplistic means of changing all of
 * the fonts and images easily with this system.
 * </p>
 * 
 * @author Alan Foster
 * @version 1.0
 */
public final class Configuration {
	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * This class shouldn't be instantiated. This should also be done to all of
	 * the inner classes too, but it's not that important
	 **/
	private Configuration() {
	}

	// ----------------------------------------------------------------
	// GUI fields
	// ----------------------------------------------------------------
	public final static class GUI {
		// ----------------------------------------------------------------
		// Fonts
		// ----------------------------------------------------------------
		public final static class Fonts {
			public static final String HEADING = "fontTest";
			public static final String NORMAL = "fontTest";
			public static final String INPUT_FIELD = "fontTest";
			public static final String BUTTON_FONT = "fontTest";
			public static final String HUD = "hudFont";
			public static final String HUD_COUNT_DOWN = "HUDCountDown";
		}

		// ----------------------------------------------------------------
		// Graphics
		// ----------------------------------------------------------------
		public final static class Graphics {
			public static final String BACKGROUND_IMAGE = "grunge_background";
			public static final String MENU_HEADER = "welcomeScreenHeader";

			// Graphical buttons
			public static final String GRAPHIC_BUTTON_NORMAL = "mainMenuNormal";
			public static final String GRAPHIC_BUTTON_HOVER = "mainMenuHover";
			public static final String GRAPHIC_BUTTON_FOCUS = "mainMenuFocus";
		}

		// ----------------------------------------------------------------
		// Graphics Misc
		// ----------------------------------------------------------------
		public final static class Misc {
			/**
			 * The normal mouse shown throughout the system. IE, the basic mouse
			 * pointer
			 */
			public static final String GAME_MOUSE = "gameMouse";
		}

		// ----------------------------------------------------------------
		// Graphics Misc
		// ----------------------------------------------------------------
		public final static class GameOverlay {
			public static final String PAUSED_OVERLAY = "pausedGameGradient";
			public static final String DEATH_OVERLAY = "pausedGameGradient";
		}
	}

	// ----------------------------------------------------------------
	// Misc
	// ----------------------------------------------------------------
	public final static class Misc {
		// Currently not used.
		public final static class Logging {
			/**
			 * Currently not used... Potentially it /should/ be used in
			 * 'production' for actual errors that occur. But again, unneeded
			 * for a small game I guess. However, it should be noted that
			 * logging IS offered, just not the logging save location. This
			 * GameEngine makes use of the slf4j facade
			 * 
			 * @see GameLogging
			 **/
			public static final String LOGGING_LOCATION = "/logs/";
		}
	}
}
