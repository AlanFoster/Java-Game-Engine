package world;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Observable;

import src.Configuration;

import world.World.WorldState;

import engine.assets.GameGraphic;
import engine.main.GameEngine;
import engine.main.GameLayer;
import engine.main.GameScreen;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.managers.GameAssetManager;

/**
 * The world manager keeps track of the World and WorldParser. It first tells
 * the world parser to load all of the details required for the level, then
 * gives all of this information to the world to handle.
 * <p>
 * The world manager provides an overlay layer which sits above the world, so
 * that the world can be paused and an overlay can be provided. This will be
 * nice for pausing/end of game effects etc, as we can still see the world
 * beneath it.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class WorldManager extends GameScreen {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(WorldManager.class);

	/**
	 * Sets the level that will be loaded by this WorldManager once startUp is
	 * called
	 */
	private String levelRequired = "levelOne";

	/**
	 * A layer dedicated solely to the Entity system. Note, both logical and
	 * rendering systems will all occur at this point in time. It is within this
	 * that the world, players, enemies etc should be added (through the entity
	 * system)
	 */
	private World worldLayer;

	/**
	 * This is the 'overlay' for the game. This will be shown above the whatever
	 * the entity systems draw (usually players, enemies etc). This allows us to
	 * 'pause' the game, but still visibly show the world, or provide a nice
	 * effect when the player is killed, or when time has ran out. We can simply
	 * do this by stop logically updating the worldLayer, but continue rendering
	 * it. (This support is provided by the GameLayer class already)
	 */
	private GameLayer overlayLayer;

	/**
	 * Stores the current overlay image. IE when the player pauses the game,
	 * this will be a paused image, and when the player dies it will be a death
	 * image.
	 * <p>
	 * It is the pausedGameGradient by default, but this of course doesn't
	 * matter.
	 */
	private GameGraphic overlayGraphic;

	/**
	 * The WorldParser loads all of the XML files which contains all of the
	 * level information that is required. It also stores them for future use.
	 */
	private WorldParser worldParser;
	/**
	 * Set to true when the game is paused, and the world no longer recieves
	 * logical updates. Set to false when the game isn't paused, and the game
	 * now recieves logical updates.
	 */
	private boolean gamePaused;

	/**
	 * The 'game cursor' which will be used during the game, most likely it will
	 * be a cross hair of sorts
	 */
	private BufferedImage GAME_CURSOR;
	/**
	 * The original cursor that was found when first entering into the game.
	 * When the game is over/paused etc, it gets set back to this
	 */
	private BufferedImage PREVIOUS_CURSOR;

	/**
	 * Asset names for overlay screens when when the game is paused or the
	 * player has died\
	 */
	private static final String pausedOverlayName = Configuration.GUI.GameOverlay.PAUSED_OVERLAY;
	private static final String deathOverlayName = Configuration.GUI.GameOverlay.DEATH_OVERLAY;

	/**
	 * Creates a new world manager which loads in
	 */
	public WorldManager(String levelDetails, String levelRequired) {
		super("levelManager");

		worldLayer = new World();
		// Listen to any messages the world sends out, this will most likely be
		// messages such as the game ending
		worldLayer.addObserver(this);

		overlayLayer = new GameLayer("overlay");
		overlayGraphic = GameAssetManager.getInstance().getObject(GameGraphic.class, pausedOverlayName);
		overlayLayer.add(overlayGraphic);

		// overlayLayer.add(overlayGraphic);

		addGameLayer(worldLayer);
		addGameLayer(overlayLayer);

		this.levelRequired = levelRequired;

		// Load all of the maps for the world loader
		worldParser = new WorldParser(levelDetails);
	}

	/**
	 * Sets the level that will be loaded by this WorldManager once startUp is
	 * called
	 * 
	 * @param levelRequired
	 *            The level asset name name that has been registered and loaded
	 *            to the world parser
	 */
	public void setLevelRequired(String levelRequired) {
		this.levelRequired = levelRequired;
	}

	@Override
	public void startUp(GameTime gameTime) {
		GAME_CURSOR = GameAssetManager.getInstance().getObject(BufferedImage.class, "crosshair");
		PREVIOUS_CURSOR = GameAssetManager.getInstance().getObject(BufferedImage.class, "gameMouse");

		unpauseGame();

		// Load all of our level's data
		worldLayer.populateWorld(worldParser.getData(levelRequired));
		super.startUp(gameTime);
		// GameEngine.setCustomCursor(GAME_CURSOR);
	}

	/**
	 * Some basic shortcuts added :: - P to unpause/pause the game - Q to quit
	 * to main menu
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		/**
		 * If the P key is pressed, pause or unpause the game
		 */
		if (e.getKeyCode() == KeyEvent.VK_P) {
			if (gamePaused) {
				unpauseGame();
			} else {
				pauseGame();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_Q) {
			GameEngine.getGameScreenManager().setGameScreen("mainMenu");
		}
	}

	/**
	 * Pauses the game and shows the overlay layer
	 */
	private void pauseGame() {
		changeOverlayImage(pausedOverlayName);
		showLayer(overlayLayer);
		pauseLayer(worldLayer);
		gamePaused = true;
		GameEngine.setCustomCursor(PREVIOUS_CURSOR);
	}

	/**
	 * Unpauses the game and shows the overlay layer
	 */
	private void unpauseGame() {
		hideLayer(overlayLayer);
		unpauseLayer(worldLayer);
		gamePaused = false;

		// Change the cursor to the cross hair with the x,y hotspot being the
		// center of the cursor so that when the user shoots a bullet, it'll
		// intersect the center of the cross hair as expected
		GameEngine.setCustomCursor(GAME_CURSOR,
				GAME_CURSOR.getWidth() / 2,
				GAME_CURSOR.getHeight() / 2);
	}

	/**
	 * Called directly by the World instance when the timer has run out. Most
	 * likely by the HUDTimer that was created.
	 */
	protected void timeOver() {
		changeOverlayImage(pausedOverlayName);
		pauseGame();
	}
	/**
	 * Called when the world has changed its WorldState to PLAYER_DIED. Called
	 * from the observable update
	 */
	protected void playerDied() {
		changeOverlayImage(deathOverlayName);
		pauseGame();
	}

	/**
	 * Called when there is an update from the world that the level has been
	 * completed. Presumably it is here that we will load the next level
	 * 
	 * Called when the world has changed its WorldState to LEVEL_COMPLETED.
	 * Called from the observable update
	 */
	protected void levelCompleted() {
		// Pause the game
		pauseGame();
		// Show the level completed statistics
		// ... (not done) (if ever)
	}

	/**
	 * Changes the overlay graphic when the world is paused, ie on player death,
	 * time over etc
	 * 
	 * @param overlayImage
	 */
	protected void changeOverlayImage(String name) {
		overlayGraphic.setImage(GameAssetManager.getInstance().getObject(BufferedImage.class, name));
	}

	/**
	 * This is the Observer interface implementation. We listen to events sent
	 * from the World to see whether or the time is over, the player has died,
	 * or the level is completed etc.
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof WorldState) {
			WorldState worldState = (WorldState) arg;
			switch (worldState) {
			// When the timer hit 0
				case TIME_OVER:
					timeOver();
					break;
				// Set when the player has 0 health and 0 shield etc. IE, dead.
				case PLAYER_DIED:
					playerDied();
					break;
				// Set when the player completes the level
				case LEVEL_COMPLETED:
					// etc logic here
					break;
			}
		}
	}
}
