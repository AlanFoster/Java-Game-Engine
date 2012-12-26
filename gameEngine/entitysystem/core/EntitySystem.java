package entitysystem.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import engine.interfaces.ILogical;
import engine.main.GameTime;

/**
 * The super class of all entity systems. Provides a basic structure for the
 * EntitySystems that might exist.
 * <p>
 * An entity system controls and manages a list of entity which contain certain
 * components. For instance a 'movement' system will require entities which
 * contain the components such as a Spatial (which stores the location and
 * width/height), and a Movement component, which will store the details of its
 * movement speed etc. The entity system implemented within this game engine are
 * entities and components of the purest form, and by this I mean they contain
 * ONLY the pure raw data, which are generally provided as public fields are
 * fast access. And any logic involved with entities are handled by the entity
 * system. There should be no logic in components what so ever. I believe a
 * possible exception to this may be scripted entities, but this Game Engine
 * does not offer a scripting language for you to do this sadly.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see EntityManager
 * @see IEntitySystem
 */
public abstract class EntitySystem extends Observable implements IEntitySystem {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * Access to the entity manager. We require access to the entity manager so
	 * that we can acquire any entities from the entity manager when required.
	 * We use this within the refreshList to get keep track of all entities with
	 * specific components that we will look after and perform logic on.
	 */
	protected EntityManager entityManager;

	/**
	 * Stores the current list of entities that the system has access to and
	 * will perform logic on, this should be updated during refreshLists
	 */
	protected List<Entity> entityList;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	/**
	 * Creates a new Entity system
	 * 
	 * @param entityManager
	 *            We require access to the entity manager so that we can acquire
	 *            any entities from the entity manager when required. We use
	 *            this within the refreshList to get keep track of all entities
	 *            with specific components that we will look after and perform
	 *            logic on.
	 */
	public EntitySystem(EntityManager entityManager) {
		this.entityManager = entityManager;

		entityList = new ArrayList<Entity>();
		// Populate our entityList values (see IEntitySystem and
		// EntityManager for further details)
		refreshList();

		// Observer the entityManager, we do this so that we know
		// entities/components have been added/removed to the EntityManager, so
		// we can update our collection of entityList. Similar to above with
		// refreshList()
		entityManager.addObserver(this);
	}

	/**
	 * Within this method you should initialize any variables required, such as
	 * setting the required components needed from an entity to perform whatever
	 * action the system performs. This will get called once before it receives
	 * its first logic update.
	 * <p>
	 * The opposite of this is cleanUp, which will get called when it is going
	 * to be destroyed
	 * <P>
	 * This should be overrode if required.
	 */
	@Override
	public void startUp(GameTime gameTime) {
	}

	@Override
	public void cleanUp() {
		// Null the list so that it can potentially be collected by garbage
		// collection, which is called by the GameScreenManager class when game
		// screens are changed. However, that's not to say that this cleanup
		// method may be called at a different point in time, such as a scenario
		// were a system is added/removed for a duration of time, but without
		// changing game screens
		entityList.clear();
	}

	/**
	 * This EntitySystem currently only observers the EntitySystem. When the
	 * entity system receives a new component it will update the notifiers.
	 * Currently this is when we update our current EntityList.
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof EntityManager) {
			refreshList();
		}
	}
}