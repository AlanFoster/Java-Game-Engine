package hud;

import engine.interfaces.IDrawable;
import engine.interfaces.ILogical;
import entitysystem.systems.HUDSystem;
import entitysystems.components.HUDableComponent;
import entitysystems.components.IHUDable;
import entitysystems.components.Spatial;

/**
 * Any object that wishes to be drawn to the HUD should implement this
 * interface. Remember that even though an object may implement this interface,
 * it will not implicitly be registered with the HUDSystem, instead it must do
 * this explicitly.
 * <p>
 * It is expected that an object extends the class 'HUDComponent', although it
 * is obviously not required.
 * <p>
 * Note, this HudSystem will delegate the task of logical updates to any objects
 * that register the HUBSystem.
 * <p>
 * If you are looking to register an Entity/Component with the HUD system then
 * you should instead be looking at {@link IHUDable}, which is used for specific
 * components that have been given to an entity such as the player and a health
 * component, or the player and a powerup etc
 * <p>
 * 
 * @see HUDSystem
 * @see HUDableComponent
 */
public interface IHudItem extends IDrawable, ILogical {
	/**
	 * This spatial should contain the x,y, width, height location of the hud
	 * item. The HUDSystem will make use of this by deciding where to draw
	 * relative to the screen by taking this spatia's x,y,width,height
	 * 
	 * @return The spatial object of the item that wishes to be drawn to the HUD
	 */
	Spatial getSpatial();
	
	/**
	 * This stores this location of where the HUDItem would like to draw at.
	 * 
	 * @return The relative position that this huditem would potentially like to
	 *         draw at.
	 * @see DrawRelativeToViewport
	 */
	DrawRelativeToViewport getRelativeDraw();
}