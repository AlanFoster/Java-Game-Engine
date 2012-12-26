package engine.main;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import engine.interfaces.IDrawableLogical;
import engine.interfaces.ILogical;
import engine.interfaces.INameable;
import engine.interfaces.IRenderUpdateDelegator;
import engine.misc.GameLogging;
import eventhandling.IKeyboardEventable;
import eventhandling.IMouseEventable;
import eventhandling.KeyEventHandler;
import eventhandling.MouseEventHandler;

/**
 * A 'Game Screen' is an individual screen that can be shown at once. It will
 * manage game layers and invoke the render/logic methods if they are active.
 * <p>
 * This method implements {@link IRenderUpdateDelegator} so it is possible for
 * this object to be registered to the GameEngine for delegation.
 * <p>
 * This class makes use of a LinkedHashMap for storing the GameLayers that can
 * be shown. This means that the order in which GameLayers are added is the
 * order in which they will receive draw/logical invokes. This removes the need
 * to deal with weird collections which order GameLayers when adding to the
 * GameScreen.
 * <p>
 * The hierarchy for GameScreens and GameLayers can be shown below can be shown
 * as below
 * 
 * <pre>
 * [GameScreen] -> {@link GameLayer} GameLayer 
 * 							-> {@link IDrawableLogical} Objects
 * 				-> {@link GameLayer} GameLayer 
 * 							-> {@link IDrawableLogical} Objects
 * [GameScreen] -> {@link GameLayer} GameLayer  
 * 							-> {@link IDrawableLogical} Objects
 * </pre>
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
public class GameScreen implements IRenderUpdateDelegator, IKeyboardEventable, Observer, ILogical, INameable {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameScreen.class);

	// ----------------------------------------------------------------
	// Event Handling
	// ----------------------------------------------------------------
	/**
	 * This EventHandler receives AWT mouse events and stores them for the next
	 * time the game loop's logicUpdate is fired. It will then call all of the
	 * relevant event methods through all objects that have registered with this
	 * system (Requires the {@link IMouseEventable} interface to be
	 * implemented).
	 * <p>
	 * In order to allow for extending classes to have access to this static
	 * field we have made this protected.
	 */
	protected final static MouseEventHandler mouseEventHandler = MouseEventHandler.getInstance();

	/**
	 * This EventHandler receives AWT key events and stores them for the next
	 * time the game loop's logicUpdate is fired. It will then call all of the
	 * relevant event methods through all objects that have registered with this
	 * system (Requires the {@link IKeyboardEventable} interface to be
	 * implemented).
	 * <p>
	 * In order to allow for extending classes to have access to this static
	 * field we have made this protected.
	 */
	protected final static KeyEventHandler keyEventHandler = KeyEventHandler.getInstance();

	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * A map of the gameLayers registered to this GameScreen. Once registered
	 * the render/update methods of this object will be invoked directly. This
	 * gameLayers field will be instantiated using a LinkedHashMap which
	 * preserves the insertion-order. This means that rendering/updating will
	 * occur based on the order it is entered in.
	 */
	protected Map<String, GameLayer> gameLayers;
	/**
	 * Stores a list of the gameLayer names that need removed. If the layer is
	 * registered to this GameScreen it should set the remove flag with
	 * setNeedsRemoved(). There is also a method called removeLayer() which will
	 * do the same thing
	 */
	protected List<GameLayer> removeLayers;

	/**
	 * All assets must have a name when being registered with the system. This
	 * field stores the name as required. This is a unique identifier to
	 * identify this GameScreen name
	 */
	protected final String name;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new GameScreen which will delegate rendering/update invokes
	 * 
	 * @param gameEngine
	 *            used for game engine dependancy injection
	 * @param name
	 *            the name given to this game layer
	 */
	protected GameScreen(String name) {
		// set the required name
		this.name = name;

		// Create the linekd hashmap for game layers, which is used to keep the
		// logical/draw updates in the correct order. IE the order they are
		// added to the system is the order in which they recieve updates
		gameLayers = new LinkedHashMap<String, GameLayer>();

		// Create the linekd hashmap for game layers, which is used to keep the
		// logical/draw updates in the correct order. IE the order they are
		// added to the system is the order in which they recieve updates
		removeLayers = new LinkedList<GameLayer>();
	}

	// ----------------------------------------------------------------
	// Methods :: IRenderUpdateDelegator overrides
	// ----------------------------------------------------------------
	@Override
	public void startUp() {
		// This IRenderUpdateDelegator requires no startup
	}

	/**
	 * Iterates all of the game layers registered to this game screen and
	 * updates them if they are not paused. It is at this point any layers which
	 * need removed are removed.
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		deleteRequiredLayers();
		for (GameLayer gameLayer : gameLayers.values()) {
			if (gameLayer.currentState != GameLayer.GameLayerState.PAUSED) {
				gameLayer.logicUpdate(gameTime);

				// We poll this information to see if the game layer now wants
				// removed.
				if (gameLayer.needsRemoved()) {
					removeLayers.add(gameLayer);
				}
			}
		}
	}

	/**
	 * Iterates all of the registered game layers and allows for them to draw.
	 * The draw method will only be invoked if it is not a hidden game layer
	 */
	@Override
	public void draw(Graphics2D drawScreen) {
		// Pass in the gameScreen object to the child gamelayer, and it will
		// draw what it needs
		for (GameLayer gameLayer : gameLayers.values()) {
			if (gameLayer.currentState != GameLayer.GameLayerState.HIDDEN) {
				gameLayer.draw(drawScreen);
			}
		}
	}

	// ----------------------------------------------------------------
	// Methods :: ILogicalObject overrides
	// ----------------------------------------------------------------
	/**
	 * When this GameScreen is started it also passes this message on and
	 * invokes the startUp method of all the register game layers
	 * 
	 * @see ILogical
	 */
	@Override
	public void startUp(GameTime gameTime) {
		for (GameLayer gameLayer : gameLayers.values()) {
			gameLayer.startUp(gameTime);
		}

		keyEventHandler.registerEvents(this);
	}

	/**
	 * Called when the GameScreen is being replaced by a different GameScreen
	 * within the GameScreenManager. This method also invokes the same method of
	 * all of the children GameLayers. It is at this point that any events
	 * created are removed.
	 * 
	 * @see ILogical
	 */
	@Override
	public void cleanUp() {
		// Iterate all of the children layers and remove their events
		for (GameLayer gameLayer : gameLayers.values()) {
			gameLayer.cleanUp();
		}

		KeyEventHandler.getInstance().removeEvents(this);
	}

	// ----------------------------------------------------------------
	// Methods :: Misc
	// ----------------------------------------------------------------
	/**
	 * Iterates over the list of game layers that wish to be removed from this
	 * game screen. This method does not validate whether or not the gamelayer
	 * actually is registered to this method, as the removeGameLayer method does
	 * this already. And the only other place that adds to this List is when
	 * needsRemoved is set on a game layer, which I can assume makes it a valid
	 * game layer.
	 */
	private final void deleteRequiredLayers() {
		for (GameLayer layer : removeLayers) {
			gameLayers.remove(layer.getName());
		}
	}

	/**
	 * Removes a game layer at the start of the next logic update
	 * 
	 * @param gameLayer
	 *            The gamelayer which needs to be removed
	 */
	public final void removeGameLayer(GameLayer gameLayer) {
		if (!gameLayers.containsKey(name)) {
			logger.error(new NullPointerException(),
					"Died within removeGameLayer. There was no corresponding layer with the name :: ", name);
		} else {
			removeLayers.add(gameLayer);
		}
	}
	/**
	 * Registers a gamelayer to this gamescreen. Once registered it will call
	 * the update/render methods associated with the game layer.
	 * 
	 * @param gameLayer
	 */
	public final void addGameLayer(GameLayer gameLayer) {
		if (!gameLayers.containsKey(gameLayer.getName())) {
			gameLayers.put(gameLayer.getName(), gameLayer);
		} else {
			logger.error("The gamelayer name, ", gameLayer.getName(),
					" was not added to the game screen ", getName(),
					" as it already exists within this GameScreen");
		}
	}
	/**
	 * Returns the required gameLayer. If it not found it will throw a null
	 * point exception
	 * 
	 * @param name
	 *            The name of the gamelayer which is needed
	 * @return The game layer with the corresponding name. If the layer is not
	 *         found, null.
	 */
	public final GameLayer getGameLayer(String name) {
		GameLayer foundLayer = gameLayers.get(name);
		if (foundLayer == null) {
			logger.error(new NullPointerException(),
					"Died within getGameLayer. There was no corresponding layer with the name :: ", name);
			return null;
		}
		return foundLayer;
	}

	/**
	 * <p>
	 * Paused the specified game layer. Once paused it will no longer recieve
	 * logical updates.
	 * <p>
	 * <p>
	 * Note :: If the game layer is not registered with this GameScreen a null
	 * pointer exception will be given.
	 * </p>
	 * 
	 * @param layer
	 *            The layer which now longer needs logical updates.
	 */
	public final void pauseLayer(GameLayer layer) {
		if (!gameLayers.containsValue(layer)) {
			logger.error(new NullPointerException(),
					"Tried to pause a game layer that isn't registered to the GameScreen :: ", this);
		} else {
			layer.pause();
		}
	}

	/**
	 * <p>
	 * UnPauses the specified game layer. Once unpaused it will once again
	 * Receive logical updates
	 * </p>
	 * <p>
	 * Note :: If the game layer is not registered with this GameScreen a null
	 * pointer exception will be given.
	 * </p>
	 */
	public final void unpauseLayer(GameLayer layer) {
		if (!gameLayers.containsValue(layer)) {
			logger.error(new NullPointerException(),
					"Tried to unpause a game layer that isn't registered to the GameScreen :: ", this);
		} else {
			layer.unpause();
		}
	}

	/**
	 * <p>
	 * Hides a game layer. Once hidden it will no longer receive draw method
	 * calls
	 * </p>
	 * <p>
	 * Note :: If the game layer is not registered with this GameScreen a null
	 * pointer exception will be given.
	 * </p>
	 */
	public final void hideLayer(GameLayer layer) {
		if (!gameLayers.containsValue(layer)) {
			logger.error(new NullPointerException(),
					"Tried to hide a game layer that isn't registered to the GameScreen :: ", this);
		} else {
			layer.hide();
		}
	}

	/**
	 * <p>
	 * Shows a game layer. Once hidden it will once again receive draw method
	 * calls
	 * </p>
	 * <p>
	 * Note :: If the game layer is not registered with this GameScreen a null
	 * pointer exception will be given.
	 * </p>
	 */
	public final void showLayer(GameLayer layer) {
		if (!gameLayers.containsValue(layer)) {
			logger.error(new NullPointerException(),
					"Tried to unhide a game layer that isn't registered to the GameScreen :: ", this);
		} else {
			layer.unhide();
		}
	}

	/**
	 * The toString method provides the basic information of the system for
	 * debugging etc.
	 */
	@Override
	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append("GameScreen :: ")
				.append(getName())
				.append("\n");
		// Iterate over all of the registered game layers and append their
		// toString values
		for (GameLayer gameLayer : gameLayers.values()) {
			info.append(gameLayer.toString());
		}
		return info.toString();
	}

	/**
	 * This logic should be overridden by any extending GameScreen classes if
	 * required. It is the Observer interface implementation.
	 */
	@Override
	public void update(Observable o, Object arg) {

	}

	/**
	 * Any overriding GameScreens should implement their logic as required. All
	 * GameScreens receive key events by default.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
	}

	/**
	 * All assets must have a name when being registered with the system. This
	 * method returns the name as required
	 * 
	 * @see engine.interfaces.INameable#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
}
