package entitysystems.components;

import entitysystem.core.IComponent;
import entitysystem.systems.HUDSystem;

/**
 * Give this any component to any entity that requires to be tracked within the
 * HUD.
 * <p>
 * When this HUDComponent is given to an entity, the HUD System will search for
 * all components that implement the interface IHUDable and then draw the
 * details onto the screen automatically, providing that it has been
 * instantiated and is receiving logical/draw updates of course.
 * <p>
 * Examples of such components Hudable components are the Health component.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see HUDSystem
 */
public class HUDableComponent implements IComponent {
}