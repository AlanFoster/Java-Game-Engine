package engine.main;

import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import engine.interfaces.IRenderUpdateDelegator;
import engine.main.GameTime.UpdateType;
import engine.misc.GameLogging;
import engine.misc.GameSettings;
import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;
import eventhandling.KeyEventHandler;
import eventhandling.MouseEventHandler;

/**
 * This class can be considered the heart beat of any game. It manages the
 * logicalUpdate, draw cycles. It currently delegates the task of
 * drawing/updating to the {@link GameScreenManager} class, as really such
 * things shouldn't be within this GameEngine class.
 * <p>
 * It it here that the event handler is instantiated, {@link GameEventHandler}.
 * I have decoupled this GameEngine from many of the systems offered by this
 * GameEngine. This class offers a singleton getInstance method, but it is
 * expected that there will be no need for this so it is annotated as
 * <code>@Deprecated</code>
 * <p>
 * This game engine has been developed drawing inspiration from Philip Hanna's
 * Java Game Engine (http://www.cs.qub.ac.uk/~
 * P.Hanna/CSC2007/JavaCodeRepository.htm) and after being influenced by what
 * XNA has to offer during the lectures I have implemented similar concepts and
 * offered similar classes throughout this game engine. Which is how the
 * {@link GameAssetManager} was created on, with the difference of the generics
 * being different, due to Java's poor implementation of generics with type
 * erasures to allow java to support backwards compatibility and interact with
 * legacy code unfortunately.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameScreenManager
 * @See {@link GameTime}
 */
@SuppressWarnings("serial")
public class GameEngine extends JFrame {
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
	private final static GameLogging logger = new GameLogging(GameEngine.class);

	/**
	 * <p>
	 * The game engine will actually work with versions less than 1.6
	 * (Originally 1.7 though :( )
	 * </p>
	 * <p>
	 * Not that this field actually makes any difference anyway, as the JVM will
	 * stop the game running if it's not 1.6 anyway, for example :: <br>
	 * Exception in thread "main" java.lang.UnsupportedClassVersionError:
	 * src/Main : Unsupported major.minor version 51.0
	 * </p>
	 */
	private final static float REQUIRED_JRE = 1.6f;

	// ----------------------------------------------------------------
	// Graphic Fields
	// ----------------------------------------------------------------

	/**
	 * The desired updates per second that the game loop will try to achieve. In
	 * order to more effectively achieve this, make sure to read the comments
	 * for MAX_NO_LOGIC_ONLY
	 */
	static long DESIRED_UPDATES_PER_SEC = 60;

	/**
	 * The maximum number of logical only cycles performed when running slow. It
	 * is hard to choose a 'correct' value for this, as if it is too low and we
	 * will fail to achieve our expected FPS in the long run - this is because
	 * logically we are running slow and we are not dropping the renders
	 * required to get back on track.
	 * <p>
	 * If it is important that you hit the required updates per second, increase
	 * this. This is because the maximum amount of updates per second is defined
	 * as being the minimum of desired updates per second and renders per second
	 * if it is 0.
	 */
	static int MAX_NO_LOGIC_ONLY = 0;

	/**
	 * Stores access to the GameSettings class. It is here that we store the
	 * gameWidth, height, bit depth debugging mode etc. I have provided a level
	 * of security to make sure that only the GameEngine has write access, and
	 * every other class has read access.
	 */
	protected static GameSettings.Settings gameSettings;

	/**
	 * This field is only used when 'developing' and not running in FSEM.
	 * There's the potential for this to be used when the user does not have a
	 * FSEM support for the desired width, height too.
	 */
	private static BufferedImage nonFSEMBufferedImage;

	/**
	 * Used when drawing FSEM, a great link ::
	 * http://download.oracle.com/javase/tutorial/extra/fullscreen/index.html
	 */
	private static GraphicsDevice graphicsDevice;
	/**
	 * Store the bufferStrategy since we're going to be accessing it a lot when
	 * drawing within FSEM
	 */
	private static BufferStrategy bufferStrategy;

	/**
	 * Allow for the ability to draw to the Buffer/BufferedImage (if non FSEM).
	 * Best to allocate the memory for this once
	 */
	private static Graphics2D drawScreen;

	/**
	 * The singleton instance of the game engine.
	 */
	private static GameEngine instance;

	// ----------------------------------------------------------------
	// Managers/Factory fields
	// ----------------------------------------------------------------
	protected static GameScreenManager gameScreenManager;

	// ----------------------------------------------------------------
	// Event handling
	// ----------------------------------------------------------------
	/**
	 * Get the EventHandler that handles our mouse events. We use this in the
	 * constructor so that we can use addMouseListener and
	 * addMouseMotionListener, so that all mouse events will be given to the
	 * handler.
	 **/
	protected final static MouseEventHandler mouseEventHandler = MouseEventHandler.getInstance();

	/**
	 * Get the EventHandler that handles our key events. We use this in the
	 * constructor so that we can use addKeyListener, so that all mouse events
	 * will be given to the handler.
	 */
	protected final static KeyEventHandler keyEventHandler = KeyEventHandler.getInstance();

	// ----------------------------------------------------------------
	// Misc fields
	// ----------------------------------------------------------------
	/**
	 * States whether or not we are currently running. For instance if a method
	 * crashes, they will most likely set this to false and stop the
	 * render/update cycle. It is volatile to allow for threaded access.
	 */
	protected volatile boolean running = true;

	/**
	 * Decides whether or not we are in 'developing' mode. Currently developing
	 * mode only turns off FSEM. But I'm sure it could be allowed to introduce
	 * specific functions that only need to be available to the developer
	 */
	protected boolean developing;

	/**
	 * This will be set to true either when the developing boolean is set to
	 * true, OR, when setting FSEM has failed (ie, when the user's graphics
	 * device does not support the required displaymode) and the developer has
	 * set 'fsemRequired' to false, allowing for non-fsem mode to initiate.
	 */
	protected boolean runningNonFsem;

	/**
	 * Stores the GameTime object, which keeps track of the elapsed time of game
	 * loops etc. Similar to C#'s implementation
	 */
	private GameTime gameTime;

	/**
	 * The GameEngine will invoke the method calls of rendering and updating
	 * during the main game loop method. It should be noted that this field is
	 * protected, and as there is no methods provided to set this field, that an
	 * extending class should change it if required.
	 * 
	 * @see IRenderUpdateDelegator
	 */
	protected IRenderUpdateDelegator renderUpdateDelegator;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * All fields that will be used are initialized during this constructor. It
	 * is during this method that the IRenderUpdateDelegator is initialised too.
	 * It should be noted that there is currently no method provided to allow
	 * for the external changing of this delegator, however it is a protected
	 * field so any extending classes will be allowed to change it as they wish
	 * </p>
	 * <p>
	 * After this object has been created the user should call startGame() with
	 * the width, height and required bitdepth and it will then attemp to
	 * initiate FSEM.
	 * </p>
	 * The access type of this method is protected, this means that only the
	 * extending class will be able to create an instance of it. This shouldn't
	 * be a problem to a developer. This allows us to, hopefully, implement a
	 * singleton for this class.
	 */
	protected GameEngine() {
		// See my notes on REQUIRED_JRE about this
		if (Float.parseFloat(System.getProperty("java.version").substring(0, 3)) < REQUIRED_JRE) {
			logger.error(Helpers.concat("Invalid Java Version! ", REQUIRED_JRE, " or greater only"));
			Runtime.getRuntime().exit(0xDEADBEEF);
		}

		// Set up our 'singleton'
		instance = this;

		new GameSettings(this);
		// Create an instance of our GameSettings
		gameSettings = GameSettings.getInstance(this);

		gameScreenManager = new GameScreenManager();

		// Set the renderUpdateDelagotor to be the gameScreenManager
		// I'm not sure if i should create a method for this so it can be
		// overridden, used, or whatever.
		renderUpdateDelegator = gameScreenManager;

		// set up listeners for the eventHandler
		// addKeyListener(eventHandler);
		// addMouseListener(eventHandler);
		// addMouseMotionListener(eventHandler);
		// logicalMouseEventHandler = new NewGameEventHandler();
		addKeyListener(keyEventHandler);
		addMouseListener(mouseEventHandler);
		addMouseMotionListener(mouseEventHandler);

		// Instantiate the GameTime class
		gameTime = new GameTime();

		// Registers a new virtual-machine shutdown hook
		// Called when the program exits normally ie when the vm is terminated,
		// the user log offs or system shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutdown hook called");
				if (running) {
					restoreScreen();
					running = false;
				}
			}
		});
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Returns the current instance of the game engine. This isn't exactly the
	 * singleton pattern, but it allows us to do similar things. It is really
	 * suggested that no class uses this method however, as this GameEngine
	 * should be decoupled enough from the other systems for there to be no need
	 * for any class to have access to the GameEngine directly.
	 */
	@Deprecated
	public static GameEngine getInstance() {
		return instance;
	}

	/**
	 * Returns the current game screen manager
	 * 
	 * @return The current render update delegator
	 * 
	 */
	public static GameScreenManager getGameScreenManager() {
		return gameScreenManager;
	}

	/**
	 * <p>
	 * Begins to the start the game, either in FSEM or non FSEM. This method
	 * will ensure that the user's graphic device supports the required game
	 * width and height before initiating this however.
	 * </p>
	 * <p>
	 * If the user's graphic device does not support the required width/height,
	 * but your game only supports the specific width/height, setting @param
	 * fsemRequired to false will start the game in non-fsem instead.
	 * </p>
	 * This is a private method as it has the additional 'fsemFailed' parameter
	 * added to it.
	 * 
	 * @param gameWidth
	 *            The game width that the screen should be set to
	 * @param gameHeight
	 *            The game height that the screen should be set to
	 * @param gameDepth
	 *            The bit depth of the game
	 * @param fsemRequired
	 *            If FSEM required is true it will attempt to start FSEM, and if
	 *            it fails it will start run the game without being in FSEM
	 *            anymore. If it is false, and FSEM wasn't successfully started,
	 *            it will end the program.
	 * @param fsemFailed
	 *            If we attempted to start FSEM and failed, this value will be
	 *            true. By default it should obviously be false. We use this so
	 *            we can recursively call startGame to repeat our startGame
	 *            logic with the new addition of fsemFailed being true.
	 */
	private void startGame(int gameWidth, int gameHeight, int gameDepth, boolean fsemRequired, boolean fsemFailed) {
		// Call the start up of our renderUpdateDelegator.
		// It may need some information of its own.
		// for instance GameScreenManager will throw an exception if no starting
		// GameScreen has been specified
		renderUpdateDelegator.startUp();

		try {
			// Remove decorations for the frame, remove the ability to resize
			// the window (somewhat unneeded as decorations are removed), and
			// setIgnoreRepaint to true as we are actively rendering, and we
			// don't want swing elements messing with our drawing!
			setUndecorated(true);
			setResizable(false);
			setIgnoreRepaint(true);

			// If we are 'developing' ie running in non-fsem mode
			if (developing || fsemFailed) {
				// Prepare the buffered image to allow us to draw to it during
				// the render cycle
				nonFSEMBufferedImage = new BufferedImage(gameWidth, gameHeight, BufferedImage.TYPE_INT_RGB);
				setSize(gameWidth, gameHeight);
				setVisible(true);

				// Either we're developing or fsem failed, so set nonfsem to
				// true
				runningNonFsem = true;
				// Update the logger to say that although we failed initally
				// setting fsem, we were still able to start up non-fsem :)
				if (!fsemRequired && fsemFailed) {
					logger.info("Setting non-FSEM was successful!");
				}
			} else if (!fsemFailed && !initialiseFullScreen()) {
				logger.error("Cannot initialise full screen mode.");
				restoreScreen();

				// Okay, so we've failed to set FSEM. Check if the developer
				// wanted the ability to still play this game in nonfsem
				if (!fsemRequired) {
					logger.info("fsemRequired was falsed, attempting to start nonfsem!");
					// Call the same method, but with fsem having failed.
					// This won't cause recursion as the requirement for entry
					// into this is fsemFailed being false
					startGame(gameWidth, gameHeight, gameDepth, fsemRequired, true);
				} else {
					logger.info("Graphics device did not support display mode, and requiredFSEM was true.",
							"Not attempting to start non-fsem");
				}
				return;
			}
			// start the update/render process thread
			new Thread(new GameUpdateCycle()).start();

			setDefaultCloseOperation(EXIT_ON_CLOSE);
			// Set the game widths/height/gameDepth, so that other classes can
			// access it as desired
			gameSettings.gameWidth = gameWidth;
			gameSettings.gameHeight = gameHeight;
			gameSettings.gameDepth = gameDepth;
		} catch (Exception e) {
			logger.error(e, "Crashed within startGame method");
			Runtime.getRuntime().exit(0xDEADBEEF);
		}
	}

	/**
	 * <p>
	 * Begins to the start the game, either in FSEM or non FSEM. This method
	 * will ensure that the user's graphic device supports the required game
	 * width and height before initiating this however.
	 * </p>
	 * <p>
	 * If the user's graphic device does not support the required width/height,
	 * but your game only supports the specific width/height, setting @param
	 * fsemRequired to false will start the game in non-fsem instead.
	 * </p>
	 * 
	 * @param gameWidth
	 *            The game width that the screen should be set to
	 * @param gameHeight
	 *            The game height that the screen should be set to
	 * @param gameDepth
	 *            The bit depth of the game
	 * @param fsemRequired
	 *            If FSEM required is true it will attempt to start FSEM, and if
	 *            it fails it will start run the game without being in FSEM
	 *            anymore. If it is false, and FSEM wasn't successfully started,
	 *            it will end the program.
	 */
	public void startGame(int gameWidth, int gameHeight, int gameDepth, boolean fsemRequired) {
		startGame(gameWidth, gameHeight, gameDepth, fsemRequired, false);
	}

	/**
	 * Replaces the current mouse cursor image with that which is given to it,
	 * with a hotspot of 0,0
	 * 
	 * @param image
	 *            The image which should now be the cursor
	 */
	public static void setCustomCursor(BufferedImage image) {
		setCustomCursor(image, 0, 0);
	}

	/**
	 * Replaces the current mouse cursor image with that which is given to it
	 * with the required X,Y hotspot
	 * 
	 * @param image
	 *            The image which should now be the cursor
	 * @param x
	 *            The X location of the mouse hotspot
	 * @param y
	 *            The Y location of the mouse hotspot
	 */
	public static void setCustomCursor(BufferedImage image, int x, int y) {
		instance.setCursor(instance.getToolkit().createCustomCursor(image, new Point(x, y), "mouseCursor"));
	}

	/**
	 * Attempts to initialize full screen mode. Firstly checks whether or not
	 * the graphics device supports full screen, then attempts to verify whether
	 * or not the desired dimensions are offered by the graphics device
	 * 
	 * @return true if the graphic card supports the required FSEM dimensions;
	 *         False if either the graphics device is not full screen supported,
	 *         or the dimensions were not available for FSEM
	 * 
	 */
	private final boolean initialiseFullScreen() {
		graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		if (!graphicsDevice.isFullScreenSupported()) {
			logger.error("Full screen not supported by graphics device");
			return false;
		}

		graphicsDevice.setFullScreenWindow(this);
		if (setDisplayMode(gameSettings.gameWidth, gameSettings.gameHeight, gameSettings.gameDepth)) {
			setBufferStrategy();
			return true;
		}

		logger.error("Display mode not offered for FSEM mode");
		return false;
	}

	/**
	 * Attempts to set the the display mode to the required width, height and
	 * bit depth. This method will check whether or not it is valid to change
	 * the display modes, and that the graphics card supports the display mode.
	 * 
	 * @param width
	 *            The width we want to set the display mode to
	 * @param height
	 *            The height we want to set the display mode to
	 * @param bitDepth
	 *            The required bitdepth that we wish to set the display mode to
	 * @return Returns true if the graphics device supports display change, and
	 *         display mode is available. Returns false if either display change
	 *         not supported or display mode not available
	 */
	private final boolean setDisplayMode(int width, int height, int bitDepth) {
		if (!graphicsDevice.isDisplayChangeSupported() || !isDisplayModeAvailable(width, height, bitDepth)) {
			logger.info("Display change not supported, or display mode not available");
			return false;
		} else {
			graphicsDevice.setDisplayMode(new DisplayMode(width, height, bitDepth, DisplayMode.REFRESH_RATE_UNKNOWN));
		}
		return true;
	}

	/**
	 * Checks whether or not the graphics device supports the display mode
	 * 
	 * @param width
	 *            The width we want to set the display mode to
	 * @param height
	 *            The height we want to set the display mode to
	 * @param bitDepth
	 *            The required bitdepth that we wish to set the display mode to
	 * @return true if the graphics device matches the width, height and bit
	 *         depth, false if one of these parameters is false
	 */
	private final boolean isDisplayModeAvailable(int width, int height, int bitDepth) {
		for (DisplayMode mode : getDisplayModes()) {
			// Can't use mode.equals() with a new DisplayMode object
			// This is because DisplayMode.equals(dm) compares refreshRate()
			if (width == mode.getWidth() && height == mode.getHeight() && bitDepth == mode.getBitDepth()) {
				return true;
			}
		}
		logger.info("couldn't find any display modes for :: width :",
				width, ", height :", height, ", bit depth :", bitDepth);
		return false;
	}

	/**
	 * returns an array of DisplayMode. This has been added as a public method
	 * so that other classes can access the list of display modes. This means
	 * that if needed, the user can select a different display mode possibly,
	 * without having access to the graphicsDevice directly.
	 */
	public final DisplayMode[] getDisplayModes() {
		return graphicsDevice.getDisplayModes();
	}

	/**
	 * Creates a new strategy for multi-buffering on this component.
	 * Multi-buffering is useful for rendering performance. This method attempts
	 * to create the best strategy available with the number of buffers
	 * supplied. This buffering strategy will use 2 buffers
	 */
	private final void setBufferStrategy() {
		try {
			createBufferStrategy(2);
			Thread.sleep(1000);
		} catch (Exception e) {
			logger.error(e, "Couldn't set buffer strategy");
			Runtime.getRuntime().exit(0xDEADBEEF);
		}
		bufferStrategy = getBufferStrategy();
	}

	/**
	 * If we are in FSEM we will attempt to remove the displayed mode
	 */
	private void restoreScreen() {
		if (graphicsDevice != null) {
			Window window = graphicsDevice.getFullScreenWindow();
			if (window != null) {
				window.dispose();
			}
			graphicsDevice.setFullScreenWindow(null);
		}
	}

	// ----------------------------------------------------------------
	// Draw Method
	// ----------------------------------------------------------------
	/**
	 * This method is called during each iteration of the game cycle.
	 * 
	 * For information on the rendering method see
	 * http://download.oracle.com/javase
	 * /1.4.2/docs/api/java/awt/image/BufferStrategy.html
	 */
	private void draw() {
		try {
			// Get the graphics of either the bufferedImage for non-FSEM or the
			// buffer strategy for FSEM
			drawScreen = (Graphics2D) (runningNonFsem ? nonFSEMBufferedImage.getGraphics() : bufferStrategy.getDrawGraphics());
			// Hand over drawing to our renderUpdateDelator, which will take
			// care of whatever it needs to draw
			renderUpdateDelegator.draw(drawScreen);

			if (runningNonFsem) {
				this.getGraphics().drawImage(nonFSEMBufferedImage, 0, 0, null);
			} else if (!bufferStrategy.contentsLost()) {
				// If the drawing buffer hasn't been lost since the last call
				bufferStrategy.show();
			}

			// fixes AWT event queue problems with linux, which causes speed
			// issues
			Toolkit.getDefaultToolkit().sync();

			// Dispose of screen graphic to clear resources
			drawScreen.dispose();
		} catch (Exception e) {
			logger.error(e, "Crashed within drawing");
			Runtime.getRuntime().exit(0xDEADBEEF);
		}
	}

	// ----------------------------------------------------------------
	// Update cycle
	// ----------------------------------------------------------------
	private final class GameUpdateCycle implements Runnable {
		@Override
		public void run() {
			/**
			 * The amount of time required to spend in order to achieve the
			 * desired FPS.
			 */
			long updateTime = 1000000000 / DESIRED_UPDATES_PER_SEC;

			/**
			 * sleepTime : The calculation of the time required to sleep to
			 * maintain a constant FPS. If this is negative, it means we're
			 * running slow and we should logically update until we either get
			 * back on track, or the maximum amount of updates without drawing
			 * is reached (MAX_NO_DRAWS)
			 */
			long sleepTime;

			/**
			 * Our sleep debt is changed either when we went to sleep after a
			 * fast logic update, and we overshot (or undershot) or expected
			 * sleep time. It is also increased by a number between 0 and
			 * updateTime when it is running slow, as we must factor in this
			 * difference for our next loop
			 */
			long sleepDebt = 0;

			/**
			 * Stores the time measured in nanosecoconds before it began to
			 * sleep when it was running fast.
			 */
			long timeBeforeSleeping;

			try {
				while (running) {
					// Update the GameTime instance to keep track of the current
					// time.
					gameTime.updateTime(System.nanoTime(), UpdateType.RENDER_AND_LOGIC);

					// Perform the basic update/draw cycle
					renderUpdateDelegator.logicUpdate(gameTime);
					draw();
					sleepTime = sleepDebt + updateTime - (System.nanoTime() - gameTime.previousGameTime);

					// if sleepTime is positive, that means there is no
					// correction to do, and we can just sleep as expected
					// Currently this game loop does not correct sleep periods
					// that over/under ran
					if (sleepTime > 0) {
						timeBeforeSleeping = System.nanoTime();
						Thread.sleep(sleepTime / 1000000);

						// offset and add that to our 'overshoot', or sleep debt
						// which we will try to get back next time.
						sleepDebt = -((System.nanoTime() - timeBeforeSleeping) - sleepTime);
					} else {
						/*
						 * If we're running slow, we perform a loop of only
						 * logical updates, and disregard render updates until
						 * we get back on track, it also breaks out of this loop
						 * we looped too many times without the
						 * MAX_NO_LOGIC_ONLY limit has been exceeded (the total
						 * amount of updates with no renders) Keep track of our
						 * current skips
						 */
						for (int logicOnlyCount = 0; sleepTime < 0 && logicOnlyCount < MAX_NO_LOGIC_ONLY; logicOnlyCount++) {
							gameTime.updateTime(System.nanoTime(), UpdateType.LOGIC_ONLY);
							sleepTime += updateTime;
						}

						// If we've gotten back on target, we may have gotten an
						// expected time between 0 and updateTime which we will
						// try to fix on the next game loop
						sleepDebt = sleepTime;
					}
				}
			} catch (Exception e) {
				logger.error(e, "Crashed within run() game loop");
			} finally {
				Runtime.getRuntime().exit(0xDEADBEEF);
			}
		}
	}
}
