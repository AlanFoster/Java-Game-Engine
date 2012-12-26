package entitysystem.core;

import engine.main.GameTime;

/**
 * This class provides methods for 'processing' an entity. As it is expected
 * many systems will simply 'process' an entity in the same manner each time we
 * have provided this abstract class which provides the basic logic that is
 * expected from such entity systems.
 * <p>
 * If you wish to create an entity system which does not 'process' and entity,
 * IE, a drawing system for example, then you should instead extend
 * {@link EntitySystem} directly.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see EntitySystem
 * @see EntityManager
 */
public abstract class ProcessEntitySystem extends EntitySystem {
	/**
	 * Creates a new entity processing Entity system
	 * 
	 * @param entityManager
	 *            We require access to the entity manager so that we can acquire
	 *            any entities from the entity manager when required. We use
	 *            this within the refreshList to get keep track of all entities
	 *            with specific components that we will look after and perform
	 *            logic on.
	 */
	public ProcessEntitySystem(EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * This method will be called during the update cycle of the game loop.
	 * During this time we will loop through each entity within our entityList
	 * (which stores all entites that this system manages), and call their
	 * process entity method.
	 * <p>
	 * The processEntity method is abstract and therefore any extending system
	 * must provide this logic themselves, depending on the system.
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		for (Entity entity : entityList) {
			processEntity(entity);
		}
	}

	/**
	 * All extending classes must provide their own implementation for this as
	 * we will be processing each entity which is within their entityList during
	 * the logical update
	 */
	public abstract void processEntity(Entity entity);
}
