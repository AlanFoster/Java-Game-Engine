package engine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import engine.main.GameEngine;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.State;
import engine.misc.managers.StateManager;
import eventhandling.IKeyboardEventable;
import eventhandling.IMouseEventable;
import eventhandling.KeyEventHandler;
import eventhandling.MouseEventHandler;

/**
 * This class offers the ability to create a new visual input field that is
 * similar to an HTML input field. This class registers with the key event
 * handling system so that it can receive key events when the user inputs any
 * characters, which will be added to the field's contents. And it inherits the
 * register with the mouse event system so that it will only add characters to
 * the input field when it has gained 'focus', see {@link IMouseEventable} for
 * further details
 * <p>
 * Could be useful for allowing a username to be inputted for tracking
 * highscores etc.
 * <p>
 * This class extends GameButton as all of the methods we'd expect to be offered
 * from an input field are in the GameButton
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameButton
 * @see IKeyboardEventable
 * @see IMouseEventable
 */
public class GameInputField extends GameButton implements IKeyboardEventable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * This EventHandler receives AWT key events and stores them for the next
	 * time the game loop's logicUpdate is fired. It will then call all of the
	 * relevant event methods through all objects that have registered with this
	 * system (Requires the {@link IKeyboardEventable} interface to be
	 * implemented).
	 * <p>
	 * This GameScreen manager is the point at which the keyEventHandler will
	 * Receive its 'logic update' in which it will fire all of its required
	 * events.
	 */
	private final static KeyEventHandler keyEventHandler = KeyEventHandler.getInstance();

	/**
	 * When this input has received focus, IE when the user has pressed within
	 * the bounds of the object this will be set to true. It is set to false
	 * when the user has clicked on the button and clicked outside of the bounds
	 * (and received its BLUR event)
	 */
	public boolean hasFocus;

	/** Stores the length of the string within the TextInputField **/
	protected int currentSize;

	/**
	 * * Limits the user's input to a maximum value. If this value is 0, no
	 * limit is
	 **/
	protected int maximumSize;

	/**
	 * Based on the width, it works out the maximum characters that can be shown
	 * at once
	 */
	protected int maximumCharSpace;

	/**
	 * Stores the current caret position. This should NOT be set directly,
	 * instead you should set it with updateCursor()
	 */
	protected int cursorPos;

	/**
	 * The amount of time that the cursor will show for, and hide for. The
	 * Cursor Blinking Time. This default blinking time is 450 milliseconds.
	 */
	protected int desiredBlinkingTime = 450;

	/**
	 * Stores the current amount of time that the cursor has been shown for.
	 * When this input field receives focus it will be set the the value of
	 * desiredBlinkingTime, and each game loop update it will be decreased, once
	 * it is 0 it will change the cursor state to showing/not showing.
	 */
	protected int blinkingCountDown;

	/**
	 * This boolean is used to decide whether or not to show the cursor within
	 * the text field. This will be set to true when the input field has first
	 * gained focus. Then the blinking count down will decrement each game loop
	 * update, when the blinking count down has reached 0 it will set this to
	 * false, and every time the desired blinking time reaches 0, the state will
	 * alternate.
	 */
	protected boolean showCursor;

	/**
	 * This GameFont is simply used to calculate the width of the GameFont
	 * before the cursor, so that we can correctly place the cursor as required
	 */
	GameFont beforeCursorText;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new instance of a GameInputField which is similar to <select
	 * type="input" /> in HTML
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
	public GameInputField(String name, GameFont font, String text,
			float x, float y, float width, float height) {
		super(name, font, text, x, y, width, height);

		currentSize = text.length();
	}

	/**
	 * Creates a new instance of a GameInputField which is similar to <select
	 * type="input" /> in HTML, with the desired blinking time
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
	 * @param blinkingTime
	 *            The amount of time that the cursor will show for, and hide
	 *            for. The Cursor Blinking Time.
	 */
	public GameInputField(String name, GameFont font, String text,
			float x, float y, float width, float height,
			int blinkingTime) {
		super(name, font, text, x, y, width, height);

		desiredBlinkingTime = blinkingTime;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	@Override
	public void startUp(GameTime gameTime) {
		super.startUp(gameTime);
		keyEventHandler.registerEvents(this);
	}

	/**
	 * This method will be called when an object is about to be removed from its
	 * parent. It is here that we remove ourself from any event handling systems
	 * that we've registered with.
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		keyEventHandler.removeEvents(this);
	}

	/**
	 * Fired When the mouse LEAVES the dimension of an object, this is only
	 * fired once. IE, if a the mouse entered a game object, and left the game
	 * object it is only fired on the one game loop update
	 */
	@Override
	public void mouseOut() {
		if (!hasFocus)
			super.mouseOut();
	}

	/**
	 * This is fired when a mouse is clicked down, and is in the dimensions of
	 * the registered eventable object
	 */
	@Override
	public void mousePressed() {
		super.mouseOver();
		hasFocus = true;
		// set the cursor to the end of the text field when pressed
		updateCursorPos(currentSize - cursorPos);
		// Reset the 'blinking' count down, and change the cursor set to be
		// visible
		showCursor = true;
		blinkingCountDown = desiredBlinkingTime;
	}

	/**
	 * This is fired when an object which originally gained MOUSE_FOCUS (ie
	 * mouse clicked on the object), and then loses focus by the mouse then
	 * being clicked elsewhere
	 */
	@Override
	public void mouseBlur() {
		super.mouseBlur();
		hasFocus = false;
		cursorPos = currentSize;
	}

	/**
	 * Set the font object, By changing this we will essentially be changing the
	 * font. At this point we also get a shallow clone of the current GameFont
	 * so that we can calculate the which we use for calcuating the placement of
	 * the input field's cursor
	 * 
	 * @param gameFont
	 *            The new GameFont which will be used by this GameButton.
	 */
	public void setFont(GameFont gameFont) {
		super.setFont(gameFont);
		beforeCursorText = gameFont.getShallowClone();
	}

	/**
	 * Sets the maximum size of this field. NOTE :: This simply sets the maximum
	 * size of the field, and will not perform any sort of substring to make the
	 * text field fit to the required field. It will only perform that logic
	 * when it has focus and is being typed into. However, this is simple logic
	 * to add, and I see no need for it.
	 * 
	 * @param maxsize
	 *            The new maximum size of this field
	 */
	public void setMaxSize(int maxsize) {
		maximumSize = maxsize;
	}
	/**
	 * Change the cursor position within the text field. At this point we also
	 * update the 'beforeCursorText', which we use to calculate the cursor
	 * postion
	 */
	protected void updateCursorPos(int amount) {
		cursorPos += amount;
		// Update the before cursor text to calculate the width for showing the
		// cursor as expected
		beforeCursorText.setText(getText().substring(0, cursorPos));
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
		blinkingCountDown -= gameTime.getElapsedTimeMilli();
		if (blinkingCountDown <= 0) {
			blinkingCountDown = desiredBlinkingTime;
			showCursor = !showCursor;
		}
	}

	/**
	 * Check if the current text input is empty
	 * 
	 * @return Returns true if, and only if, length() is 0.
	 */
	public boolean isEmpty() {
		return gameFont.getText().isEmpty();
	}

	/**
	 * Adds keys to the inputfield when the keyPressed event is fired to this
	 * object. This event will only be fired during logical updates. The
	 * requirement to add to this text field is that the user has placed focus
	 * on the text field through clicking on it.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		// Only consider changing the value of the text field when the user has
		// focused onto the text field
		if (hasFocus) {
			// Get the input as a keycode
			int userInput = e.getKeyCode();
			switch (userInput) {
			/**
			 * If the back space is pressed, delete one character to the left of
			 * the caret position
			 */
				case KeyEvent.VK_BACK_SPACE:
					if (currentSize > 0 && cursorPos > 0) {
						setText(getText().substring(0, cursorPos - 1) + (cursorPos < currentSize ? getText().substring(cursorPos) : ""));
						currentSize--;
						updateCursorPos(-1);
					}
					break;
				/**
				 * If the left key is pressed, move the caret to the left
				 */
				case KeyEvent.VK_LEFT:
					if (cursorPos > 0) {
						updateCursorPos(-1);
					}
					break;
				/**
				 * If the right key is pressed, move the caret to the right
				 */
				case KeyEvent.VK_RIGHT:
					if (cursorPos < currentSize) {
						updateCursorPos(+1);
					}
					break;
				/**
				 * If the delete key is pressed, delete the character to the
				 * right of the caret pos
				 */
				case KeyEvent.VK_DELETE:
					if (cursorPos != currentSize) {
						setText(getText().substring(0, cursorPos) + getText().substring(cursorPos + 1));
						currentSize--;
					}
					break;
				/**
				 * Default is any other key pressed which can be added to the
				 * text field
				 */
				default:
					char charInput = e.getKeyChar();
					// Only add the typed keyChar if the game font contains the
					// character or it's a space, it isn't an action key AND the
					// currentSize is less than the maximum size (if it's set)
					if ((!e.isActionKey() && gameFont.containsChar(charInput) || charInput == ' ')
							&& (maximumSize == 0 || currentSize < maximumSize)) {
						setText(getText().substring(0, cursorPos) + charInput + (cursorPos < currentSize ? getText().substring(cursorPos, currentSize) : ""));
						updateCursorPos(+1);
						currentSize++;
					}
					break;
			}
		}
	}

	/**
	 * Override the drawGameFont method so that we can draw the cursor position
	 * within the input field as required. And potentially clip any of the
	 * shape.
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
	public void drawGameFont(Graphics2D drawScreen, int offsetX, int offsetY) {
		if (gameFont == null) {
			logger.error(new NullPointerException(), "GameFont was null in GameButton");
		}

		// Draw the game font
		gameFont.draw(drawScreen,
				(int) (offsetX + getX()),
				(int) (offsetY + getY() + (getHeight() / 2) - (gameFont.getHeight() / 2)));

		// Calculate the width of the string before the cursor position
		if (hasFocus && currentSize > 0 && showCursor) {
			int leftSideWidth = (int) (beforeCursorText.getWidth());

			drawScreen.setColor(Color.DARK_GRAY);
			drawScreen.fillRect((int) (getX() + offsetX) + leftSideWidth,
					(int) (offsetY + getY() + (getHeight() / 2) - (gameFont.getHeight() / 2)),
					2, (int) getHeight() - 10);
		}
	}
}
