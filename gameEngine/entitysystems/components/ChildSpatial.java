package entitysystems.components;

import entitysystem.core.IComponent;
import entitysystem.systems.ParentSystem;

/**
 * A child spatial is given to an object which is binded to a parent entity. On
 * each update entity which is owned by a parent will have their position
 * automatically updated. However, since we will be modifying the original
 * spatial of the entity so that collision detection, and other systems, know of
 * the entitys actual location, we must store the orignal spatial of the entity
 * when it became a child. This is how we measure the 'offset' from the parent
 * entity.
 * <p>
 * This class offers the boolean of whether or not a child entity should rotate
 * with the parent. If true this entity will share the same rotation as the
 * parent that owns it. If false, this entity will have its own rotation as
 * desired
 * <p>
 * As with all components, this class does not contain any logic instead this is
 * contained within the system. In this case it is the {@link ParentSystem}
 * which handles such logic.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see Children
 * @see ParentSystem
 * @see Spatial
 */
public class ChildSpatial implements IComponent {
	/**
	 * Keep track of the entity's originally desired spatial. This value is set
	 * initally by the ChildSystem. This is used to keep track of the relative
	 * spatial that the child entity may have. For instance of the child entity
	 * has a spatial with x being -20, and y being -20. We set this spatial be
	 * -20, -20, and when updating the spatial of components within the
	 * ChildSystem we can keep track of this relative spacial, and update the
	 * entity's actual position relatively. In other words, parent.X - 20,
	 * parent.Y - 20 in this scenario
	 */
	public Spatial spatial;

	/**
	 * Rotate with parent. If true this entity will share the same rotation as
	 * the parent that owns it. If false, this entity will have its own rotation
	 * as desired
	 */
	public boolean rotateWithParent;

	/**
	 * 
	 * @param rotateWithParent
	 *            If true this entity will share the same rotation as the parent
	 *            that owns it. If false, this entity will have its own rotation
	 *            as desired
	 */
	public ChildSpatial(boolean rotateWithParent) {
		this.rotateWithParent = rotateWithParent;
	}
}
