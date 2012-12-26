package menu;

import java.awt.image.BufferedImage;

import src.Configuration;
import src.TankSurvival;
import engine.assets.GameGraphic;
import engine.gui.GameButton;
import engine.gui.GameButtonGraphical;
import engine.gui.GameFont;
import engine.interfaces.ILogical;
import engine.interfaces.IPositionable;
import engine.main.GameEngine;
import engine.main.GameLayer;
import engine.main.GameScreen;
import engine.main.GameTime;
import engine.misc.GameSettings;
import engine.misc.Helpers;
import engine.misc.State;
import engine.misc.managers.GameAssetManager;
import engine.misc.managers.StateManager;

/**
 * <p>
 * This class represents the 'MainMenu' for the game. This game screen will
 * allow the user to pick from the options offered by the game. This class makes
 * effective use of the GameFont classes and State manager to produce dynamic
 * menus, which means that the menu system can be EASILY changed by changing the
 * field called "options"
 * </p>
 * It is expected that this is the first screen that starts up when the game is
 * loaded. This class is used for the navigational point between all of the
 * GameScreens. ( A 'Game Screen' is an individual screen that can be shown at
 * once. It will manage game layers and invoke the render/logic methods.)
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see {@link GameScreen}
 * @See {@link StateManager}
 */
public class MainMenu extends GameScreen {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * Stores the background which gets rendered first
	 */
	private GameLayer background;

	/**
	 * Stores all of the GameGraphicButtons that are offered to the user.
	 */
	private GameLayer menuOptions;

	/**
	 * The distance between the bottom of the menu to the top of the next button
	 */
	private int verticalMenuSpacing;

	/**
	 * The width of the graphical game button
	 */
	private int menuWidth;

	/**
	 * The height of the graphical game button
	 */
	private int menuHeight;

	/**
	 * The starting Y location of the menus.
	 */
	private int menuStartYPos;

	/**
	 * The desired ending point for the game menu. Currently this calculated
	 */
	private int endLocationX;

	/**
	 * The speed at which the menus will move accross the screen each
	 * logicalUpdate
	 */
	private int menuVelocity;

	/**
	 * Stores an instance of StateManager which will allow the MainMenu to
	 * register a basic set of states that this class can go into, for instance
	 * Animating
	 * 
	 * @see StateManager
	 */
	StateManager stateManager;

	/**
	 * Stores a list of the options available to the user within the game
	 * screen. The design for this is
	 * <p>
	 * options[i][0] is the text given to the graphical button
	 * </p>
	 * <p>
	 * options[i][1] is the GameScreen name that is being linked to. For
	 * instnace when the button is clicked, what the resulting gameScreen will
	 * be set to
	 * </p>
	 */
	// @formatter:off
	String[][] options = { { "Start New Game", TankSurvival.GameScreenNames.LEVEL_ONE },
			{ "Path Finding", TankSurvival.GameScreenNames.PATHFINDING_TESTS },
			{ "Sound Tests", TankSurvival.GameScreenNames.SOUND_TESTS },
			{ "Form Tests", TankSurvival.GameScreenNames.GUI_TESTS},
	};
	// @formatter:on

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new instance of the MainMenu class.
	 */
	public MainMenu() {
		super("mainMenu");
		background = new GameLayer("background");

		background.add(GameAssetManager.getInstance().getObject(GameGraphic.class, Configuration.GUI.Graphics.BACKGROUND_IMAGE));

		// Get the header and center it
		GameGraphic header = GameAssetManager.getInstance().getObject(GameGraphic.class, Configuration.GUI.Graphics.MENU_HEADER);
		header.setX(GameSettings.getGameWidth() / 2 - header.getWidth() / 2);

		background.add(header);

		menuOptions = new GameLayer("menuOptions");
		addGameLayer(background);
		addGameLayer(menuOptions);

		// Create the menuButtons required
		createMenuOptions();

		// Add the list of possible states that this MainMenu can undergo
		createStateManager();
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Iterates over the array options[][] which contains the Graphic Label and
	 * required gameScreen that it links to. If more buttons are required to be
	 * added, or removed, then it should be done by editing the options variable
	 */
	private void createMenuOptions() {
		String gameFontName = Configuration.GUI.Fonts.BUTTON_FONT;
		// Load all of the game graphics from the GUI congfiguration
		GameGraphic normalGraphic = GameAssetManager.getInstance().getObject(GameGraphic.class, Configuration.GUI.Graphics.GRAPHIC_BUTTON_NORMAL);
		GameGraphic hoverGraphic = GameAssetManager.getInstance().getObject(GameGraphic.class, Configuration.GUI.Graphics.GRAPHIC_BUTTON_HOVER);
		GameGraphic focusGraphic = GameAssetManager.getInstance().getObject(GameGraphic.class, Configuration.GUI.Graphics.GRAPHIC_BUTTON_FOCUS);

		/**
		 * Calculate the target locations for the menu, and apply the settings
		 */
		verticalMenuSpacing = 20;
		menuWidth = (int) normalGraphic.getWidth();
		menuHeight = (int) normalGraphic.getHeight();
		// Automatically position everything
		menuStartYPos = (GameSettings.getGameHeight() / 2) - (((options.length - 1) * (menuHeight + menuHeight)) / 2);
		endLocationX = GameSettings.getGameWidth() / 2 - menuWidth / 2;
		menuVelocity = 15;

		String fontName = Configuration.GUI.Fonts.BUTTON_FONT;

		String menuName = null, menuLinksTo = null;
		/**
		 * Iterate through each menu item and set the location to be below
		 * eachother with a spacing of verticalMenuSpacing
		 */
		for (int i = 0, x = -menuWidth, y = menuStartYPos + menuHeight + verticalMenuSpacing; i < options.length; i++, y += menuHeight + verticalMenuSpacing) {
			menuName = options[i][0];
			menuLinksTo = options[i][1];
			GameButtonGraphical button = new GameButtonGraphical(Helpers.concat(menuName, "-", menuLinksTo),
					GameAssetManager.getInstance().getClonedObject(GameFont.class, fontName), menuName, x, y,
					normalGraphic, hoverGraphic, focusGraphic) {
				@Override
				public void mousePressed() {
					super.mousePressed();
					// Instead of using the name we could also have passed in
					// the linkTo name in the constructor
					GameEngine.getGameScreenManager().setGameScreen(getName().substring(getName().indexOf("-") + 1));
				}
			};

			menuOptions.add(button);
		}
	}

	/**
	 * Creates a new instance of the StateManager and adds the list of possible
	 * states that this main menu can transition to. Its inital state is set to
	 * "animatingButtons". When this GameScreen is seen the buttons will come in
	 * from the left and right and center into the middle of the page. After
	 * centered it will allow for the buttons to be pressed and then it will
	 * enter an idle state.
	 */
	private final void createStateManager() {
		// I wonder if we can lazily load this each time somehow?
		stateManager = new StateManager("mainMenuStateManager");

		// Add the state which will animate the buttons coming into the screen
		// from either set. When the buttons have reached the middle it will
		// transition into the registerEvents state
		stateManager.addPossibleState(new State("animatingButtons") {
			@Override
			public void logicUpdate(GameTime gameTime) {
				// The current iteration within the foreach loop. foreach loop
				// is faster than for(get(i)), this is why it's outside
				int i = 0;
				int plannedVelocity;
				// iterate through each logical child added to the menuOptions
				// gamelayer, in this case it will be only the
				// GameButtonGraphical children
				for (ILogical logicalObject : menuOptions.getAllLogicalChildren()) {
					// Ilogical object doesn't implement IPositionable, so we
					// have to typecast to the right interface. This allows us
					// to update the x,y coords
					IPositionable option = (IPositionable) logicalObject;

					// Move right if endLocation > current x pos.
					// We set the plannedVelocity to either be the desired
					// menuVelocity given as a field within this class, or we
					// set it to the closest distance that it can be to the goal
					// location so that it doesn't 'overshoot' the mark given
					plannedVelocity = endLocationX > option.getX() ?
							Math.min((int) (endLocationX - option.getX()), menuVelocity) :
							Math.max(-menuVelocity, (int) (endLocationX - option.getX()));

					option.setX(option.getX() + plannedVelocity);
					// Transiton to the next State, if we've reached our goal
					// destination, progress to the next state. Otherwise the
					// next state is our own.
					setTransitionStateName(option.getX() == endLocationX ? "registerEvents" : getName());
				}
			}
		});

		// Registers the button's events by calling the method
		// registerButtonEvents
		stateManager.addPossibleState(new State("registerEvents") {
			@Override
			public void logicUpdate(GameTime gameTime) {
				registerButtonEvents();
				setTransitionStateName("idle");
			}
		});

		stateManager.addPossibleState(new State("idle") {
			@Override
			public void logicUpdate(GameTime gameTime) {
				setTransitionStateName(getName());
			}
		});
	}

	/**
	 * This method is called when buttons should now become eventable. Most
	 * likely this will be called after the initial introduction animation has
	 * completed
	 */
	public void registerButtonEvents() {
		for (ILogical logicalObject : menuOptions.getAllLogicalChildren()) {
			GameButton option = (GameButton) logicalObject;
			option.enableEvents();
		}
	}

	// ----------------------------------------------------------------
	// Logical
	// These methods are overridden over interface ILogicalObject
	// ----------------------------------------------------------------
	/**
	 * <p>
	 * During start up we must make sure that the cursor is set to the chosen
	 * game cursor
	 * </p>
	 * <p>
	 * This method has been overridden so that the menu buttons can be restarted
	 * back to the required positions, to the left and right of the screen.
	 * </p>
	 */
	@Override
	public void startUp(GameTime gameTime) {
		// Call the startUp methods of all the register GameLayers
		super.startUp(gameTime);
		// Change the mouse
		GameEngine.setCustomCursor(GameAssetManager.getInstance().getObject(BufferedImage.class,
				Configuration.GUI.Misc.GAME_MOUSE));

		// Realign all of the menus off to the side
		int x = -menuWidth;

		// I remember reading that a foreach loop is faster than get(i), i have
		// no source to back this up though. We iterate through each logical
		// child added to the menuOptions gamelayer, in this case it will be
		// only the GameButtonGraphical children, and restart them to their
		// desired positions
		/**
		 * Alternate between the menu item being to the left of the screen and
		 * to the right of the screen so that they can be zoomed in
		 */
		int i = 0;
		for (ILogical logicalObject : menuOptions.getAllLogicalChildren()) {
			GameButton option = (GameButton) logicalObject;
			option.setX(i++ % 2 == 0 ? -menuWidth : GameSettings.getGameWidth());
		}

		// Default the starting state of the stateManager to start animating the
		// menu buttons
		stateManager.setStartingState("animatingButtons");
	}

	/**
	 * As not every GameScreen has a StateManager class by default, we must
	 * explictly tell it to update. We could perhaps add the StateManager to a
	 * GameLayer, as it implements {@link ILogical}, but I would like to keep
	 * the logic seperate, as it doesn't make sense to add it to a specific
	 * Gamelayer
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		// Call the logic update of the super class, GameScreen, which updates
		// all of the GameLayers
		super.logicUpdate(gameTime);
		// Update the current state register in the StateManager
		stateManager.logicUpdate(gameTime);
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		// We must explicitly do this ourselves, as it is not registered to a
		// GameLayer. See the logicUpdate comments for more information
		stateManager.cleanUp();
	}
}
