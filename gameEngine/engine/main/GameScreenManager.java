package engine.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import engine.interfaces.ILogical;
import engine.interfaces.IRenderUpdateDelegator;
import engine.misc.GameLogging;
import engine.misc.GameSettings;
import engine.misc.Helpers;
import engine.misc.State;
import engine.misc.managers.StateManager;
import eventhandling.IKeyboardEventable;
import eventhandling.IMouseEventable;
import eventhandling.KeyEventHandler;
import eventhandling.MouseEventHandler;

/**
 * This gameScreenManager receives the logical updates and draw requests from
 * the GameEngine, and this class passes the messages onto the current
 * GameScreen that is active. When there is a request to change the GameScreen
 * this method will also apply a fade out and fade in effect between GameScreen
 * changes.
 * <p>
 * It is also this class that provides the 'logical' updates to the event
 * systems so that they fire their waiting events during the time of the logic
 * update. I have done this explicitly with two fields for the event systems,
 * just to make it clearer, rather than reuse the {@link ILogical} idea that
 * GameLayer provides for instance.
 * <p>
 * ** Note :: At the time of writing this engine I designed 'GameScreens' as
 * being completely separate entities and I never imagined the possibility of
 * GameScreens being stacked together, like GameLayers are, but after exploring
 * apphub's XNA GameStateManager I noticed it used its GameScreens like I use my
 * GameLayers... If there exists such a requirement to do such a thing I would
 * prefer the use of a list of current game screens instead of the single one
 * currently, and for them to only receive logical/drawing interface (with the
 * linked list implementation being used). This would stop the useless need for
 * 'if gameScreen.active) gameScreen.update' logic that the apphub system liked
 * using... IE only the screens which are currently 'active' are added to the
 * list, and not every single one which could possibly be active.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameLayer
 * @see GameScreen
 * @see GameEngine
 */
public final class GameScreenManager implements IRenderUpdateDelegator {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameScreenManager.class);

	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * This EventHandler receives AWT mouse events and stores them for the next
	 * time the game loop's logicUpdate is fired. It will then call all of the
	 * relevant event methods through all objects that have registered with this
	 * system (Requires the {@link IMouseEventable} interface to be
	 * implemented).
	 * <p>
	 * This GameScreen manager is the point at which the mouseEventHandler will
	 * recieve its 'logic update' in which it will fire all of its required
	 * events.
	 */
	private final static MouseEventHandler mouseEventHandler = MouseEventHandler.getInstance();

	/**
	 * This EventHandler receives AWT key events and stores them for the next
	 * time the game loop's logicUpdate is fired. It will then call all of the
	 * relevant event methods through all objects that have registered with this
	 * system (Requires the {@link IKeyboardEventable} interface to be
	 * implemented).
	 * <p>
	 * This GameScreen manager is the point at which the keyEventHandler will
	 * recieve its 'logic update' in which it will fire all of its required
	 * events.
	 */
	private final static KeyEventHandler keyEventHandler = KeyEventHandler.getInstance();

	/**
	 * The state manager which stores all of the possible states that that this
	 * Class can be under. This will be updated explicitly within the
	 * logicUpdateMethod
	 */
	protected StateManager stateManager;

	/**
	 * Stores a Map of String to GameLayerManagers. This should be populated
	 * with all possible gameScreens during game.
	 * 
	 * @see addGameScreen
	 */
	private final Map<String, GameScreen> gameScreens;
	/**
	 * The current game screen which is recieving logical and draw updates
	 */
	private GameScreen currentGameScreen;
	/**
	 * The screen which will replace the current game screen during the next
	 * logical updates.
	 */
	private GameScreen waitingGameScreen;

	/**
	 * The desired colour that is wanted when changing game screens
	 */
	private int desiredTransitionColor = 0x000000;

	/**
	 * The actual Color that is drawn to the screen, set when we are either in
	 * 'fadeIn' or 'fadeOut' sstates
	 */
	private Color transitionColor = new Color(desiredTransitionColor);

	private int transitionSpeed = 5;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new instance of a GameScreenManager which will maintain a
	 * single GameScreen at a time and send logical and drawing information
	 * during each game loop to the currently active game screen
	 */
	public GameScreenManager() {
		gameScreens = new HashMap<String, GameScreen>();
	}

	/**
	 * Called by the game engine within the startGame method
	 * 
	 * Stops a game being started when there is no starting GameScreen selected
	 */
	@Override
	public void startUp() {
		if (waitingGameScreen == null) {
			logger.error(new NullPointerException(),
					Helpers.concat("Tried starting game without there being default game screen given.",
							"Make sure to set it with setCurrentGameScreen(GameScreen gameScreen)"));
			Runtime.getRuntime().exit(0xDEADBEEF);
		}

		stateManager = new StateManager("gameScreenmanagerStateManager");

		// Called after a gamescreen has just been loaded. After it has faded
		// in, it will change to a state which will check whether or not it
		// needs to change the game screen again
		stateManager.addPossibleState(new State("fadeIn") {
			int alpha = 255;

			@Override
			public void logicUpdate(GameTime gameTime) {
				transitionColor = new Color(((alpha -= transitionSpeed) << 24) | desiredTransitionColor, true);
				if (alpha < transitionSpeed) {
					transitionColor = null;
					// change the alpha back to 255, for next time the game
					// screen is changed
					alpha = 255;
					setTransitionStateName("checkWaitingGameScreen");
				} else {
					setTransitionStateName(getName());
				}
			}
		});

		// Called when a request is made to replace the current game screen. It
		// will fade out first, and after fading out it will transition to a
		// state which will actually replace the gamescreen
		stateManager.addPossibleState(new State("fadeOut") {
			int alpha;

			@Override
			public void logicUpdate(GameTime gameTime) {
				if (alpha == 255)
					alpha = 0;
				transitionColor = new Color(((alpha += transitionSpeed) << 24) | desiredTransitionColor, true);
				if (alpha >= 255) {
					setTransitionStateName("changeCurrentGameScreen");
				} else {
					setTransitionStateName(getName());
				}
			}
		});

		// Called when there was a request to change the GameScreen, and the
		// current waitingGameScreen is not null. This method will attempt to
		// fire the GC when swapping game screens. This is where both the
		// 'startUp' (for the waitingGameScreen) and 'cleanUp' (from the
		// currentGameScreen) methods will be fired
		stateManager.addPossibleState(new State("changeCurrentGameScreen") {
			@Override
			public void logicUpdate(GameTime gameTime) {
				if (currentGameScreen != null) {
					// Call the clean up for the current layer, IE remove events
					// etc
					currentGameScreen.cleanUp();
					// Now is the best time to do GC. A well designed game
					// shouldn't have to do GC much, but it's better to run it
					// now rather than for it to be forcibly ran during an
					// important part of the game
					System.gc();
				}

				currentGameScreen = waitingGameScreen;
				// Call the start up for the layer, register events, reset
				// components etc
				currentGameScreen.startUp(gameTime);

				// / Remove the waiting game screen as it has been replaced
				waitingGameScreen = null;

				// fade in the screen now
				setTransitionStateName("fadeIn");
			}
		});

		// When in this state we busy check whether or not the waitinGameScreen
		// is null, and if it is, we should go the fade out state, which will
		// then eventually load the new gameScreen. We could possibly change
		// into the fadeOut transition when setGameScreen is called... But I
		// like this as it means that it will only consider a change game screen
		// after the animation etc has completed, which I think will stop a lot
		// of interface bugs (IE, user spam clicking a menu button)
		stateManager.addPossibleState(new State("checkWaitingGameScreen") {
			@Override
			public void logicUpdate(GameTime gameTime) {
				if (waitingGameScreen != null) {
					setTransitionStateName("fadeOut");
				} else {
					setTransitionStateName(getName());
				}
			}
		});

		stateManager.setStartingState("changeCurrentGameScreen");
	}

	// ----------------------------------------------------------------
	// Methods :: IRenderUpdateDelegator overrides
	// ----------------------------------------------------------------
	/**
	 * iterate through each object and call its update method
	 * 
	 * Passing the time through the method signature inspired by C# XNA
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		stateManager.logicUpdate(gameTime);

		// Perform logic updates on our event handling system. We do this so it
		// will fire any events that were queued between game loops.
		keyEventHandler.logicUpdate(gameTime);
		mouseEventHandler.logicUpdate(gameTime);

		getCurrentGameScreen().logicUpdate(gameTime);
	}

	/**
	 * Begin the delegation of drawing to the current game screen. This method
	 * firstly clears the drawing screen to white, passes the graphic object to
	 * the current game layer that needs it, and then applys the fade in/out
	 * effect if it's needed afterwards
	 * 
	 * @param drawScreen
	 *            The Graphics2D object for drawing to the screen
	 */
	@Override
	public void draw(Graphics2D drawScreen) {
		// Draw a white background first. This won't be needed if the graphics
		// of a layer cover the entire screen. But I am keeping this as a lot of
		// my tests of the game engine don't cover the entire screen, and it
		// would be annoying to the end user if they didn't know.
		drawScreen.setColor(Color.white);
		drawScreen.fillRect(0, 0, GameSettings.getGameWidth(), GameSettings.getGameHeight());

		// Draw the current game screen which is activated by this
		// GameScreenManager
		getCurrentGameScreen().draw(drawScreen);

		// Draw the transition colour if we are in a state of fading in or out
		if (transitionColor != null) {
			// drawScreen.setColor(Color.black);
			drawScreen.setColor(transitionColor);
			drawScreen.fillRect(0, 0, GameSettings.getGameWidth(), GameSettings.getGameHeight());
		}
	}

	// ----------------------------------------------------------------
	// Methods :: Adders
	// ----------------------------------------------------------------
	/**
	 * <p>
	 * This method adds a new GameScreen to the list of possible game screens
	 * that can be added.
	 * </p>
	 * <p>
	 * <b>This method relies on an inputed string name rather than relying on
	 * the actual gameScreen's name, this is for developing purposes, and can be
	 * changed within the future. For now I will overload this method in case I
	 * forget to change it.</b>
	 * </p>
	 * 
	 * @param name
	 * @param gameScreen
	 */
	public void addGameScreen(String name, GameScreen gameScreen) {
		if (gameScreens.containsKey(name)) {
			logger.error(new Exception(), "Duplicated gamescreen name! Screen already exists\n-- DETAILS --\n", gameScreens.get(name));
		} else {
			gameScreens.put(name, gameScreen);
		}
	}

	/**
	 * <p>
	 * This method adds the gameScreen to the list of possible game screens that
	 * the screen manager can control.
	 * </p>
	 * <p>
	 * <b>This method relies on the GameScreen's name for managing
	 * information.</b>
	 * </p>
	 * 
	 * @see addGameScreen
	 */
	public void addGameScreen(GameScreen gameScreen) {
		if (gameScreens.containsKey(gameScreen.getName())) {
			logger.error(new Exception(), "Duplicated gamescreen name! Screen already exists\n-- DETAILS --\n", gameScreen.getName());
		} else {
			gameScreens.put(gameScreen.getName(), gameScreen);
		}
	}

	// ----------------------------------------------------------------
	// Methods :: Misc
	// ----------------------------------------------------------------
	/**
	 * The colour that will be used for transitionining between screens, after
	 * setGameScreen has been called.
	 * 
	 * @param color
	 *            The desired colour, this should be in the format of RGB. IE,
	 *            0xFF000 for red. It should <b>not</b> contain an alpha
	 * @return
	 */
	public void setTransitionColor(int color) {
		desiredTransitionColor = color;
	}

	/**
	 * @return The GameScreen object that is currently being delegated the
	 *         update/draw methods
	 */
	public GameScreen getCurrentGameScreen() {
		return currentGameScreen;
	}

	/**
	 * 
	 * @param gameScreenName
	 *            The name of the game screen which needs to be shown next.
	 *            After this is called, the GameScreenManager will begin to fade
	 *            out, switch to the required game screen, then fade back in
	 */
	public void setGameScreen(String gameScreenName) {
		if (gameScreens.containsKey(gameScreenName)) {
			waitingGameScreen = gameScreens.get(gameScreenName);
		} else {
			logger.error(new NullPointerException(), "Attempted to change the game screen to a screen that doesn't exist! Name :: ", gameScreenName);
		}
	}

	/**
	 * Get all of the toString methods of all the added children.
	 */
	public void outputAllGameScreenStatistics() {
		for (GameScreen gameScreen : gameScreens.values()) {
			logger.info(gameScreen.toString());
		}
	}
}