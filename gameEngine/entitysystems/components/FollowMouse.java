package entitysystems.components;

import entitysystem.core.IComponent;
import entitysystem.systems.FollowMouseSystem;

/**
 * Add this component to an entity if it wishes to follow the mouse. This
 * component will be handled by the MouseFollowingSystem. When following the
 * mouse it will also take into consideration the viewport location.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see FollowMouseSystem
 */
public class FollowMouse implements IComponent {
}