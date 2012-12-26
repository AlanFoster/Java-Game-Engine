package entitysystems.components;

import java.awt.image.BufferedImage;

/**
 * This interface can be implemented by existing components that wish to be
 * trackable to the HUD. For instance by default an entity with the
 * {@link HUDableComponent} will not actually produce any statistics within the
 * gamescreen. Instead components must implement this interface after for it to
 * then be picked up by the HUD.
 * <p>
 * Hudable items will not need to explicitly register with the HUD system, as
 * long as the parent as the HUDableComponent attached, and the component has
 * implemented this it will work.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public interface IHUDable {
	/**
	 * Returns the current icon which will be used to represent an icon within
	 * the HUD
	 * 
	 * @return The BufferedImage that will be used when drawn to the HUD
	 */
	BufferedImage getHUDIcon();
	/**
	 * 
	 * @return A string of relevent information that wants to be drawn in
	 *         combination with the hud icon. For instance it may be useful to
	 *         present the statistics like "Health : 100/100"
	 */
	String getHUDDetails();
}
