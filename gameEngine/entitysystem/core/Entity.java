package entitysystem.core;

import java.util.List;
import java.util.UUID;

import engine.interfaces.ICloneable;
import engine.misc.Helpers;

/**
 * An entity is a way of collecting many individual chunks of data called
 * components. These components are used by the entity systems which will have
 * direct access to the fields of the components.
 * <p>
 * An entity which is created is automatically registered into the entity
 * manager and all of its components are automatically picked up by any systems.
 * (Providing the refreshList() method is called of the entity manager - which
 * refreshes the systems)
 * <p>
 * This class does NOT contain the components itself. It just offers methods of
 * adding methods to the entity manager that it stores a reference to.
 * 
 * @author Alan Foster
 * @version 1.0

 * @see EntitySystem
 * @see IComponent
 * @see EntityTemplate
 */
public class Entity {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The unique universal ID for this entity, which can be used for
	 * serialization
	 */
	private final UUID id;
	/**
	 * Access to the entity manager. We need this as we don't actually store any
	 * of the components within this class. Note, conceptually the entity
	 * shouldn't have this access to the entity manager, but it's a lot nicer
	 * typing entity.getComponent(Component.class) rather than
	 * entityManager.getComponent(entity, Component.class);
	 */
	private final EntityManager entityManager;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * @param entityManager
	 *            access to the entity manager so that we can provide programmer
	 *            friendly 'addComponent' methods
	 * @parma id the universally unique id that can be used for serialisation
	 *        etc.
	 * @param components
	 *            The variadic arg of components that can be given to this
	 *            component when it is created.
	 */
	public Entity(EntityManager entityManager, UUID id, IComponent... components) {
		this.entityManager = entityManager;
		this.id = id;

		// Add all of the components without causing a refresh of the entity
		// manager (which causes system list refreshes)
		for (IComponent component : components) {
			addComponentNoRefresh(component);
		}
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * @return the UUID given at the entity's creation
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * A private method that takes a parameter to show whether or not the
	 * EntityManager should tell any listening systems to update their
	 * EntityLists
	 * 
	 * @param component
	 *            The component to add
	 * @param refresh
	 *            whether or not to cause a refresh of the entity systems that
	 *            get their entities from the entity manager
	 * @return this current object, useful for chained events etc
	 */
	private Entity addComponent(IComponent component, boolean refresh) {
		entityManager.addComponent(this, component, refresh);
		/**
		 * Returning this object allows for chained method calls. For instance
		 * foo.addComponent(...).addComponent(...) etc
		 */
		return this;
	}

	/**
	 * Add a component to this entity. Calls the the method addComponent() with
	 * no request for an entity system refresh
	 * 
	 * @param component
	 *            The component to add
	 * @return The entity reference, useful for chained events etc
	 */
	public Entity addComponentNoRefresh(IComponent component) {
		return addComponent(component, false);
	}

	/**
	 * Add a component to this entity. Calls the the method addComponent()
	 * without an explicit entity system refresh
	 * 
	 * @param component
	 *            The component to add
	 * @return The entity reference, useful for chained events etc
	 */
	public Entity addComponentRefresh(IComponent component) {
		return addComponent(component, true);
	}

	/**
	 * 
	 * @return All components added to this entity of the type IComponent. If
	 *         type T is required, see getComponent(Class<T> clazz) instead
	 */
	public List<IComponent> getAllComponents() {
		return entityManager.getAllComponents(this);
	}

	/**
	 * 
	 * @param clazz
	 *            The class of the component added to the entity. This component
	 *            must obviously implement IComponent
	 * @return The component as type T
	 */
	public <T extends IComponent> T getComponent(Class<T> clazz) {
		return entityManager.getComponentAsType(clazz, this, false);
	}

	/**
	 * 
	 * @param clazz
	 *            The class of the component added to the entity. This component
	 *            must obviously implement IComponent
	 * @param required
	 *            A logger error will be given if required is true. (Not really
	 *            that useful I don't think)
	 * @return The component as type T
	 */
	public <T extends IComponent> T getComponent(Class<T> clazz, boolean required) {
		return entityManager.getComponentAsType(clazz, this, required);
	}

	/**
	 * Removes a component from the entity
	 * 
	 * @param clazz
	 *            The class of the component, IE Spatial.class
	 */
	public <T extends IComponent> void removeComponent(Class<T> clazz) {
		entityManager.removeComponent(clazz, this);
	}

	@Override
	public String toString() {
		return Helpers.concat("UUID :: ", id.toString(),
				" components :: ", entityManager.getAllComponents(this));
	}
}