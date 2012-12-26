package engine.gui;

import java.awt.Graphics2D;
import engine.assets.GameGraphic;
import engine.interfaces.IDrawableLogical;
import engine.misc.GameLogging;
import eventhandling.IMouseEventable;
import eventhandling.MouseEventHandler;

/**
 * This class offers the basic functionality of an interactive <b>graphical</b>
 * button. If you require a non-graphical and only require basic shapes to be
 * drawn you should use {@link GameButton} instead.
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

public class GameButtonGraphical extends GameButton {
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
	private final static GameLogging logger = new GameLogging(GameButtonGraphical.class);

	/**
	 * The 'normal' graphic shown by this button. This graphic will set when the
	 * mouse does not intersect the GameButtonGraphical's dimensions, and has
	 * not receives a focus click.
	 */
	protected GameGraphic normalGraphic;

	/**
	 * This is the graphic shown when the mouse intersects the dimensions of
	 * this object, and has not received a focus click.
	 */
	protected GameGraphic hoverGraphic;
	/**
	 * The image drawn by this object when the mouse has been clicked on this
	 * object, and it gained 'focus'
	 */
	protected GameGraphic focusGraphic;

	/**
	 * The current GameGraphic representation which will be drawn to the screen.
	 * This object will be set to the either objects normalGraphic,
	 * hoverGraphic, focusGraphic depending on the states. Read their notes for
	 * more details.
	 */
	protected GameGraphic currentGraphic;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
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
	 * @param normalGraphic
	 *            The 'normal' graphic shown by this button. This graphic will
	 *            set when the mouse does not intersect the
	 *            GameButtonGraphical's dimensions, and has not receives a focus
	 *            click.
	 * @param hoverGraphic
	 *            This is the graphic shown when the mouse intersects the
	 *            dimensions of this object, and has not received a focus click.
	 * @param focusGraphic
	 *            The image drawn by this object when the mouse has been clicked
	 *            on this object, and it gained 'focus'
	 */
	public GameButtonGraphical(String name,
			GameFont font, String text,
			int x, int y,
			GameGraphic normalGraphic, GameGraphic hoverGraphic, GameGraphic focusGraphic) {
		super(name, font, text, x, y, normalGraphic.getWidth(), normalGraphic.getHeight());

		this.normalGraphic = this.currentGraphic = normalGraphic;
		this.hoverGraphic = hoverGraphic;
		this.focusGraphic = focusGraphic;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * This draw happens in to stages. Firstly it draws the current graphic
	 * which is being represented by this graphic button, then it makes a
	 * request for the GameFont object to be drawn. This drawing of the font
	 * object is within the super class {@link GameButton}
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
		currentGraphic.draw(drawScreen, (int) getX() + offsetX, (int) getY() + offsetY);
		super.drawGameFont(drawScreen, offsetX, offsetY);
	}

	/**
	 * Fired When the mouse enters the dimension of an object, this is only
	 * fired once. IE, if a mouse is over an object for more than 1 game update,
	 * it will only be fired once and not every time until the user moves their
	 * mouse out
	 */
	@Override
	public void mouseOver() {
		currentGraphic = hoverGraphic;
	}

	/**
	 * Fired When the mouse LEAVES the dimension of an object, this is only
	 * fired once. IE, if a the mouse entered a game object, and left the game
	 * object it is only fired on the one game loop update
	 */
	@Override
	public void mouseOut() {
		currentGraphic = normalGraphic;
	}

	/**
	 * This is fired when a mouse is clicked down, and is in the dimensions of
	 * the registered eventable object
	 */
	@Override
	public void mousePressed() {
		currentGraphic = focusGraphic;
	}

	/**
	 * This is fired when an object which originally gained MOUSE_FOCUS (ie
	 * mouse clicked on the object), and then loses focus by the mouse then
	 * being clicked elsewhere
	 */
	@Override
	public void mouseBlur() {
		currentGraphic = normalGraphic;
	}
}
