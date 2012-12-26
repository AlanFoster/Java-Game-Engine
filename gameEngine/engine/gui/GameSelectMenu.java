package engine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import engine.components.GameSizeableComponent;
import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.Helpers;

/**
 * Creates a new Selectable field menu. This has been based on the HTML
 * select/option implementation, in which the select menu shows the currently
 * selected child at the top, and when clicked on it will show all options that
 * the menu has registered to it.
 * <p>
 * This selectable menu offers a 'value' field, which is also similar to the
 * HTML counterpart that this is based on, and will store the currently selected
 * child's value.
 * <p>
 * In order to represent this 'value' field which each option will contain, we
 * have encapsulated a private final inner class which extends a
 * {@link GameButton} and stores the extra value field. This class also
 * overrides the mouse focus functionality in order to process the logic
 * required for closing/opening the menu, and selecting the new game option
 * values.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GameSelectMenu extends GameSizeableComponent implements IDrawableLogical {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The button which will always visibly be shown, and will represent the
	 * 'currently' selected option within the menu. When a select option is
	 * chosen from this menu, the selected child's text representation and value
	 * will become the selected child's text/value respectively.
	 */
	protected SelectMenuOption selectedChild;

	/**
	 * All of the options which are offered by this menu. When this menu's state
	 * is MenuSate.OPEN these will be shown and be selected to replace the
	 * currently chosen option.
	 */
	protected List<SelectMenuOption> children;

	/**
	 * The states that this {@link GameSelectMenu} can be under, currently this
	 * is OPEN and CLOSED
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	enum MenuState {
		OPEN, CLOSED
	}

	/**
	 * The current MenuState which this menu is under. By default this is set to
	 * closed.
	 */
	protected MenuState currentMenuState = MenuState.CLOSED;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------

	/**
	 * Creates a new select option menu
	 * 
	 * @param name
	 *            The name of this object
	 * @param gameFont
	 * @param posX
	 *            The X location of this object, with 0,0 being top left
	 * @param posY
	 *            The Y location of this object, with 0,0 being top left
	 * @param width
	 *            The height of this object. All options will share this width
	 * @param height
	 *            The width of this object. All options will share this width
	 * @param menuValues
	 *            A 2D string array which will represent the text value of the
	 *            an option, and the actual value of the button which will be
	 *            given to this {@link GameSelectMenu} when it becomes the
	 *            currently selected child.
	 * 
	 *            <pre>
	 * 			<code>
	 * 			options[0][0] is the visual text which will be shown 
	 *         	options[0][1] is the 'value' of the text field
	 * 			</code>
	 * </pre>
	 */
	public GameSelectMenu(String name, GameFont gameFont,
			float posX, float posY,
			float width, float height,
			String[][] menuValues) {
		super(name, posX, posY, width, height);

		children = new ArrayList<SelectMenuOption>();

		selectedChild = new SelectMenuOption(Helpers.concat("selectedChild", name),
				gameFont.getShallowClone(),
				"",
				posX, posY, width, height, "d");

		// Add a bunch of children to the menu for testing
		float menuYLocation = posY + selectedChild.getHeight();
		for (int i = 0; i < menuValues.length; i++) {
			String representedText = menuValues[i][0];
			String value = menuValues[i][1];

			SelectMenuOption newChild = new SelectMenuOption(name, gameFont.getShallowClone(),
					representedText,
					posX, menuYLocation, width, height,
					value);
			children.add(newChild);
			menuYLocation += newChild.getHeight();
		}

		setSelectedChild(children.get(0));
		closeMenu();
	}

	/**
	 * Creates a new select option menu with a default menu option. This field
	 * use the value of null to represent that the title option is selected
	 * within this menu.
	 * 
	 * @param name
	 *            The name of this object
	 * @param gameFont
	 * @param posX
	 *            The X location of this object, with 0,0 being top left
	 * @param posY
	 *            The Y location of this object, with 0,0 being top left
	 * @param width
	 *            The height of this object. All options will share this width
	 * @param height
	 *            The width of this object. All options will share this width
	 * @param defaultText
	 *            The text that will be shown as the 'selected child' within
	 *            this menu. This option will be given a value of null to
	 *            represent that the title option is still selected within the
	 *            system.
	 * @param menuValues
	 *            A 2D string array which will represent the text value of the
	 *            an option, and the actual value of the button which will be
	 *            given to this {@link GameSelectMenu} when it becomes the
	 *            currently selected child.
	 */
	public GameSelectMenu(String name, GameFont gameFont,
			float posX, float posY,
			float width, float height,
			String defaultText,
			String[][] menuValues) {
		this(name, gameFont, posX, posY, width, height, menuValues);
		selectedChild.setText(defaultText);
		selectedChild.value = null;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	@Override
	public void startUp(GameTime gameTime) {
		selectedChild.startUp(gameTime);
		for (GameButton child : children) {
			child.startUp(gameTime);
		}
	}

	@Override
	public void cleanUp() {
		selectedChild.cleanUp();
		for (GameButton child : children) {
			child.cleanUp();
		}
	}

	/**
	 * When an option from the {@link GameSelectMenu} has been selected, this
	 * method will be called. This method sets the current selected Child's text
	 * and the value now represented by this {@link GameSelectMenu} object.
	 * 
	 * @param newSelected
	 *            The child that was selected from this GameSelectMenu
	 */
	protected void setSelectedChild(SelectMenuOption newSelected) {
		selectedChild.setText(newSelected.getText());
		selectedChild.value = newSelected.value;
	}

	/**
	 * Iterates all children which have been added to this menu, and enables
	 * their events. This will be called when the menu has been opened, and we
	 * want for the event handler to fire mouse events to the object.
	 */
	protected void addChildEvents() {
		for (GameButton child : children) {
			child.enableEvents();
		}
	}

	/**
	 * Iterates all children which have been added to this menu, and removes
	 * their events. This will be called when the menu has been closed, so that
	 * any mouse events which were created by the child buttons will be ignored
	 */
	protected void removeChildEvents() {
		for (GameButton child : children) {
			child.disableEvents();
		}
	}

	/**
	 * Opens the menu. Within the next draw loop all children buttons will be
	 * shown
	 */
	protected void openMenu() {
		addChildEvents();
		currentMenuState = MenuState.OPEN;
	}
	/**
	 * Closes the menu. Within the next draw loop all children buttons will now
	 * be hidden. This method also removes all events which have been added by
	 * the children buttons so that they are no longer fired.
	 */
	protected void closeMenu() {
		removeChildEvents();
		currentMenuState = MenuState.CLOSED;
	}

	/**
	 * Get the value represented by this select menu
	 * 
	 * @return The string value within this menu button
	 */
	public String getValue() {
		return selectedChild.value;
	}

	/**
	 * Draws the currently selected child, with a rather crude downwards
	 * pointing arrow to the right of it. This drawn can be overriden, and it is
	 * suggested that you do so! The method to change is :: drawOpenBox
	 * <p>
	 * The draw method will only draw the children if the menu's current state
	 * is MenuState.OPEN
	 * 
	 * @param drawScreen
	 *            Direct access to the graphics2d object where all drawing
	 *            should appear
	 * 
	 * @param offsetX
	 *            The x offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 * @param offsetY
	 *            The y offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.(non-Javadoc)
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		selectedChild.draw(drawScreen, offsetX, offsetY);
		if (currentMenuState == MenuState.OPEN) {
			for (GameButton child : children) {
				child.draw(drawScreen, offsetX, offsetY);
			}
		}

		// Draws a rather crude downwards pointing arrow. This drawn can be
		// overriden, and it is suggested that you do so!
		drawOpenBox(drawScreen, offsetX, offsetY);
	}

	/**
	 * Draws a rather crude downwards pointing arrow. This drawn can be
	 * overriden, and it is suggested that you do so!
	 * 
	 * @param drawScreen
	 *            Direct access to the graphics2d object where all drawing
	 *            should appear
	 * 
	 * @param offsetX
	 *            The x offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 * @param offsetY
	 *            The y offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.(non-Javadoc)
	 */
	protected void drawOpenBox(Graphics2D drawScreen, int offsetX, int offsetY) {
		// Draw a white filled rect
		drawScreen.setColor(Color.white);
		drawScreen.fillRect((int) (getX() + getWidth() - 40), (int) getY(), 40, (int) getHeight());

		// Draw a rectangle
		drawScreen.setColor(currentMenuState == MenuState.OPEN ? Color.black : new Color(0x1e4052));
		drawScreen.drawRect((int) (getX() + getWidth() - 40), (int) getY(), 40, (int) getHeight());
		// Draw the 'downwards point arrow', which suggests it can be opened
		drawScreen.drawString("V", (int) (getX() + getWidth() + -20), (int) (getY() + getHeight() / 2));
	}

	/**
	 * This class represents an option which can be shown within the menu when
	 * opened.
	 * <p>
	 * In order to represent the 'value' field which each option will contain,
	 * we have encapsulated a private final inner class which extends a
	 * {@link GameButton} and stores the extra value field. This class also
	 * overrides the mouse focus functionality in order to process the logic
	 * required for closing/opening the menu, and selecting the new game option
	 * values.
	 * 
	 * @author Alan Foster
	 * 
	 * @version 1.0
	 */
	private final class SelectMenuOption extends GameButton {
		/**
		 * The value that this option has. If this {@link SelectMenuOption} is
		 * selected, this value will be given to the {@link GameSelectMenu} -
		 * like that of an HTML field.
		 */
		public String value;

		/**
		 * Creates a new GameButton which will be drawn to the screen directly
		 * during the draw update. This object can be added directly to a
		 * GameLayer if required as it implements {@link IDrawableLogical}
		 * 
		 * @param name
		 *            The name of this GameButton, used for debugging when
		 *            required within the game engine
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
		 *            The height of the bounds, and drawing height, of this
		 *            button
		 * @param value
		 *            The value that this option has. If this
		 *            {@link SelectMenuOption} is selected, this value will be
		 *            given to the {@link GameSelectMenu} - like that of an HTML
		 *            field.
		 * */
		public SelectMenuOption(String name,
				GameFont font, String text,
				float x, float y,
				float width, float height,
				String value) {
			super(name, font, text, x, y, width, height);
			this.value = value;
		}

		/**
		 * This is fired when a mouse is clicked down, and is in the dimensions
		 * of the registered eventable object
		 */
		@Override
		public void mousePressed() {
			super.mousePressed();
			// If the menu is open, close it. And if the menu is closed, open it
			if (this == selectedChild) {
				if (currentMenuState == MenuState.OPEN) {
					closeMenu();
				} else if (currentMenuState == MenuState.CLOSED) {
					openMenu();
				}
			} else {
				// If we have selected an option which isn't the selectedchild
				// option, update the selected child information
				setSelectedChild(this);
				closeMenu();
			}
		}

		/**
		 * This is fired when an object which originally gained MOUSE_FOCUS (ie
		 * mouse clicked on the object), and then loses focus by the mouse then
		 * being clicked elsewhere
		 */
		@Override
		public void mouseBlur() {
			super.mouseBlur();
			if (this == selectedChild) {
				closeMenu();
			}
		}
	}
}
