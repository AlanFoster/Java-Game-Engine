package entitysystem.systems;

import java.util.List;

import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.IComponent;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.ChildSpatial;
import entitysystems.components.Children;
import entitysystems.components.Spatial;

/**
 * Maintains the Spatial of all children in relative to its parent. Processes
 * each entity updates the child's spatial to that of the parent, taking into
 * consideration the offset it originally provided when registering as a child
 * object. If the {@link Children} component field rotateWithParent is set to
 * true we also rotate child entity to that that of the parent that it
 * registered to.
 * <p>
 * This class also offers static methods to get all components of parent, or
 * similarly so all components of type T through recursion.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see Children
 */
public class ParentSystem extends ProcessEntitySystem {
	public ParentSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Children.class);
	}

	/**
	 * Processes each entity within the entity list and updates the child's
	 * spatial to that of the parent, taking into consideration the offset it
	 * originally provided when registering as a child object.
	 * <p>
	 * It is at this point that we also 'rotate' any children, if the
	 * {@link Children} component field rotateWithParent is set to true.
	 */
	@Override
	public void processEntity(Entity entity) {
		// Get the entity's currently spatial
		Spatial parentSpatialComponent = entity.getComponent(Spatial.class, true);
		// Get all of the child entities and update their position relative
		// to the 'parent' entity
		Children childrenComponent = entity.getComponent(Children.class);
		// Get the list of entities that are children
		// List<Entity> chilrenEntities = childrenComponent.children
		for (Entity child : childrenComponent.children) {
			// Get the actual X,Y location so that we can update it
			Spatial childSpatialActual = child.getComponent(Spatial.class);
			// Get the 'childSpatial', this is so we know where the child
			// originally wanted to be drawn relatively to the parent, so we can
			// use this to update the spatial correctly
			ChildSpatial childSpatialRelative = child.getComponent(ChildSpatial.class);

			// if the child's spatial is null, it means it hasn't been set yet,
			// and that we should create its stored 'relative' spatial. There's
			// better ways to do this obviously, but it's not important in the
			// grand scheme of things.
			if (childSpatialRelative.spatial == null) {
				childSpatialRelative.spatial = new Spatial(childSpatialActual.x, childSpatialActual.y,
						childSpatialActual.width, childSpatialActual.height);
			}

			childSpatialActual.x = parentSpatialComponent.x + childSpatialRelative.spatial.x;
			childSpatialActual.y = parentSpatialComponent.y + childSpatialRelative.spatial.y;
			// If this child entity relies on the parent's rotation, change the
			// entity's actual spatial
			if (childSpatialRelative.rotateWithParent) {
				childSpatialActual.setRotation(parentSpatialComponent.getRotation());
			}
		}
	}

	/**
	 * Uses recursion to get the children of an entity, and any children of that
	 * entity and any children of that entity and... Etc. And then gets all
	 * components of type T that we require.
	 * <p>
	 * This will be used for when we want a list of every Component under of
	 * type T that a child might have, for instance a health related component
	 * or damage reduction or something
	 * 
	 * @param entity
	 *            The parent entity that we wish to find all components of type
	 *            T for.
	 * @param componentList
	 *            A list of the type that we're searching for. For instance if
	 *            we're searching for all spatials it must be a list of
	 *            <Spatial>. Remember that java will pass this list in by
	 *            reference, so when we pass the list to this method it will
	 *            modify the original contents.
	 */
	public static <T extends IComponent> void getAllChildrenTComponents(Entity entity, Class<T> clazz, List<T> componentList) {
		Children childrenComponent = entity.getComponent(Children.class);

		// Our base test, stop recursion if we no longer have children
		if (childrenComponent == null) {
			return;
		}

		for (Entity child : childrenComponent.children) {
			// Add to the componentList any of the children's components that we
			// are searching for, and then call this function on the child (in
			// case that child also has children)
			T component = child.getComponent(clazz);
			if (component != null) {
				componentList.add(component);
			}

			getAllChildrenTComponents(child, clazz, componentList);
		}
	}

	/**
	 * Uses recursion to get the children of an entity, and any children of that
	 * entity and any children of that entity and... Etc. And then gets all
	 * components within the tree of children.
	 * <p>
	 * Note :: This is different from getAllChildrenTComponents which returns
	 * all components of a specific class, this method simply returns all
	 * components within the tree of children.
	 * 
	 * @param entity
	 *            The parent entity that we want to find all components of
	 * @param componentList
	 *            A list of IComponent that we can freely add to. Remember that
	 *            java will pass this list in by reference, so when we pass the
	 *            list to this method it will modify the original contents.
	 */
	public static List<IComponent> getAllChildrenComponents(Entity entity, List<IComponent> componentList) {
		Children childrenComponent = entity.getComponent(Children.class);

		// Our base test, stop recursion if we no longer have children
		if (childrenComponent == null) {
			return componentList;
		}

		for (Entity child : childrenComponent.children) {
			// Add to the componentList any of the children's components that we
			// are searching for, and then call this function on the child (in
			// case that child also has children)
			componentList.addAll(child.getAllComponents());
			getAllChildrenComponents(child, componentList);
		}

		return componentList;
	}
}