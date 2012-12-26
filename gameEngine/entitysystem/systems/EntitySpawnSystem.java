package entitysystem.systems;

import java.util.List;

import engine.main.GameTime;
import engine.misc.Timer;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import engine.misc.Location;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystem.core.EntityTemplate;
import entitysystem.core.IComponent;
import entitysystems.components.NameTag;
import entitysystems.components.Spatial;

/**
 * At timed intervals (between a minimum and maximum value) this class will
 * create a random instance of a template given to it and also at a random
 * location given to it.
 * <p>
 * There can be many instances of this entity spawner and there are no
 * restrictions to the number of spawners required.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class EntitySpawnSystem extends EntitySystem {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(EntitySpawnSystem.class);
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * All of the possible spawn points that can be picked from when an entity
	 * is spawned
	 */
	private List<Location> spawnPoints;
	/**
	 * The templates that are registered with the entityspawnsystem, the
	 * createEntity method will bec alled from this object and it will
	 * automatically create an entity within the game world
	 */
	private List<EntityTemplate> templates;

	/**
	 * A component that is used to identify what system created the entity. The
	 * contents of this class don't really matter to much of a degree. It's
	 * unlikely that we'll ever need to say "what system created you", but even
	 * so, it's here. When refresh list is called to find all of the entities
	 * that have been created by this method we use the == comparator to find
	 * exact matches, so it's 'OK' to have two systems with the same name
	 * (although obviously, they shouldn't.)
	 */
	private final SpawnSystemTag spawnNameTag;

	/***
	 * Used to keep track of the current amount of time that has decreased on
	 * each game loop. When this timer reaches zero an entity will be spawned
	 * from the list of possible templates and spawn points.
	 */
	private Timer countdownTimer;

	/**
	 * The maximum amount of entities that can be spawned by this system at once
	 */
	private int maximumSpawnCount;
	/**
	 * The minimum time before an entity is spawned. Note :: This is the minimum
	 * time before an ATTEMPT to spawn is made. IE, if maximum spawn count
	 * restricts our entity from being spawned
	 */
	private long minSpawnTime;
	/**
	 * The maximum time before an entity is spawned. Note :: This is the minimum
	 * time before an ATTEMPT to spawn is made. IE, if maximum spawn count
	 * restricts our entity from being spawned
	 */
	private long maxSpawnTime;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * 
	 * @param entityManager
	 *            Access to the EntityManager that we will get our entities from
	 * @param spawnName
	 *            The tag given to all items spawned by this system @see
	 *            {@link NameTag}
	 * @param maximumSpawnCount
	 *            The maximum amount of enties this spawner can create at once.
	 * @param minSpawnTime
	 *            The minimum amount of time before spawning
	 * @param maxSpawnTime
	 *            The maximum amount of time before spawning
	 * @param spawnPoints
	 *            The locations that an entity can possibly be spawned from.
	 *            This will be picked at random when a random entity is going to
	 *            be spawned
	 * @param templateList
	 *            The list of all possible templates that will be created, these
	 *            create the entities for us. {@link EntityTemplate}
	 */
	public EntitySpawnSystem(EntityManager entityManager, String spawnName,
			int maximumSpawnCount, long minSpawnTime, long maxSpawnTime,
			List<Location> spawnPoints,
			List<EntityTemplate> templateList) {
		super(entityManager);

		this.spawnPoints = spawnPoints;
		this.templates = templateList;

		// Create the new timer required to do this
		countdownTimer = new Timer();

		this.minSpawnTime = minSpawnTime;
		this.maxSpawnTime = maxSpawnTime;

		this.maximumSpawnCount = maximumSpawnCount;

		setRandomTime();

		// Create a spawn name tag, this will be attached to
		// all entities that are created by this SpawnSystem. We will use
		// this for debugging to see what created an entity, and also to
		// keep track of how many total objects we have created, which will
		// be used to see if we are allowed to spawn more (IE when total
		// spawned < maximumSpawnCount)
		spawnNameTag = new SpawnSystemTag(spawnName);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------

	/**
	 * Sets our countdown to be a time between the minimum and maximum spawn
	 * time value
	 */
	private final void setRandomTime() {
		countdownTimer.setTime(minSpawnTime + (int) (Math.random() * (maxSpawnTime - minSpawnTime)));
	}

	/**
	 * Picks a random entity from our templates and sets the entity's spatial to
	 * a random x,y location
	 */
	private final void addRandomEntity() {
		// Get our list of templates and create a random entity from the
		// template
		Entity spawnedEntity = templates.get((int) (templates.size() * Math.random())).createEntity();
		// Get a random spawn point and replace the spawnedEntity's spatial
		// values with one from the list of possible spawn locations given to
		// this EntitySpawnSystem
		Location randomLocation = spawnPoints.get((int) (spawnPoints.size() * Math.random()));

		Spatial spawnedEntiySpatial = spawnedEntity.getComponent(Spatial.class);

		spawnedEntiySpatial.x = randomLocation.getY();
		spawnedEntiySpatial.y = randomLocation.getY();

		// Add our identifying tag to the entity
		spawnedEntity.addComponentRefresh(spawnNameTag);
	}

	// ----------------------------------------------------------------
	// EntitySystem overrides
	// ----------------------------------------------------------------
	@Override
	public final void logicUpdate(GameTime gameTime) {
		countdownTimer.decreaseTimer(gameTime.getElapsedTimeMilli());
		/**
		 * When the timer is finished we must make sure that we first create one
		 * of the random entities that this spawner can possibly create, and
		 * then call the reset time method.
		 */
		if (countdownTimer.isFinished()) {
			// Only create an entity if we are under our maximum spawn count
			if (entityList.size() < maximumSpawnCount) {
				addRandomEntity();
			} else {
				logger.info("Didn't add an entity, there were already too many");
			}
			setRandomTime();
		}
	}

	@Override
	public final void refreshList() {
		entityList.clear();
		// Update our entityList to only bother with enemies which contain
		// OUR specific SpawnSystemTag.
		List<Entity> entitesWithSpawnTags = entityManager.getEntitiesContaining(SpawnSystemTag.class);
		// Only keep track of our own spawn tag list
		for (Entity entity : entitesWithSpawnTags) {
			if (entity.getComponent(SpawnSystemTag.class) == spawnNameTag) {
				entityList.add(entity);
			}
		}
	}

	/**
	 * A component that is used to identify what system created the entity. The
	 * contents of this class don't really matter to much of a degree. It's
	 * unlikely that we'll ever need to say "what system created you", but even
	 * so, it's here. When refresh list is called to find all of the entities
	 * that have been created by this method we use the == comparator to find
	 * exact matches, so it's 'OK' to have two systems with the same name
	 * (although obviously, they shouldn't.)
	 */
	private final static class SpawnSystemTag implements IComponent {
		public static String identifier;

		public SpawnSystemTag(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public String toString() {
			return Helpers.concat("[spawned item from ", identifier, "]");
		}
	}
}