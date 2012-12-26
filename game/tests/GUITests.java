package tests;

import java.awt.Color;
import java.awt.event.KeyEvent;

import src.Configuration;

import engine.assets.GameGraphic;
import engine.gui.GameButton;
import engine.gui.GameFont;
import engine.gui.GameInputField;
import engine.gui.GameSelectMenu;
import engine.main.GameLayer;
import engine.main.GameScreen;
import engine.misc.GameLogging;
import engine.misc.GameSettings;
import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;

/**
 * This class is used as an example to demonstrate what graphic components that
 * the game engine currently offers, and how it might be used. This is a quick
 * example of how it might work, and is not a complete example.
 * <p>
 * This suggests how you might create a user input form for high scores or
 * whatever is required. To further this idea you could possibly create a
 * GameForm container, which would allow for regex validation etc. This was
 * removed from the 'final' version of the GameEngine however.
 * <p>
 * This example takes the form of a game screen which may take the user's
 * username and difficulty level.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GUITests extends GameScreen {
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
	 * The common font that will be shared by all of the buttons/text fields etc
	 * within this example
	 */
	final static GameFont mainGameFont = GameAssetManager.getInstance().getClonedObject(GameFont.class, Configuration.GUI.Fonts.NORMAL);

	/**
	 * Stores the background which gets rendered first
	 */
	private GameLayer background;

	/**
	 * A reference to the 'main layer' which will be registered to this game
	 * screen, so that we can freely add our gui components to it as desired
	 */
	private GameLayer mainLayer;
	/**
	 * Direct reference to the submit button so that we can position fields
	 * around it etc
	 */
	private GameButton submitButton;
	/**
	 * Direct reference to the usernameinput field so that we can get the value
	 * of it when the submit button is pressed
	 */
	private GameInputField usernameField;
	/**
	 * The 'responseText' which will be changed when the user hits the submit
	 * button
	 */
	private GameFont responseText;
	/**
	 * A drop down menu for the 'difficulty' level
	 */
	private GameSelectMenu menuButton;

	/**
	 * The Y location to put the input field
	 */
	private int inputFieldLocationY = 200;

	/*
	 * The colours for the non-graphical GameButton
	 */
	/**
	 * The normal colour that this button is presented as. This colour will be
	 * set as the currentColor when the GameButton does not have a mouse over
	 * it, and it has not recieved focus (IE clicked)
	 */
	protected Color normalColor = new Color(0xCC00FF00);
	/**
	 * The colour given to this GameButton when the mouse intersects the
	 * dimension of this object.
	 */
	protected Color hoverColor = new Color(0xCCFF0000);
	/**
	 * The colour given to this GameButton when the mouse has been clicked on
	 * this object, and it gained 'focus'
	 */
	protected Color focusColor = new Color(0xCC0000FF);

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	public GUITests() {
		super("GUITests");

		background = new GameLayer("background");
		background.add(GameAssetManager.getInstance().getObject(GameGraphic.class, Configuration.GUI.Graphics.BACKGROUND_IMAGE));
		addGameLayer(background);

		mainLayer = new GameLayer("buttonLayer");
		addGameLayer(mainLayer);

		// Create the basic components
		createInputField();
		createDescriptionText();
		createSubmitButton();
		createResponseText();
		createMenuButton();
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Creates a piece of text in the center of the screen explaining to the
	 * user what to do
	 */
	protected void createDescriptionText() {
		GameFont descriptionFont = mainGameFont.getShallowClone();
		descriptionFont.setText("~~~hello~~~\nplease enter your name");
		descriptionFont.setAlignment(GameFont.Alignment.MIDDLE_ALIGN);
		// set the location to the center of the screen
		descriptionFont.setX(getCenterLocation(descriptionFont.getWidth()));
		// Move above the input box
		descriptionFont.setY(usernameField.getY() - descriptionFont.getHeight() - 30);
		mainLayer.add(descriptionFont);
	}

	/**
	 * Creates the input field that the user can put their name into
	 */
	protected void createInputField() {
		int inputFieldWidth = 400;
		int inputFieldHeight = 40;
		// the maximum amount of characters which can be put into the text field
		int usernameMaxInputSize = 14;

		usernameField = new GameInputField("inputField", mainGameFont.getShallowClone(), "bob",
				getCenterLocation(inputFieldWidth), inputFieldLocationY,
				inputFieldWidth, inputFieldHeight);
		usernameField.setMaxSize(usernameMaxInputSize);
		mainLayer.add(usernameField);
	}

	/**
	 * Creates the submit button which will be underneath the input field. Note
	 * :: It is perfectly fine to create a new class which extends GameButton
	 * and provides the required logic, I just prefer this way of creating
	 * buttons in a fast maner without having to create thousands of full
	 * classes, or private inner classes simply to instantiate some logic.
	 */
	protected void createSubmitButton() {
		int submitButtonWidth = 200;
		int submitButtonHeight = 40;
		int submitButtonY = 500;

		// Create the submit button and override its default mousePressed logic
		submitButton = new GameButton("button", mainGameFont.getShallowClone(), "submit",
				getCenterLocation(submitButtonWidth), submitButtonY,
				submitButtonWidth, submitButtonHeight,
				normalColor, hoverColor, focusColor) {
			@Override
			public void mousePressed() {
				super.mousePressed();
				// Call our logic which will get fired when this game button
				// Receives focus
				usernameSubmitButtonPressed();
			}
		};
		mainLayer.add(submitButton);
	}

	/**
	 * Create the 'response' text, which will respond once the user has inputted
	 * their user name into the text field and pressed 'submit'
	 */
	protected void createResponseText() {
		responseText = mainGameFont.getShallowClone();
		responseText.setText("enter your details");
		// Align the text center
		responseText.setAlignment(GameFont.Alignment.MIDDLE_ALIGN);

		// set the location to the center of the screen
		responseText.setX(GameSettings.getGameWidth() / 2 - responseText.getWidth() / 2);
		// Move above the input box
		responseText.setY(submitButton.getY() + submitButton.getHeight() + 30);
		mainLayer.add(responseText);
	}

	/**
	 * Creates the drop down menu which contains the difficulty options
	 */
	protected void createMenuButton() {
		// @formatter:off
		// The possible options created by this menu.
		// options[0][0] is the visual text which will be shown 
		// options[0][1] is the 'value' of the text field, similar to that
		// of an HTML field which contains <code><select><option value="foo"></code>
		String[][] options = {
				{ "Easy", "easy" },
				{ "Med", "medium" },
				{ "Hard", "hard"}
		};
		// @formatter:on

		int menuButtonWidth = 350;
		int menuButtonHeight = 40;

		// Create the menu button below the username field
		menuButton = new GameSelectMenu("mainMenuTest", mainGameFont.getShallowClone(),
				getCenterLocation(menuButtonWidth), usernameField.getY() + usernameField.getHeight() + 20,
				menuButtonWidth, menuButtonHeight,
				"Difficulty",
				options);
		mainLayer.add(menuButton);
	}

	/**
	 * This logic is called by the GameButton created within the method
	 * createSubmitButton. It also gets called directly when the enter key is
	 * pressed
	 */
	protected void usernameSubmitButtonPressed() {
		// 'Validate' the form inptu fields, IE, if the drop down value is
		// 'null' that means it hasn't been switched from its 'header' value (a
		// descriptive word initially stored within the drop down menu which
		// shouldn't be selected'
		if (menuButton.getValue() == null) {
			responseText.setText("please enter a difficulty level!");
		} else if (usernameField.isEmpty()) {
			responseText.setText("please enter a username!");
		} else {
			// Output the username and selected difficulty level.
			// At this point you may potentially start the game, or save
			// highscores, or do whatever logic required
			responseText.setText(Helpers.concat("hello, ", usernameField.getText(), "!",
					"\nDifficulty level :: ", menuButton.getValue()));
		}
		responseText.setX(getCenterLocation(responseText.getWidth()));
	}

	/**
	 * Listen for the enter key, and if the enter key has been pressed, fire the
	 * method usernameSubmitButtonPressed if the input field has focus
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER && usernameField.hasFocus) {
			usernameSubmitButtonPressed();
		}
	}

	/**
	 * A method which will return the X location that an object should be for it
	 * to be centered in the middle of the screen
	 * 
	 * @param width
	 *            The width of the object
	 * @return The X location that the object should be for it to be centered in
	 *         the middle of the screen
	 */
	public float getCenterLocation(float width) {
		return GameSettings.getGameWidth() / 2 - width / 2;
	}
}