package engine.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.components.GameSizeableComponent;
import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.GameLogging;
import eventhandling.IMouseEventable;
import eventhandling.MouseEventHandler;

/**
 * This class offers the basic functionality of an interactive non-graphical
 * button. IE this class will make use of basic rectangles for drawing. If you
 * are looking for a more advanced, graphical, button you should instead look at
 * {@link GameButtonGraphical}
 * <p>
 * This class directly registers with the {@link MouseEventHandler} to receive
 * any mouse events, IE for mouse movement into the dimensions of this object.
 * This is fired directly during the next logical update by the
 * {@link MouseEventHandler} which calls the {@link IMouseEventable} methods
 * directly to this object
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameFont
 * @see IDrawableLogical
 * @see IMouseEventable
 */
public class GameButton extends GameSizeableComponent implements IDrawableLogical, IMouseEventable {
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
	protected final static GameLogging logger = new GameLogging(GameButton.class);
	/**
	 * This EventHandler receives AWT mouse events and stores them for the next
	 * time the game loop's logicUpdate is fired. It will then call all of the
	 * relevant event methods through all objects that have registered with this
	 * system (Requires the IEventable interface to be implemented).
	 * <p>
	 * In order to allow for extending classes to have access to this static
	 * field we have made this protected.
	 * <p>
	 * Extending classes should remember that the registering/removal of events
	 * from the mouseEventHandler must be done explicitly, preferrably during
	 * the startup/cleanup functions (as a suggestion)
	 */
	protected final static MouseEventHandler mouseEventHandler = MouseEventHandler.getInstance();

	/**
	 * The list of states which this button is in. If it is 'Active' it means
	 * that eventables work, if it is disabled, events will be ignored
	 * 
	 * This 'ignoring' will be done within the level of the mouse event handler
	 */
	private static enum EventState {
		EVENTS_ACTIVE, EVENTS_DISABLED
	}

	/**
	 * Stores the current state of the GameButton. Currently this is only used
	 * to store whether or not states want to be recieved or not, IE whether or
	 * not events for mouse over should be fired to this GameButton or not
	 */
	protected EventState currentState = EventState.EVENTS_ACTIVE;

	/**
	 * The current colour of the button. This is the colour used to draw the
	 * GameButton during the draw method
	 */
	protected Color currentColor;

	/**
	 * The normal colour that this button is presented as. This colour will be
	 * set as the currentColor when the GameButton does not have a mouse over
	 * it, and it has not recieved focus (IE clicked)
	 */
	protected Color normalColor;
	/**
	 * The colour given to this GameButton when the mouse intersects the
	 * dimension of this object.
	 */
	protected Color hoverColor;
	/**
	 * The colour given to this GameButton when the mouse has been clicked on
	 * this object, and it gained 'focus'
	 */
	protected Color focusColor;

	/**
	 * The default colours that are used within a constructor that does not
	 * define the colours explicitly.
	 */
	protected final static Color defaultNormalColor = Color.WHITE;
	protected final static Color defaultHoverColor = Color.GRAY;
	protected final static Color defaultFocusColor = Color.DARK_GRAY;

	/**
	 * This button encapsulates a GameFont so that it will handle any of the
	 * font drawing for us. This can be modified with the method setFont (which
	 * will directly change the GameFont object) or setText (which will change
	 * the text represented by the GameFont)
	 */
	protected GameFont gameFont;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new GameButton which will be drawn to the screen directly
	 * during the draw update. This object can be added directly to a GameLayer
	 * if required as it implements {@link IDrawableLogical}
	 * 
	 * @param name
	 *            The name of this GameButton, used for debugging when required
	 *            within the game engine
	 * @param font
	 *            The gameFont which represents this button.
	 * @param text
	 *            The default text which will show on this button
	 * @param x
	 *            The X location of the button from top left
	 * @param y
	 *            The Y location of the button from top left
	 * @param width
	 *            The width of the bounds, and drawing width, of this button
	 * @param height
	 *            The height of the bounds, and drawing height, of this button
	 * @param normalColor
	 *            The normal colour that this button is presented as. This
	 *            colour will be set as the currentColor when the GameButton
	 *            does not have a mouse over it, and it has not recieved focus
	 *            (IE clicked)
	 * @param hoverColor
	 *            The colour given to this GameButton when the mouse intersects
	 *            the dimension of this object.
	 * @param focusColor
	 *            The colour given to this GameButton when the mouse has been
	 *            clicked on this object, and it gained 'focus'
	 * 
	 *            /** The normal colour that this button is presented as. This
	 *            colour will be set as the currentColor when the GameButton
	 *            does not have a mouse over it, and it has not recieved focus
	 *            (IE clicked)
	 */
	public GameButton(String name, GameFont font,
			String text,
			float x, float y, float width, float height,
			Color normalColor, Color hoverColor, Color focusColor) {
		super(name, x, y, width, height);

		// Set the currentColor to be the normalColor as expected
		currentColor = normalColor;

		// Store the possible colours. Read the field's comments for further
		// details
		this.normalColor = normalColor;
		this.hoverColor = hoverColor;
		this.focusColor = focusColor;

		// Set the font object
		setFont(font);
		// Set the text represented by the font object
		setText(text);
	}

	/**
	 * Creates a new GameButton which will be drawn to the screen directly
	 * during the draw update. This object can be added directly to a GameLayer
	 * if required as it implements {@link IDrawableLogical}
	 * 
	 * @param name
	 *            The name of this GameButton, used for debugging when required
	 *            within the game engine
	 * @param font
	 *            The gameFont which represents this button.
	 * @param text
	 *            The default text which will show on this button
	 * @param x
	 *            The X location of the button from top left
	 * @param y
	 *            The Y location of the button from top left
	 * @param width
	 *            The width of the bounds, and drawing width, of this button
	 * @param height
	 *            The height of the bounds, and drawing height, of this button
	 */
	public GameButton(String name, GameFont font,
			String text,
			float x, float y, float width, float height) {
		this(name, font, text, x, y, width, height,
				defaultNormalColor, defaultHoverColor, defaultFocusColor);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * This method will be called when an object is first coming into
	 * realization. It is here that we register with the event system
	 */
	@Override
	public void startUp(GameTime gameTime) {
		mouseEventHandler.registerEvents(this);
	}

	/**
	 * Set the font object, By changing this we will essentially be changing the
	 * font
	 * 
	 * @param gameFont
	 *            The new GameFont which will be used by this GameButton.
	 */
	public void setFont(GameFont gameFont) {
		this.gameFont = gameFont;
	}

	/**
	 * This method sets the text of the GameFont directly, so that on the next
	 * draw cycle it will show the updated text value
	 * 
	 * @param text
	 *            The next text represented by this button
	 */
	public void setText(String text) {
		gameFont.setText(text);
	}

	/**
	 * Get the current text represented within this GameButton. This will be the
	 * text stored within the GameFont object
	 * 
	 * @return The current text represented by this GameButton
	 */
	public String getText() {
		return gameFont.getText();
	}

	/**
	 * This method will be called when an object is about to be removed from its
	 * parent. It is here that we remove ourself from any event handling systems
	 * that we've registered with.
	 */
	@Override
	public void cleanUp() {
		mouseEventHandler.unregisterEvents(this);
	}

	/**
	 * Enables the current object to be eligable for any event handling fires.
	 * For instance mouse over.
	 */
	public void enableEvents() {
		currentState = EventState.EVENTS_ACTIVE;
	}

	/**
	 * Disables events from being sent to this GameButton. The object does not
	 * need to unregister from any event systems for this to work.
	 */
	public void disableEvents() {
		currentState = EventState.EVENTS_DISABLED;
	}

	/**
	 * Draws the current GameButton in 3 stages. Firstly we draw the base colour
	 * represented by this non-graphical button, then we draw the game font over
	 * it, then we apply the border ontop.
	 * 
	 * @param drawScreen
	 *            Direct access to the graphics2d object where all drawing
	 *            should appear
	 * @param offsetX
	 *            The x offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 * @param offsetY
	 *            The y offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		drawScreen.setColor(currentColor);
		drawScreen.fillRect((int) (offsetX + getX()), (int) (offsetY + getY()), (int) getWidth(), (int) getHeight());

		drawGameFont(drawScreen, offsetX, offsetY);

		drawScreen.setColor(Color.black);
		drawScreen.drawRect((int) (offsetX + getX()), (int) (offsetY + getY()), (int) getWidth(), (int) getHeight());
	}

	/**
	 * Draws the GameFont object which is used to visually represent the text
	 * within this GameButton. In order to allow for more modularised and
	 * adaptable code this has been put into a separate method.
	 * 
	 * @param drawScreen
	 *            Direct access to the graphics2d object where all drawing
	 *            should appear
	 * @param offsetX
	 *            The x offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 * @param offsetY
	 *            The y offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 */
	protected void drawGameFont(Graphics2D drawScreen, int offsetX, int offsetY) {
		if (gameFont == null) {
			logger.error(new NullPointerException(), "GameFont was null in GameButton");
		}

		// Draw the game font to the center of the button. If I have time i'll
		// change this to use the alignment, like which GameFont currently
		// offers
		gameFont.draw(drawScreen,
				(int) (offsetX + getX() + (getWidth() / 2) - (gameFont.getWidth() / 2)),
				(int) (offsetY + getY() + (getHeight() / 2) - (gameFont.getHeight() / 2)));
	}

	/**
	 * Fired When the mouse enters the dimension of an object, this is only
	 * fired once. IE, if a mouse is over an object for more than 1 game update,
	 * it will only be fired once and not every time until the user moves their
	 * mouse out
	 */
	@Override
	public void mouseOver() {
		currentColor = hoverColor;
	}

	/**
	 * Fired When the mouse LEAVES the dimension of an object, this is only
	 * fired once. IE, if a the mouse entered a game object, and left the game
	 * object it is only fired on the one game loop update
	 */
	@Override
	public void mouseOut() {
		currentColor = normalColor;
	}

	/**
	 * This is fired when a mouse is clicked down, and is in the dimensions of
	 * the registered eventable object
	 */
	@Override
	public void mousePressed() {
		currentColor = focusColor;
	}

	/**
	 * This is fired when an object which originally gained MOUSE_FOCUS (ie
	 * mouse clicked on the object), and then loses focus by the mouse then
	 * being clicked elsewhere
	 */
	@Override
	public void mouseBlur() {
		currentColor = normalColor;
	}

	/**
	 * Decides whether or not this IMouseEventable object still wants to receive
	 * any mouse events still.
	 * 
	 * @return True if the IMouseEventable object doesn't need to receive
	 *         events. False if the IMouseEventable object still wants to
	 *         receive any events still.
	 */
	@Override
	public boolean isEventsDisabled() {
		return currentState == EventState.EVENTS_DISABLED;
	}
}
