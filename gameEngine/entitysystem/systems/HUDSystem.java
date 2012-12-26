package entitysystem.systems;

import hud.DrawRelativeToViewport;
import hud.HUDTimer;
import hud.IHudItem;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import src.Configuration;

import engine.gui.GameFont;
import engine.interfaces.IDrawable;
import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.GameQueue;
import engine.misc.GameSettings;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystem.core.IComponent;
import entitysystem.core.IEntitySystem;
import entitysystems.components.HUDableComponent;
import entitysystems.components.IHUDable;

/**
 * The HUD System. This system is split into two distinct parts. It offers both
 * component based hud registering by getting all entities that have the
 * components which implement IHudable. These will automatically be
 * added/removed from the screen when components are added/removed from
 * enitites.
 * <p>
 * The second part is that it takes objects which want specific rendering to the
 * screen. For this you must implement IHudItem and register explicitly with
 * this system (which differs from the implicit registering offered through
 * components). This class makes use of {@link DrawRelativeToViewport} which is
 * used for placement within the screen
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see IHudItem
 * @see IHUDable
 * @see EntitySystem
 * @see Entity
 */
public final class HUDSystem extends EntitySystem implements IDrawableLogical, IEntitySystem {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(HUDSystem.class);
	/**
	 * This is the list of hudItems that wish to be rendered to the screen as
	 * part of the Hud System.
	 * 
	 * If you are looking to register an Entity/Component with the HUD system
	 * then you should instead be looking at {@link IHUDable} instead, which is
	 * used for specific components that have been given to an entity such as
	 * the player and a health component, or the player and a powerup etc
	 */
	private GameQueue<IHudItem> hudItemQueue;

	/**
	 * A list dedicated for 'new' components which an entity might have which
	 * will then be used to create the new HUD images. This is used in the
	 * method refreshList() for allocation reasons in order to hopefully stop GC
	 * from interrupting the game.
	 */
	private List<IHUDable> potentiallyNewHudableList;

	/**
	 * Stores the list of current HUD components.
	 */
	private List<IHUDable> currentHUDComponents;

	/**
	 * The GameFont that this HUDSystem uses, which it draws to the screen
	 */
	private GameFont hudComponentText;

	/**
	 * The starting location of HUD components to be drawn from. This is taken
	 * from the left hand side in pixels
	 */
	private int drawHudComponentsX = 10;

	/**
	 * The starting location of the HUD components to be drawn from. This is
	 * taken from the top left of the screen, in pixels.
	 */
	private int drawHudComponentsY = 10;

	/**
	 * This is defined as the distance from the bottom of one hud's graphic
	 * component to the top of the next one. For instance if an image is 100
	 * pixels high, and the drawHUdComponentSpacing is 50 pixels. Then the next
	 * hud component icon will be drawn at 150.
	 */
	private int drawHudComponentSpacingY = 10;

	/**
	 * The 'padding' around the viewport to draw components. For instance
	 * normally top left may be 0,0 when drawing. If our padding is 10 pixels,
	 * then drawing will begin from 10, 10.
	 */
	private int HudPadding = 20;

	/**
	 * 
	 * @param entityManager
	 *            Access the entityManager. Technically this isn't 'required' as
	 *            the EntityManager is a Singleton with static access, but
	 *            Dependancy injection is always nice i think.
	 */
	public HUDSystem(EntityManager entityManager) {
		super(entityManager);

		// Initialise our lists, read their field descriptions for further
		// details.
		currentHUDComponents = new ArrayList<IHUDable>();
		potentiallyNewHudableList = new ArrayList<IHUDable>(10);
		hudItemQueue = new GameQueue<IHudItem>();

		// Create a 'loading' text asset. This will obviously be replaced by the
		// actual message by whatever needs it, but i didn't want to risk having
		// a null string
		hudComponentText = GameAssetManager.getInstance().getClonedObject(GameFont.class, Configuration.GUI.Fonts.HUD);
		hudComponentText.setText("loading");

		// Populate our entityList values (see IEntitySystem and
		// EntityManager for further details)
		refreshList();

		// Observer the entityManager, we do this so that we know
		// entities/components have been added/removed to the EntityManager, so
		// we can update our collection of entityList. Similar to above with
		// refreshList()
		entityManager.addObserver(this);
		refreshList();

	}

	/**
	 * Draws the HUD in two stages, firstly the entity related information. And
	 * secondly the HudItems which have been added to the system
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
		drawIHudable(drawScreen, offsetX, offsetY);
		drawHudItems(drawScreen, offsetX, offsetY);
	}

	/**
	 * This method will be called from the main overridden draw method
	 * 
	 * The IHudable list is used is used for specific components that have been
	 * given to an entity such as the player and a health component, or the
	 * player and a powerup etc
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
	private final void drawIHudable(Graphics2D drawScreen, int offsetX, int offsetY) {
		// Iterate over our list of items which want to be shown on the HUD and
		// grab their graphic icons, ie [HealthImage] as mentioned within this
		// method's header comments
		int drawY = drawHudComponentsY;
		for (IHUDable hudable : currentHUDComponents) {
			drawScreen.drawImage(hudable.getHUDIcon(), drawHudComponentsX + HudPadding, drawY + HudPadding, null);

			hudComponentText.setText(hudable.getHUDDetails());
			hudComponentText.draw(drawScreen, offsetX + drawHudComponentsX + hudable.getHUDIcon().getWidth() + HudPadding,
					offsetY + HudPadding + (int) (drawY - (hudComponentText.getHeight() / 2) + (hudable.getHUDIcon().getHeight() / 2)));

			// Increment the drawX position to include this current HUD icon's
			// height, and the spacing which is needed for next time
			drawY += hudable.getHUDIcon().getHeight() + drawHudComponentSpacingY + HudPadding;
		}
	}

	/**
	 * Draw the specific HudItems that want drawn to the screen. This differs to
	 * the IHudable class, which contains components given to an entity. A
	 * huditem would include things like a minimap, or count down timer etc.
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
	private final void drawHudItems(Graphics2D drawScreen, int offsetX, int offsetY) {
		for (IHudItem hudItem : hudItemQueue) {

			// Decide where the object should be drawn. Call the draw method of
			// the hudItem as IHudItem extends IDrawable
			switch (hudItem.getRelativeDraw()) {
				case CENTER:
					hudItem.draw(drawScreen,
							(int) (offsetX + (GameSettings.getGameWidth() / 2) - (hudItem.getSpatial().width / 2)),
							(int) (offsetY + (GameSettings.getGameHeight() / 2) - (hudItem.getSpatial().height / 2)));
					break;
				case TOP_LEFT:
					hudItem.draw(drawScreen,
							(int) (offsetX + HudPadding),
							offsetY + HudPadding);
					break;
				case TOP_RIGHT:
					hudItem.draw(drawScreen,
							(int) (offsetX + (GameSettings.getGameWidth() - hudItem.getSpatial().width)) - HudPadding,
							offsetY + HudPadding);
					break;
				case BOTTOM_LEFT:
					hudItem.draw(drawScreen,
							(int) (offsetX + HudPadding),
							(int) (offsetY + (GameSettings.getGameHeight() - hudItem.getSpatial().height - HudPadding)));
					break;
				case BOTTOM_RIGHT:
					hudItem.draw(drawScreen,
							(int) (offsetX + (GameSettings.getGameWidth() - hudItem.getSpatial().width)) - HudPadding,
							(int) (offsetY + (GameSettings.getGameHeight() - hudItem.getSpatial().height - HudPadding)));
					break;
				default:
					// This shouldn't really happen, but I guess there's always
					// null that could appear out of no where.
					logger.error("type not supported within drawHudItems ", hudItem.getRelativeDraw());
					break;
			}
		}
	}

	/**
	 * Get an entity with the {@link HUDableComponent} attached to them, and get
	 * all components which implement the {@link IHUDable} (non-Javadoc)
	 * 
	 * @see entitysystem.core.IEntitySystem#refreshList()
	 */
	@Override
	public void refreshList() {
		List<Entity> hudComponentList = entityManager.getEntitiesContaining(HUDableComponent.class);

		// Check if there are indeed any entities that want to be shown on the
		// HUD
		if (hudComponentList.size() > 0) {
			// Get the Entity that wants to be tracked to the HUD. HUD supports
			// only one entity atm
			Entity entity = hudComponentList.get(0);

			// Get a list of all of the components that this entity has. We do
			// this so we can check if any of those components implement the
			// interface IHUDable Note, we also need to get all of the
			// components of the any children entities (and if they have any
			// children themselves etc)
			List<IComponent> allComponentsList = entity.getAllComponents();
			ParentSystem.getAllChildrenComponents(entity, allComponentsList);

			// Our entity might be a parent entity, and contain children
			// entities which hold the IHUdable instead of the parent (Perhaps
			// like a shield for instance?) We must find these and add them to
			// our component list

			// Clear our preallocate store of 'new' hudable components
			potentiallyNewHudableList.clear();

			// iterate all of the components of the entity and check if that
			// element implements the interface IHUDable, which provides the
			// methods for getting the hudable's icon and output message
			// currently
			for (IComponent component : allComponentsList) {
				if (component instanceof IHUDable) {
					potentiallyNewHudableList.add((IHUDable) component);
				}
			}

			currentHUDComponents.clear();
			currentHUDComponents.addAll(potentiallyNewHudableList);
		}
	}

	@Override
	public void startUp(GameTime gameTime) {
		for (IHudItem hudItem : hudItemQueue) {
			hudItem.startUp(gameTime);
		}
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
		// Call any 'startUps' required
		updateGameQueue(gameTime);
		for (IHudItem item : hudItemQueue) {
			item.logicUpdate(gameTime);
		}
	}

	/**
	 * This method calls the cleanup method of all hudItems that it manages.
	 * This method will be called when an object is about to be removed from its
	 * parent, so presumably this cleanup will be called at the end of the game.
	 */
	@Override
	public void cleanUp() {
		// Call the clean up of all our items, so that GC can come visit. Then
		// clear our list of items
		for (IHudItem item : hudItemQueue) {
			item.cleanUp();
		}
		hudItemQueue.clear();
	}
	/**
	 * Updates all of the huditems whch are just being added to the HUD, and
	 * also tells the GameQueue to remove any waiting to be deleted HudItems
	 * 
	 * @param gameTime
	 */
	private void updateGameQueue(GameTime gameTime) {
		// Iterate and call the start ups of any hudItems which are
		// waited to be added to the HUDSystem explicitly
		for (IHudItem hudItem : hudItemQueue.getAllWaiting()) {
			hudItem.startUp(gameTime);
		}

		// Get the list of HUDItems which wanted to be removed from the
		// GameQueue, they are now removed (instead of at the time, which
		// could've caused concurrent deletion problems)
		for (IHudItem hudItem : hudItemQueue.getAllWaitingDeletion()) {
			hudItem.cleanUp();
		}

		// Tell the GameQueue to clear its list of deleting, and move its list
		// of waiting to its list of current objects
		hudItemQueue.updateLists();
	}
	
	/**
	 * When adding an IHudItem to this system we will manage its drawing and
	 * logical updates so that it can be drawn to the HUDSystem screen
	 * 
	 * @param hudItem
	 *            The new hud item which needs removed
	 */
	public void add(IHudItem hudItem) {
		hudItemQueue.add(hudItem);
	}
	/**
	 * Remove a hud item from the screen during the next logical update. When it
	 * is removed it will no longer be drawn to the HUD.
	 * 
	 * @param hudItem
	 *            The hudItem which will be removed from the HUD
	 */
	public void remove(IHudItem hudItem) {
		if (!hudItemQueue.remove(hudItem)) {
			logger.info("tried to remove a hud item that didn't exist");
		}
	}

	/**
	 * Creates a tempoary message which is drawn to the hud. This allows for the
	 * creation of a timed HUD message, which will disappear after the duration
	 * given to it.
	 * 
	 * @param message
	 *            The message wanted to be shown
	 * @param duration
	 *            The duration in milliseconds for the message to appear for
	 * @param drawLocation
	 *            The draw location in relation to the gamescreen
	 */
	public void addTimedMessage(String message, int duration, DrawRelativeToViewport drawLocation) {
		add(new HUDTimer(message, drawLocation, duration) {
			@Override
			public void timeOver() {
				remove(this);
			}
		});
	}
}
