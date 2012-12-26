package engine.main;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import engine.interfaces.IDrawableLogical;
import engine.interfaces.IRemovable;
import engine.interfaces.IDrawable;
import engine.interfaces.ILogical;
import engine.interfaces.INameable;
import engine.interfaces.IObservable;
import engine.interfaces.IPositionable;
import engine.misc.GameLogging;
import engine.misc.GameQueue;
import engine.misc.GameSettings;
import engine.misc.GameViewPort;
import engine.misc.Helpers;
import engine.misc.Location;
import eventhandling.IKeyboardEventable;

/**
 * A game layer is registered to a GameScreen and will receive logical updates
 * from screen. This class will then pass the logical and drawing updates onto
 * any objects that require it. For objects that wish to recieve logical updates
 * they are required to implement the {@link ILogical} interface. For any
 * objects that wish to receive draw updates are required to implement
 * {@link IDrawable}. And Sadly if an object wishes to recieve both logical and
 * draw updates they should implement {@link IDrawableLogical} (See my notes on
 * this to see why I was forced to create this).
 * 
 * <pre>
 * [GameScreen] -> {@link GameLayer} GameLayer 
 * 							-> {@link IDrawableLogical} Objects
 * 				-> {@link GameLayer} GameLayer 
 * 							-> {@link IDrawableLogical} Objects
 * [GameScreen] -> {@link GameLayer} GameLayer  
 * 							-> {@link IDrawableLogical} Objects
 * </pre>
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GameLayer implements ILogical, IPositionable, INameable, Observer, IRemovable {
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
	private final static GameLogging logger = new GameLogging(GameLayer.class);

	/**
	 * The dimensions of the object. Private as the fields can be set through
	 * the interface, {@link IPositionable}.
	 */
	private Rectangle2D.Float dimensions = new Rectangle2D.Float();

	/**
	 * A GameQueue of logical objects which handles the problems with concurrent
	 * modification
	 * 
	 * @see GameQueue
	 */
	protected GameQueue<ILogical> logicalQueue;

	/**
	 * A list of drawable objects. Note, this is not a GameQueue as it is not
	 * expected for any objects to be added/removed from any lists during the
	 * drawing methods, as drawing is ONLY for drawing, no logical changes.
	 */
	protected List<IDrawable> drawableList;

	/**
	 * Stores the states that this layer can be under
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	protected enum GameLayerState {
		/**
		 * We recieve will perform both logical and graphic code
		 */
		ACTIVE,
		/**
		 * We will no longer perform and logical code
		 */
		PAUSED,
		/**
		 * We will no longer perform drawing
		 */
		HIDDEN;
	}

	/**
	 * The current state that this layer is in
	 */
	GameLayerState currentState = GameLayerState.ACTIVE;

	/**
	 * The viewport is conceptually viewed as 'the viewing' area within this
	 * game engine. Each GameLayer stores a different GameViewPort for
	 * flexibility, however many gamelayers may share the same reference to a
	 * single viewport.
	 */
	protected GameViewPort viewPort;

	/**
	 * Holds whether or not we need removed. IE, if we're no longer required to
	 * recieve logical/drawing updates
	 */
	protected boolean needsRemoved;

	/**
	 * All assets must have a name when being registered with the system. This
	 * field stores the name as required. This must be unique, and is enforced
	 * by the GameScreen class.
	 */
	protected final String name;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	/**
	 * Creates a new GameLayer object, and instantiates all queues/lists used
	 * within this GameLayer
	 * 
	 * @param name
	 *            An identifiable given to this GameLayer. This must be unique,
	 *            and is enforced by the GameScreen class.
	 */
	public GameLayer(String name) {
		this.name = name;

		// gameComponentList is a LinkedList so that specified drawing order can
		// exist.
		drawableList = new LinkedList<IDrawable>();
		logicalQueue = new GameQueue<ILogical>();

		viewPort = new GameViewPort(0, 0, GameSettings.getGameWidth(), GameSettings.getGameHeight());
	}

	/**
	 * Creates a new GameLayer object, with the required x,y and width height
	 * 
	 * @param name
	 *            An identifiable given to this GameLayer. This must be unique,
	 *            and is enforced by the GameScreen class.
	 * @param x
	 *            The X location of this GameLayer. All children registered to
	 *            this layer will be offset by this value
	 * @param y
	 *            The Y location of this GameLayer. All children registered to
	 *            this layer will be offset by this value
	 * @param width
	 *            The width of this game layer
	 * @param height
	 *            The height of this game layer
	 */
	public GameLayer(String name, int x, int y, int width, int height) {
		this(name);
		setDimensions(x, y, width, height);
	}
	/**
	 * Creates a new GameLayer object, with the required x,y. This constructor
	 * assumes a width/height of the current game setting's width and height
	 * respectively
	 * 
	 * @param name
	 *            An identifiable given to this GameLayer. This must be unique,
	 *            and is enforced by the GameScreen class.
	 * @param x
	 *            The X location of this GameLayer. All children registered to
	 *            this layer will be offset by this value
	 * @param y
	 *            The Y location of this GameLayer. All children registered to
	 *            this layer will be offset by this value
	 */
	public GameLayer(String name, int x, int y) {
		this(name, x, y, GameSettings.getGameWidth(), GameSettings.getGameHeight());
	}

	/**
	 * Creates a new GameLayer object with the required name, and an existing
	 * viewport that it will use
	 * 
	 * @param name
	 *            An identifiable given to this GameLayer. This must be unique,
	 *            and is enforced by the GameScreen class.
	 * @param viewPort
	 *            The reference to a game object that this GameLayer should use
	 */
	public GameLayer(String name, GameViewPort viewPort) {
		this(name);
		setViewPort(viewPort);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Sets the current game layer's viewport to the required viewport object.
	 * This allows for different game layers to share different or the same
	 * viewports
	 * 
	 * @param viewPort
	 */
	public void setViewPort(GameViewPort viewPort) {
		this.viewPort = viewPort;
	}

	/**
	 * Returns the current viewport associated with this game layer
	 * 
	 * @return
	 */
	public GameViewPort getViewPort() {
		return viewPort;
	}

	/**
	 * Adds an object to this game layer which will receive draw updates from
	 * this game layer
	 * 
	 * @param drawable
	 *            The new object which wants to receive draw method requests
	 */
	public void add(IDrawable drawable) {
		if (drawable == null) {
			logger.error(new NullPointerException(),
					"attempted to add null drawable object to GameLayer", this);
		}
		drawableList.add(drawable);
	}

	/**
	 * Adds an object to this game layer which will receive logical updates from
	 * this game layer
	 * 
	 * @param logical
	 *            The new logical object which wants to receive logical update
	 *            requests
	 */
	public void add(ILogical logical) {
		if (logical == null) {
			logger.error(new NullPointerException(),
					"attempted to add null logical object to GameLayer", this);
		}
		logicalQueue.add(logical);
	}

	/**
	 * See my notes within {@link IDrawableLogical} for details as to why this
	 * exists.
	 * 
	 * @param object
	 *            The object which is both logical and drawable
	 */
	public void add(IDrawableLogical object) {
		add((ILogical) object);
		add((IDrawable) object);
	}

	/**
	 * Removes a game object from this layer, either if its a logical or
	 * drawable child. I decided not to duplicate three versions of remove,
	 * similar to my three versions of add. See my notes on
	 * {@link IDrawableLogical} as to why it exists.
	 * 
	 * @param gameComponent
	 *            The object which is required to be removed
	 */
	public final void remove(Object gameComponent) {
		// Remove this object from our logical list if its a logical object
		if (gameComponent instanceof ILogical) {
			ILogical logicalObject = (ILogical) gameComponent;
			logicalQueue.remove(logicalObject);
			logicalObject.cleanUp();
		}

		// Remove this object from the drawable list, if it's a drawable object
		if (gameComponent instanceof IDrawable) {
			drawableList.remove((IDrawable) gameComponent);
		}
	}

	// ----------------------------------------------------------------
	// Drawing Methods
	// ----------------------------------------------------------------
	/**
	 * Draws all of the visible children added to this game layer, as long as
	 * this layer is not hidden
	 * 
	 * @param drawScreen
	 */
	protected void draw(Graphics2D drawScreen) {
		if (currentState != GameLayerState.HIDDEN) {
			// Draw visible children directly added to the layer
			for (IDrawable gameObject : getVisibleDrawable(drawableList, getX(), getY())) {
				gameObject.draw(drawScreen, (int) (getX() - viewPort.getX()), (int) (getY() - viewPort.getY()));
			}
		}
	}

	// Returns the list of objects that are within the viewport in the list
	// given Allows for extending classes to have access to this if they want
	// their own custom drawing/culling etc.
	/**
	 * Returns a list of all objects that are viewable within a the list of
	 * IDrawable that is passed in. This logic has been seperated to its own
	 * method in case you wish to override it, and for instance either create
	 * your own culling, OR - which is suggested - make use of the
	 * EntitySystem's rendering systems. If you wish to make use of the Entity
	 * rendering systems, make the layer that all Systems register to override
	 * and return allItems as required
	 * 
	 * @param allItems
	 *            Every IDrawable object that has been registered to this
	 *            GameLayer
	 * @param offsetX
	 *            The offsetX of this GameLayer
	 * @param offsetY
	 *            The Y offset of this game layer.
	 * @return A list of drawable children that need drawn to the drawscreen.
	 *         The draw() method will be called through the interface IDrawable
	 */
	protected List<IDrawable> getVisibleDrawable(List<IDrawable> allItems, float offsetX, float offsetY) {
		List<IDrawable> visibleItems = new LinkedList<IDrawable>();
		// Iterate through each game object and if its visible we return the the
		// object within the list
		for (IDrawable gameObject : allItems) {
			if (gameObject == null) {
				logger.error(new NullPointerException(), Helpers.concat("Null pointer within graphic culling. Game Layer name == ", getName()));
			}

			if (viewportContainsPositionable((IPositionable) gameObject)) {
				visibleItems.add(gameObject);
			}
		}
		return visibleItems;
	}

	/**
	 * @param gameObject
	 *            The positionable object that needs to be checked against the
	 *            boundaries of the viewport
	 * @return true if the viewport contains this postionable object, false if
	 *         it doesn't
	 */
	public boolean viewportContainsPositionable(IPositionable gameObject) {
		return viewPort.contains(gameObject.getDimensions());
	}

	/**
	 * @return Returns all of the drawble objects added to this gameLayer
	 **/
	public final List<IDrawable> getAllDrawableChildren() {
		return drawableList;
	}

	/**
	 * @return Returns all 'current' logical children added to this game layer.
	 *         IE, those receiving logical updates.
	 * 
	 */
	public final List<ILogical> getAllLogicalChildren() {
		return logicalQueue.getAllCurrent();
	}

	/**
	 * Returns an object based on its name when registered with the system. This
	 * method checks both drawable and logical children
	 * 
	 * @param name
	 *            The name of the object
	 */
	public final INameable getNamedObject(String name) {
		int logicalChildrenCount = getAllLogicalChildren().size();
		int namedChildrenCount = 0;
		for (ILogical logicalObject : getAllLogicalChildren()) {
			if (logicalObject instanceof INameable) {
				namedChildrenCount++;
			}
		}
		logger.info(logicalChildrenCount, " ", namedChildrenCount);
		return null;
	}

	// ----------------------------------------------------------------
	// Observer implementation
	// ----------------------------------------------------------------
	/**
	 * Observable pattern. We use this when listening to any children that may
	 * have implemented IDestroyable.
	 * <p>
	 * Note :: Due to any class that implements IObservable will be
	 * encapsulating (as the class is final) rather than extending, it is
	 * assumed that the arg is the object that encapsulated the object. So
	 * normally we might do if(obj instanceof Foo){} but as obj is only ever
	 * going to be an instance of Observable and GameOvevable, we can't do that,
	 * and we must check against the arg sent.
	 */
	@Override
	public void update(Observable obj, Object arg) {
		if (arg instanceof IRemovable) {
			if (((IRemovable) arg).needsRemoved()) {
				// Stop observing this object as we no longer care about it
				((IObservable) arg).deleteObserver(this);
				remove(arg);
			}
		}
	}

	// ----------------------------------------------------------------
	// Logical Methods
	// ----------------------------------------------------------------
	/**
	 * Logically updates all registered logical children as long as the layer is
	 * not paused
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		if (currentState != GameLayerState.PAUSED) {
			// Call the start up of any methods that just registered, and add
			// them to the logic update list
			startUpLogicalQueue(gameTime);

			// for (int i = 0, length = gameLogicalQueue.size(); i < length &&
			// !gameLogicalQueue.isEmpty(); i++) {
			// ILogicalObject gameObject = gameLogicalQueue.poll();

			for (ILogical logicalObject : logicalQueue) {
				logicalObject.logicUpdate(gameTime);
			}
		}
	}

	/**
	 * Iterates all logical children that are waiting to be added to this game
	 * layer, and calls their startUp method using the current GameTime object
	 * 
	 * @param gameTime
	 */
	private void startUpLogicalQueue(GameTime gameTime) {
		// Iterate and call the start ups of any logical children which are
		// waited to be added to the GameLayer
		for (ILogical logicalObject : logicalQueue.getAllWaiting()) {
			logicalObject.startUp(gameTime);
		}

		// Get the list of objects which wanted to be removed from the
		// GameQueue, they are now removed (instead of at the time, which
		// could've caused concurrent deletion problems)
		for (ILogical logicalObject : logicalQueue.getAllWaitingDeletion()) {
			logicalObject.cleanUp();
		}

		// Tell the GameQueue to clear its list of deleting, and move its list
		// of waiting to its list of current objects
		logicalQueue.updateLists();
	}

	@Override
	public void startUp(final GameTime gameTime) {
		// Call the start up methods of any children that previously existed on
		// the logicalQueue
		for (ILogical gameComponent : logicalQueue) {
			gameComponent.startUp(gameTime);
		}
		// Also call the start up methods of any children that were queued to be
		// added
		startUpLogicalQueue(gameTime);
	}

	/**
	 * Iterates all registered children and calls their clean up method. This
	 * method will be called by the GameScreen when it has been removed from the
	 * the system, or when the GameScreen changes. IE when a change is made to
	 * the GameScreen
	 */
	@Override
	public void cleanUp() {
		// Tell all registered children to perform their cleanup method
		for (ILogical gameComponent : logicalQueue) {
			gameComponent.cleanUp();
		}
	}

	// ----------------------------------------------------------------
	// State changes
	// ----------------------------------------------------------------
	/**
	 * Pauses the current GameLayer. When paused this game layer will no longer
	 * perform logical updates
	 */
	public final void pause() {
		currentState = GameLayerState.PAUSED;
	}

	/**
	 * Unpauses the current GameLayer. When unpaused it will recieve logical
	 * updates as expected
	 */
	public final void unpause() {
		currentState = GameLayerState.ACTIVE;
	}

	/**
	 * Changes the current state to be HIDDEN. This means that it will no longer
	 * perform any drawing ot the screen, and will in essence be 'hidden'
	 */
	public final void hide() {
		currentState = GameLayerState.HIDDEN;
	}

	/**
	 * Changes the current game layer so that it will no perform drawing updates
	 * to the screen as expected
	 */
	public final void unhide() {
		currentState = GameLayerState.ACTIVE;
	}

	/**
	 * Returns true if this GameLayer needs to be removed from the GameScreen
	 * that it is registered to. This information is polled by the GameScreen.
	 */
	@Override
	public boolean needsRemoved() {
		return needsRemoved;
	}

	/**
	 * Changes the needsRemoved boolean value to true, so that on the next game
	 * update this game layer will be removed from the game screen that it is
	 * registered to. 
	 */
	@Override
	public void removeMe() {
		needsRemoved = true;
	}

	// ----------------------------------------------------------------
	// INameable implementations
	// ----------------------------------------------------------------
	/**
	 * All assets must have a name when being registered with the system. This
	 * method returns the name as required
	 */
	@Override
	public String getName() {
		return name;
	}

	// ----------------------------------------------------------------
	// IPositionable implementations
	// ----------------------------------------------------------------
	/**
	 * The x position of the positionable object, with 0,0 being top left
	 */
	public float getX() {
		return dimensions.x;
	}

	/**
	 * Sets the X location of this object. This should be from the top left
	 * position.
	 */
	public void setX(float x) {
		dimensions.x = x;
	}

	/**
	 * The Y position of the positionable object, with 0,0 being top left
	 */
	public float getY() {
		return dimensions.y;
	}

	/**
	 * Set the Y postiion of the object, with 0,0 beign the top left
	 */
	public void setY(float y) {
		dimensions.y = y;
	}

	/**
	 * The width of this positionable object
	 */
	public float getWidth() {
		return dimensions.width;
	}

	/**
	 * Set the new width of this object
	 */
	public void setWidth(float width) {
		dimensions.width = width;
	}

	/**
	 * get the height of this object
	 */
	public float getHeight() {
		return dimensions.height;
	}

	/**
	 * Change the height of this object
	 */
	public void setHeight(float height) {
		dimensions.height = height;
	}

	/**
	 * Update the location of this game graphic
	 * 
	 * @param x
	 *            The new X location, with 0,0 being the top left
	 * @param y
	 *            The new Y location, with 0,0 beign the top left
	 */
	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	/**
	 * Update the location of this game graphic, with 0,0 being top left
	 * 
	 * @param location
	 *            The new location object
	 */
	public void setLocation(Location location) {
		setX(location.getX());
		setY(location.getY());
	}

	/**
	 * Set the dimensions of the object
	 * 
	 * @param x
	 *            X location of this object. This should be from the top left
	 *            position.
	 * @param y
	 *            Y location of this object. This should be from the top left
	 *            position.
	 * @param width
	 *            The new width of the object
	 * @param height
	 *            The new height of the object
	 */
	@Override
	public void setDimensions(float x, float y, float width, float height) {
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}

	/**
	 * Return the dimensions of this object
	 */
	@Override
	public Float getDimensions() {
		return dimensions;
	}

	// ----------------------------------------------------------------
	// Misc
	// ----------------------------------------------------------------
	/**
	 * This method will output to the console different pieces of information
	 * such as The current gameLayer's name All gameComponents registered with
	 * this GameLayer
	 */
	@Override
	public String toString() {
		StringBuffer info = new StringBuffer();
		info.append("Layer Name :: ").append(getName()).append("\n");
		info.append("logical Queue : ").append(logicalQueue).append("\n");
		info.append("Draw List : ").append(drawableList);

		// Output drawable
		info.append(drawableList.size() > 0 ? drawableList.size() + " drawable game components added :: " : "No current game drawable components added").append("\n");
		for (IDrawable obj : drawableList)
			info.append(Helpers.concat(obj.toString(), "\n"));

		return info.toString();
	}
}