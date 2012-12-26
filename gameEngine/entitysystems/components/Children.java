package entitysystems.components;

import java.util.ArrayList;
import java.util.List;

import engine.misc.Helpers;
import entitysystem.core.Entity;
import entitysystem.core.IComponent;
import entitysystem.systems.ParentSystem;

/**
 * This component should be given to an enemy that will potentially have 'child'
 * entities. For instance, an entity composed of smaller sub-entities. A good
 * example of this would be a tank. A tank itself is an itself, composed of
 * seperate entities such as the barrel and the tank body.
 * 
 * All children will be handled by the ChildSystem which will simply move every
 * child's entity location to the parent's location. This means that collision
 * detection, and drawing etc will be 'relative' to the parents top left
 * position. IE, 0,0 is the top left.
 * 
 * Notes :: All children added to this system should have a Spatial and a
 * ChildSpatial. The spatial component will be used as its 'actual' position.
 * Initially this spatial should contain the values of the desired relative
 * location. The entity should also have a 'ChildSpatial', this will then be
 * used to store the entity's original spatial location when it is first met by
 * the ChildSystem. The reason for doing it this way is that it allows for the
 * entity's actual x,y to be manipulated, taking into consideration its desired
 * relative coordinates, so that it doesn't affect any of the other systems. IE,
 * rendering and collision detection etc will work as usual.
 * 
 * For instance, if the Spatial is -20, -20, this means that from that point on
 * the X,Y position of the spatial will be set to the parent's x,y spatial minus
 * 20. And the ChildSpatial will now contain the spatial of -20, - 20
 * 
 * @See {@link ParentSystem}
 * @see {@link ChildSpatial}
 * 
 */
public class Children implements IComponent {
	// This entity is encapsulated
	public List<Entity> children;

	// Take note of the child's stating x,y pos in particular, so that the
	// child updating system can move to exactly the required position
	// Spatial childSpatial;

	public Children(Entity... childs) {
		// Create a list list with the size of child's array
		children = new ArrayList<Entity>(childs.length);

		for (Entity child : childs) {
			Spatial childSpatialComponent = child.getComponent(Spatial.class);
			Equipment childEquipmentComponent = child.getComponent(Equipment.class);
			if(childSpatialComponent != null && childEquipmentComponent != null){
				childSpatialComponent.x = childEquipmentComponent.x;
				childSpatialComponent.y = childEquipmentComponent.y;
			}
			children.add(child);
		}
	}

	public void addChildEntity(Entity entity) {
		children.add(entity);
	}

	@Override
	public String toString() {
		return Helpers.concat("[children :: ", children, "]");
	}
}