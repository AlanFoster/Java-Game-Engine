package world;

import hud.DrawRelativeToViewport;
import hud.HUDTimer;
import hud.MiniMapSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import templates.BlockTemplate;
import templates.EnemyTemplate;
import templates.PlayerTemplate;
import templates.TankShieldTemplate;
import world.WorldParser.LevelDetails;
import engine.interfaces.IDrawable;
import engine.interfaces.IObservable;
import engine.main.GameLayer;
import engine.main.GameScreen;
import engine.main.GameTime;
import engine.misc.Timer;
import engine.misc.GameLogging;
import engine.misc.GameObservervable;
import engine.misc.GameSettings;
import engine.misc.GameViewPort;
import engine.misc.Helpers;
import engine.misc.Location;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntityTemplate;
import entitysystem.systems.AISystem;
import entitysystem.systems.AnimateSystem;
import entitysystem.systems.CameraSystem;
import entitysystem.systems.CollisionSystem;
import entitysystem.systems.DegradeSystem;
import entitysystem.systems.DeleteSystem;
import entitysystem.systems.HealthSystem;
import entitysystem.systems.ParentSystem;
import entitysystem.systems.CollectableSystem;
import entitysystem.systems.FollowMouseSystem;
import entitysystem.systems.HUDSystem;
import entitysystem.systems.HealthBarSystem;
import entitysystem.systems.ParticleEmitterSystem;
import entitysystem.systems.PlayerControlledSystem;
import entitysystem.systems.PreviousSpatialSystem;
import entitysystem.systems.RenderSystem;
import entitysystem.systems.EntitySpawnSystem;
import entitysystem.systems.ShootingSystem;
import entitysystem.systems.TimedDeleteSystem;
import entitysystem.systems.VelocitySystem;
import entitysystems.components.MinimapDrawn;
import entitysystems.components.Spatial;
import eventhandling.IKeyboardEventable;
import eventhandling.IMouseEventable;
import eventhandling.KeyEventHandler;
import eventhandling.MouseEventHandler;

/**
 * This GameLayer manages 'the world', which we can give the label of a world.
 * Within this world we register the basic systems that we expect to use, such
 * as the {@link RenderSystem} and {@link CameraSystem} etc.
 * <P>
 * This class performs no logic of it self, other than passing on the logical
 * and drawing information to the systems that have registered to it (as it
 * extends GameLayer). This class simply acts the 'glue' holding all of the
 * systems together in a centralised class.
 * <p>
 * This {@link GameLayer} will most likely be 'owned' by the
 * {@link WorldManager}'s GameScreen. As this class extends {@link GameLayer} it
 * means that this world can be easily paused/unpaused etc. Currently the 'P'
 * key implements this pausing as required.
 * <p>
 * Note :: Even though this class may be 'owned' by {@link WorldManager}, this
 * class does not know anything about this class, and will simply communicate to
 * any listeners through the {@link GameObservervable} and it stores within its
 * field, so it's not a requirement for this {@link GameLayer} to be owned by
 * that specific {@link GameScreen}, as it is uncoupled form it.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameLayer
 * @see GameEngine
 * @see EntityManager
 */
public class World extends GameLayer implements IObservable {
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
	final static GameLogging logger = new GameLogging(GameLayer.class);

	/**
	 * Store a reference to the entity manager which keeps stores all entities
	 * and components. This manager does not do anything with the components, it
	 * is simply used a data store. To perform any manipulation to entities and
	 * their components it must be done through a system.
	 */
	final static EntityManager entityManager = EntityManager.getInstance();
	/**
	 * Store a reference to the GameAssetManager which will allow for us to
	 * directly load in any assets that we require
	 */
	final static GameAssetManager assetManager = GameAssetManager.getInstance();

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

	/**
	 * Encapsulate an object so we can pass any messages around, for instance
	 * when the level has ended etc.
	 **/
	protected GameObservervable observerObj;

	/**
	 * The tiled map that represents the world
	 */
	protected TiledMap map;

	/**
	 * The main viewport that all systems will have access to.
	 */
	protected GameViewPort mainViewPort;

	/**
	 * Direct access to some systems for ease
	 */
	/**
	 * The HUD System which draws out any details that sit above the world, IE
	 * player score etc
	 */
	protected HUDSystem hudSystem;
	/**
	 * The camera system which will
	 */
	protected CameraSystem cameraSystem;

	/**
	 * The scale of the drawn minimap. IE if there is 100 map tiles width, and
	 * the scale factor is 2, then the width will be 200 pixels
	 */
	protected static final int MINIMAP_SCALE_FACTOR = 2;

	/**
	 * The list of states that this world can be under
	 */
	public enum WorldState {
		// Set when time reaches 0
		TIME_OVER,
		// Set when the player has 0 health and 0 shield etc. IE, dead.
		PLAYER_DIED,
		// Set when the player completes the level
		LEVEL_COMPLETED,
		// The world is playing as expected
		PLAYING
	}

	/**
	 * The current WorldState that this class is under. When this is updated the
	 * listeners will be notified through the {@link GameObservervable} object
	 * 'observerObj'
	 */
	WorldState currentState = WorldState.PLAYING;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	public World() {
		super("world");

		// Initialise our observer object so we can pass messages around to any
		// systems
		observerObj = new GameObservervable();

		// The main viewport that should be shared by all the GameLayers that
		// require it
		// Note, the X,Y coords start from top left
		mainViewPort = new GameViewPort(0, 0,
				GameSettings.getGameWidth(),
				GameSettings.getGameHeight());

		add(map = new TiledMap(mainViewPort));

		createGenricEntitySystems();
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Create all of the systems that are required within the World. All of
	 * these should be 'generic', and should therefore not be populated with any
	 * data explicitly within this method. If you wish to a specific system,
	 * like an EntitySpawnSystem then it should do so in another method, to keep
	 * the logic tighly grouped and relevent.
	 * 
	 * @param viewPort
	 */
	protected void createGenricEntitySystems() {
		// Create the bounds for our camera system. This will be replaced when a
		// level is loaded and the bounds are specified by the map
		Spatial cameraBoundsSpatial = new Spatial(
				GameSettings.getGameWidth() / 2,
				GameSettings.getGameHeight() / 2,
				GameSettings.getGameWidth() / 2,
				GameSettings.getGameHeight() / 2);

		// Create all of the required generic systems
		RenderSystem renderSystem = new RenderSystem(entityManager, DrawOrder.values(), mainViewPort);
		PreviousSpatialSystem previousSpatialSystem = new PreviousSpatialSystem(entityManager);
		VelocitySystem velocitySystem = new VelocitySystem(entityManager);
		CollisionSystem collisionSystem = new CollisionSystem(mainViewPort, entityManager, map.getTileWidth());
		PlayerControlledSystem controllerSystem = new PlayerControlledSystem(entityManager, mouseEventHandler, keyEventHandler);
		cameraSystem = new CameraSystem(entityManager, mainViewPort, cameraBoundsSpatial);
		FollowMouseSystem followMouseSystem = new FollowMouseSystem(entityManager, mouseEventHandler, mainViewPort);
		AnimateSystem animateSystem = new AnimateSystem(entityManager);
		ParticleEmitterSystem particleEmitterSystem = new ParticleEmitterSystem(entityManager);
		TimedDeleteSystem timedDeleteSystem = new TimedDeleteSystem(entityManager);
		HealthBarSystem healthBarSystem = new HealthBarSystem(entityManager, mainViewPort);
		AISystem aiSystem = new AISystem(entityManager);
		ShootingSystem shootingSystem = new ShootingSystem(entityManager);
		hudSystem = new HUDSystem(entityManager);
		CollectableSystem collectableSystem = new CollectableSystem(entityManager, hudSystem);
		ParentSystem parentSystem = new ParentSystem(entityManager);
		HealthSystem healthSystem = new HealthSystem(entityManager);
		DegradeSystem destroyableSystem = new DegradeSystem(entityManager);
		DeleteSystem deleteSystem = new DeleteSystem(entityManager);

		/*
		 * Add all of our systems to this game layer. Note, all systems are
		 * logical by default (as they implement ILogicalObject), so will
		 * Receive the expected logic updates, such as
		 * startup/logicUpdate/cleanup, by this game layer. This is also true
		 * for the rendering, but only if the layer implements IDrawable
		 */
		add(previousSpatialSystem);
		add(collisionSystem);
		add(controllerSystem);
		add(cameraSystem);
		add(followMouseSystem);
		add(animateSystem);
		add(velocitySystem);
		add(particleEmitterSystem);
		add(timedDeleteSystem);
		add(renderSystem);
		add(healthBarSystem);
		add(collectableSystem);
		add(aiSystem);
		add(hudSystem);
		add(parentSystem);
		add(shootingSystem);
		add(healthSystem);
		add(destroyableSystem);
		add(deleteSystem);
	}

	/**
	 * Override the getVisibleDrawable method, which is normally used for
	 * culling. We do this as we will let our EntitySystems take care of any of
	 * the culling required.
	 */
	@Override
	protected List<IDrawable> getVisibleDrawable(List<IDrawable> allItems, float offsetX, float offsetY) {
		return allItems;
	}

	/**
	 * Creates the player entity.
	 */
	protected void createPlayer() {
		new PlayerTemplate().createEntity();
	}

	/**
	 * Allow for objects to register themself to the HUD as required. IE the
	 * game count down timer and minimap system.
	 */
	private final void createHUD() {
		createMiniMap();
		createHudGameTimer();
	}

	/**
	 * Creates and adds the MiniMapSystem to the HUD, which will graphically
	 * represent an overview of the world's tiled map, which is considered the
	 * 'terrain' (which is cached within the MiniMapSystem as it will not change
	 * frequently, if ever), and shows all entities which can be represented on
	 * the minimap, IE they contain the {@link MinimapDrawn} component (which
	 * the player and enemies currently have by default)
	 */
	private final void createMiniMap() {
		hudSystem.add(new MiniMapSystem(entityManager, DrawRelativeToViewport.BOTTOM_RIGHT,
				map.getMiniMapLocations(),
				map.getTileWidth(), map.getTileHeight(),
				mainViewPort,
				MINIMAP_SCALE_FACTOR));
	}

	/**
	 * Creates the HUD timer for the game, which counts down from the starting
	 * time to 0. When the counters timer reaches 0 it will change the current
	 * world's state to WorldState.TIMER_OVER, which will then notify all
	 * listening systems. It is assumed at this point the game will be over, but
	 * is a generic enough system for there to be 'minigames' which have
	 * counters etc.
	 */
	private final void createHudGameTimer() {
		/**
		 * Creates an item that can be added to the HUDSystem, using the
		 * recommended formatted provided through the comment's header.
		 */
		hudSystem.add(new HUDTimer() {
			// Initialisation block, we can change any of the fields that we
			// need to at this point.
			{
				drawLocation = DrawRelativeToViewport.TOP_RIGHT;
				// This should be populated from the XML, but i had no time.
				// Easy to change however!
				countDownTimer = new Timer(13371337);
			}

			// Called when our timer reaches 0. Call the updatecurrentstate
			// method, which will hold any state change logic
			@Override
			public void timeOver() {
				logger.info("world time came to end. Changing State to ", WorldState.TIME_OVER);
				updateCurrentState(WorldState.TIME_OVER);
			}
		});
	}

	/**
	 * Changes the current state and notifies all observers that have registered
	 * for messages. We send the actual WorldState to our observers, rather than
	 * the world object.
	 * 
	 * @param newState
	 *            The new world state that we will enter, which will be notified
	 *            to all observers
	 */
	protected void updateCurrentState(WorldState newState) {
		// Update our state to the new state
		currentState = newState;

		// Notify all of our observers about the new state
		observerObj.setChanged();
		observerObj.notifyObservers(currentState);
	}

	/**
	 * Creates a the enemy spawn points. Tests the EntitySpawnSystem which, At
	 * timed intervals (between a minimum and maximum value) will create a
	 * random instance of a template given to it and also at a random location
	 * given to it. There can be many instances of this entity spawner and there
	 * are no restrictions to the number of spawners required.
	 */
	public void createEntitySpawner() {
		// Create the possible spawn locations
		List<Location> enemySpawnPoints = new ArrayList<Location>() {
			{
				// Generate random points near to where the player spawns, for
				// testing.
				for (int i = 0; i < 30; i++) {
					add(new Location(Helpers.randomBetween(0, (int) map.getWidth()),
							Helpers.randomBetween(0, (int) map.getHeight())));
				}
			}
		};

		// Add to the list of possible templates that will be created by the
		// EntitySpawnSystem
		List<EntityTemplate> templateList = new ArrayList<EntityTemplate>() {
			{
				add(new EnemyTemplate());
				// add(new TankShieldTemplate());
			}
		};

		// spawn system using our spawn locations and template list for the
		// items that need spawned
		int maxSpawnCount = 30;
		int minRandomSpawn = 1000;
		int maxRandomSpawn = 2000;
		EntitySpawnSystem enemyManager = new EntitySpawnSystem(entityManager, "enemySpawner",
				maxSpawnCount,
				minRandomSpawn, maxRandomSpawn,
				enemySpawnPoints, templateList);
		add(enemyManager);
	}

	/**
	 * Creates a number of blocks which test the degrade component. which when
	 * being hit with another entity which has the collide component and damage
	 * component. Visually this component will 'degrade' at the area in which
	 * the collision has occurred.
	 */
	public void createTestDegradeBlocks() {
		int NUM_OF_TEST_DEGRADE_BLOCKS = 5;
		BlockTemplate DEGRADE_TEMPLATE = new BlockTemplate();
		// Iterate through all of the required number of blocks and create their
		// entity, manipulating their spatial to view them better to the right
		// of eachother
		for (int i = 0; i < NUM_OF_TEST_DEGRADE_BLOCKS; i++) {
			Entity newDegrade = DEGRADE_TEMPLATE.createEntity();
			Spatial newDegradeSpatial = newDegrade.getComponent(Spatial.class);
			newDegradeSpatial.x = 100 + 200 * i;
		}
	}

	/**
	 * Populate the world information with the given level details
	 * 
	 * @param levelDetails
	 *            The level details to populate the world with
	 */
	public void populateWorld(LevelDetails levelDetails) {
		map.loadMap(levelDetails.getMapAssetName());
	}

	/**
	 * Starts the level by instaniating the player entity as required, HUD
	 * system etc. It is at this point that we set the actual bounds of the
	 * cameraSystem as we have a loaded map at this point.
	 */
	@Override
	public void startUp(GameTime gameTime) {
		createPlayer();
		createEntitySpawner();
		createHUD();
		new TankShieldTemplate().createEntity();

		createTestDegradeBlocks();
		entityManager.refreshEntityLists();

		float halfViewportWidth = (mainViewPort.getWidth() / 2);
		float halfViewportHeight = (mainViewPort.getHeight() / 2);
		cameraSystem.updateBoundaries(halfViewportWidth, halfViewportHeight,
				map.getWidth() - halfViewportWidth, map.getHeight()
						- halfViewportHeight);

		super.startUp(gameTime);
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		// Explicitly tell the entityManager to clear everything
		entityManager.clearAll();
	}

	// ----------------------------------------------------------------
	// IObservable implementations
	// ----------------------------------------------------------------
	@Override
	public void addObserver(Observer o) {
		observerObj.addObserver(o);
	}

	@Override
	public void deleteObserver(Observer o) {
		observerObj.deleteObserver(o);
	}
}
