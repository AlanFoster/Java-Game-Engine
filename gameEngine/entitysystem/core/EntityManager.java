package entitysystem.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

import engine.misc.GameLogging;

/***
 * This class manages and stores all entities and components. This manager does
 * not do anything with the components, it is simply used a data store. To
 * perform any manipulation to entities and their components it must be done
 * through a system.
 * 
 * @see EntitySystem
 * 
 *      Note :: This class 'lacks' support for polymorphism. This shouldn't be a
 *      problem as generally components shouldn't really extend eachother. There
 *      are two possible fixes for this however, either we can change the data
 *      structure of registeredComponents, or you could possibly attempt to work
 *      out if the component extends another class. For instance, consider the
 *      following code
 * 
 *      <pre>
 * <code>{@code
 * public static void main(String[] args){ 
 * 		IComponent cEntity = new C();
 * 		Class<?> superClass = cEntity.getClass();
 * 		while((superClass = superClass.getSuperclass()) != Object.class){
 * 			System.out.println(superClass);
 * 		}
 * 	}
 * 	
 * 	public static class A implements IComponent {
 * 	}
 * 
 * 	public static class B extends A {
 * 	}
 * 	
 * 	public static class C extends B {
 * 	}
 *  }</code>
 * </pre>
 * 
 *      Which will of course give the expected output of something similar to
 * 
 *      <pre>
 * class tests.ComponentTests$B
 * class tests.ComponentTests$A
 * </pre>
 * 
 *      So, ideally we can simply simply call the same add method to add that
 *      component's superclass on each iteration to the list of
 *      registeredComponents so that the other systems will pick it up as though
 *      it was a non-inherited component and won't treat it any differently.
 *      Which I think is clever.
 * 
 * 
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class EntityManager extends Observable {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(EntityManager.class);

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Stores the list of all registered entities
	 */
	private static List<Entity> entityList;

	/**
	 * The Key is the class type ie Spatial.class, the value is a hash map of
	 * entity to registered components of that type. Stores all currently
	 * registered components within the entity system, with the entity its
	 * attached to as the key. Note :: HashMap will replace duplicate keys.
	 * Currently this is not a problem when writing, but if it is required to
	 * have many of the same components (I'm not sure why you'd want that at
	 * all.. And would suggest a design flaw possibly) the data type should be
	 * changed
	 */
	private static HashMap<Class<? extends IComponent>, HashMap<Entity, IComponent>> registeredComponents;

	// ----------------------------------------------------------------
	// Constructor and Singleton methods
	// ----------------------------------------------------------------
	/**
	 * Creates a new EntityManager instance. This has been made private for the
	 * singleton
	 */
	private EntityManager() {
		entityList = new LinkedList<Entity>();
		registeredComponents = new HashMap<Class<? extends IComponent>, HashMap<Entity, IComponent>>();
	}

	/**
	 * stores the singleton instance, only when accessed will it be be
	 * instantiated
	 */
	private static class EntityManagerInstance {
		private static EntityManager instance = new EntityManager();
	}

	/**
	 * A public method that will return the current Singleton instance
	 * 
	 * @return The one instance of this class
	 */
	public static EntityManager getInstance() {
		return EntityManagerInstance.instance;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Removes an entity from the entity system. Once an entity is removed from
	 * the system it will remove any components attached to it too. A
	 * removeEntity call causes an explicit call to refreshEntityLists() which
	 * will automatically update any entity systems. This means that it will no
	 * longer be used by any systems from the instant a delete call is made.
	 * 
	 * @param entity
	 *            The entity to remove from the manager
	 */
	public void removeEntity(Entity entity) {
		// Delete any components owned by it too
		List<IComponent> entityComponentList = entity.getAllComponents();
		for (IComponent IComponent : entityComponentList) {
			// Get the class of the component
			Class<?> componentClass = IComponent.getClass();
			/*
			 * Remove the entity that is stored in the map of component's
			 * classes to entities and their components. It is possible that we
			 * could check whether or not
			 * registeredComponents.get(componentClass) is now of size 0, and we
			 * could set it the entire value to null for garbage collection, but
			 * this may cause more overhead than benefit in the long run, so we
			 * ONLY remove the entity and not the HashMap found with it
			 */
			registeredComponents.get(componentClass).remove(entity);
		}

		// Once all components have been deleted, we can delete it from our
		// entity list
		entityList.remove(entity);

		// Make an explicit call to refresh any system's entityLists, so we
		// don't get NullPointExceptions being thrown within our systems
		refreshEntityLists();
	}

	/**
	 * Adds a component to an entity.
	 * 
	 * @param entity
	 *            The entity that the component will be registered with
	 * @param component
	 *            The new component that will be given to the entity
	 * @param notifyObservers
	 *            If true we will make a method call to refresh all entity lists
	 *            within the systems. If false no call to refresh entity lists
	 *            will be made.
	 */
	public void addComponent(Entity entity, IComponent component, boolean notifyObservers) {
		HashMap<Entity, IComponent> entityComponentMap = registeredComponents.get(component.getClass());
		if (entityComponentMap == null) {
			logger.info("No previous occurence of this component existed before.",
					"Creating new set for it");
			entityComponentMap = new HashMap<Entity, IComponent>();
			registeredComponents.put(component.getClass(), entityComponentMap);
		}

		// Add it to the list of components
		entityComponentMap.put(entity, component);
		// Add it to the list of registered entities
		if (!entityList.contains(entity)) {
			entityList.add(entity);
		}

		/**
		 * We check this boolean for whether or not we should notify our
		 * observers This boolean is set so that we don't waste time constantly
		 * telling every single system to refresh their entityLists when there
		 * are many components being added to one entity.
		 */
		if (notifyObservers) {
			refreshEntityLists();
		}
	}

	/**
	 * This method offers a way of notifying all observers (in this case most
	 * likely all systems that extend the EntitySystem abstract class).
	 * 
	 * It is expected that the the listening systems will refresh their lists of
	 * entities/components that they are keeping track of most
	 * 
	 * @see EntitySystem
	 */
	public void refreshEntityLists() {
		setChanged();
		notifyObservers();
	}

	/**
	 * Gets all components registered by a specific entity
	 * 
	 * @param entity
	 *            The entity that we wish to get all the components for
	 * @return A list of IComponent which have been registered with an entity.
	 *         This will return an empty list if there are no components
	 *         registered with this entity.
	 */
	public List<IComponent> getAllComponents(Entity entity) {
		List<IComponent> componentList = new LinkedList<IComponent>();

		// Iterate over each value (which is the map of classes, basically
		// each possible component) within the system. Then check if there's
		// is a component of that type that belongs to the entity
		for (HashMap<Entity, IComponent> components : registeredComponents.values()) {
			IComponent component = components.get(entity);
			if (component != null) {
				componentList.add(component);
			}
		}

		return componentList;
	}

	/**
	 * Gets a /single/ component that is owned by an entity. If this entity does
	 * not have the required component, null will be returned. It's considered
	 * 'fast' because there is no cast to type T, I assume this will be used for
	 * debugging purposes, so we can simply get access to the toString method
	 * etc if required.
	 * 
	 * The returned object will be of IComponent, so perhaps this interface will
	 * allow more in the future.
	 * 
	 * @param clazz
	 *            The class of the component that we are searching for
	 * @param entity
	 *            The entity that this component is attached to
	 * @return The component of type IComponent, null if there was no
	 *         corresponding component with the entity found.
	 */
	public IComponent getComponentFast(Class<?> clazz, Entity entity) {
		HashMap<Entity, IComponent> entityComponentMap = registeredComponents.get(clazz);
		if (entityComponentMap != null) {
			return entityComponentMap.get(entity);
		}
		return null;
	}

	/**
	 * This method returns the component of an entity of type T.
	 * 
	 * @param clazz
	 *            The class of the component that we are searching for
	 * @param entity
	 *            The entity that this component is attached to
	 * @param required
	 *            If required is true we will output a logging message saying
	 *            that the component was not found on the entity. If false no
	 *            output message will be given.
	 * @return The component of type IComponent, null if there was no
	 *         corresponding component with the entity found.
	 */
	public <T extends IComponent> T getComponentAsType(Class<T> clazz, Entity entity, boolean required) {
		HashMap<Entity, IComponent> entityComponentMap = registeredComponents.get(clazz);
		// If this component existed, return their registered component
		if (entityComponentMap != null) {
			return clazz.cast(entityComponentMap.get(entity));
		}

		// Output logging that it failed if it was a required component type
		if (required) {
			logger.info("no component of type ", clazz, " found for entity ", entity);
		}
		return null;
	}

	/**
	 * Remove a component that has been attached to an entity.
	 * 
	 * @param clazz
	 *            The class of the component that needs removed
	 * @param entity
	 *            The entity that the component needs removed from
	 */
	public void removeComponent(Class<? extends IComponent> clazz, Entity entity) {
		registeredComponents.get(clazz).remove(entity);
	}

	/**
	 * Returns a list of entities containing ALL of the required components.
	 * This differs to getEntitiesContainingAny as it requires for the entity to
	 * have all of the componentClassList components
	 * 
	 * @param components
	 *            The list of components that an entity must contain
	 * @return A list of entities that contain ALL of the required components
	 */
	public List<Entity> getEntitiesContaining(Class<?>... componentClassList) {
		List<Entity> foundEntityList = new LinkedList<Entity>();
		// Iterate all 'possible' entity lists
		entityIteration: for (Entity entity : entityList) {
			// get the components owned by that entity
			for (Class<?> componentClass : componentClassList) {
				if (getComponentFast(componentClass, entity) == null) {
					continue entityIteration;
				}
			}
			// If we've reached this point, the entity contained all of the
			// required components
			foundEntityList.add(entity);
		}

		return foundEntityList;
	}

	/**
	 * This method differs from the method getEntitiesContaining in that this
	 * method returns a list of entity which contain at least one of any from
	 * the componentClassList
	 * 
	 * @param componentClassList
	 *            The list of components that an entity must contain
	 * @return A list of entities that contain at least one of the required
	 *         components
	 */
	public List<Entity> getEntitiesContainingAny(Class<?>... componentClassList) {
		List<Entity> entityList = new LinkedList<Entity>();
		for (Class<?> clazz : componentClassList) {
			// Get all entities that have this component
			HashMap<Entity, IComponent> entityMap = registeredComponents.get(clazz);
			// Remember we might get a null entityMap
			if (entityMap != null) {
				// Add all of the entities to the entityList
				entityList.addAll(new LinkedList<Entity>(entityMap.keySet()));
			}
		}
		return entityList;
	}

	/**
	 * Checks whether or not this entity exists within the entity manager. This
	 * can be useful to keep track of whether or not a component has been
	 * removed, IE for keeping track of lists that we want to know details about
	 * such as bullets or particles. Although it is suggested that there is no
	 * explicit references to entities, and that all such logic is handled
	 * through the entity list refreshes in the entity systems
	 * 
	 * @param entity
	 *            The entity that we are checking to see if it exists within the
	 *            entity manager
	 * @return True if the entity manager contains the entity, false if the
	 *         entity manager doesn't contain the entity
	 */
	public boolean contains(Entity entity) {
		return entityList.contains(entity);
	}

	/**
	 * Clears all entities and components from the system, this should generally
	 * be called at the end of a level where you plan to repopulate everything,
	 * or when the game has ended etc. <b>This should be called explicitly when
	 * required</b>
	 * <P>
	 * Note :: Components are purely data based, so there should be no need to
	 * call some sort of cleanUp() method on each entity/component. Also, the
	 * EntityManager does not care about any Systems that make use of the
	 * EntityManager, so it is not this classes' duty to perform any cleanup
	 * methods on them. Although, currently the GameEngine's GameLayer class
	 * DOES do this when registered for logical/render updates.
	 * 
	 * @see GameLayer
	 */
	public void clearAll() {
		entityList.clear();
		for (HashMap<Entity, IComponent> componentLists : registeredComponents.values()) {
			componentLists.clear();
		}
	}

	/**
	 * @return Returns a new UUID for the entity. This UUID is universally
	 *         unique so could potentially also be used for serialisation.
	 */
	public UUID createEntity() {
		UUID newID = UUID.randomUUID();
		return newID;
	}
}