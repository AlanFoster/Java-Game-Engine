package entitysystem.core;

// Creates an entity of the type we expect. It is different to that of a
// factory.
/**
 * This method is used to conceptually store a reproducible entity in a single
 * class. For instance if there is a need to store a 'template' of an enemy for
 * instance, we will create an enemy class which extends EntityTemplate and
 * implements the abstract method. When CreateEntity is called it should
 * register a new entity with all of the basic components that an enemy entity
 * is expected to have, and then it will return that entity created to whatever
 * class called it originally.
 * <p>
 * Conceptually this is an awesome idea as it will allow for us to easily create
 * new instances of entities which can be created many times, and will allow us
 * the flexiblity to cache certain components and resources that are shared
 * accross entities, for instance this EntityTemplate could match in well with
 * the flyweight pattern.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public abstract class EntityTemplate {
	/**
	 * Direct access to the entity manager for creating our new entities from
	 */
	protected static final EntityManager entityManager = EntityManager.getInstance();

	EntityTemplate instance;

	/**
	 * Creates a new entity which will be handled automatically by the entity
	 * system, and then returns the reference to it. This method should be
	 * overridden and the logic performed to create entities and their
	 * components.
	 */
	public abstract Entity createEntity();
}