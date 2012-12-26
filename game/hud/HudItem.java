package hud;

import java.awt.Graphics2D;

import entitysystems.components.IHUDable;
import entitysystems.components.Spatial;

/**
 * This class is for all items that wish to be drawn directly to the HUDSystem.
 * This class should be extended and the required methods overrided and
 * implemented.
 * <p>
 * This class differs from {@link IHUDable} because IHUDable 
 * <p>
 * This class has been made abstract as it is expected the extending classes
 * will implement the required logic. IE, it by itself does nothing useful and
 * should not be instantiated
 * 
 * @author Alan Foster
 * @version 1.0
 */
public abstract class HudItem implements IHudItem {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * This spatial stores the x,y,width,height of the current HUDItem.
	 */
	protected Spatial spatial;

	/**
	 * By default Hud Items will be drawn to the top right of the screen,
	 * relative to the viewport. The logic within the HUDSystem will draw this
	 * with consideration of this HUDItem's width - which is set by the spatial
	 * field
	 */
	protected DrawRelativeToViewport drawLocation = DrawRelativeToViewport.TOP_RIGHT;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------

	/**
	 * Creates a new HUDItem with the default drawLocation of
	 * DrawRelativeToViewport.TOP_RIGHT and spatial values given
	 * 
	 * @param x
	 *            The X location of the HUDItem
	 * @param y
	 *            The Y location of the HUDItem
	 * @param width
	 *            The width of the HUDItem
	 * @param height
	 *            The height of the HUDItem
	 */
	public HudItem(float x, float y, float width, float height) {
		spatial = new Spatial(x, y, width, height);
	}

	/**
	 * Creates a new HUDItem with the the required draw location and spatial
	 * values given
	 * 
	 * @param drawLocation
	 *            The relative position that this huditem would potentially like
	 *            to draw at.
	 * @param x
	 *            The X location of the HUDItem
	 * @param y
	 *            The Y location of the HUDItem
	 * @param width
	 *            The width of the HUDItem
	 * @param height
	 *            The height of the HUDItem
	 */
	public HudItem(DrawRelativeToViewport drawLocation, float x, float y, float width, float height) {
		this(x, y, width, height);
		this.drawLocation = drawLocation;
	}

	/**
	 * This spatial should contain the x,y, width, height location of the hud
	 * item. The HUDSystem will make use of this by deciding where to draw
	 * relative to the screen by taking this spatia's x,y,width,height
	 * 
	 * @return The spatial object of the item that wishes to be drawn to the HUD
	 */
	@Override
	public Spatial getSpatial() {
		return spatial;
	}

	/**
	 * This stores this location of where the HUDItem would like to draw at.
	 * 
	 * @return The relative position that this huditem would potentially like to
	 *         draw at.
	 * @see DrawRelativeToViewport
	 */
	@Override
	public DrawRelativeToViewport getRelativeDraw() {
		return drawLocation;
	}

	/**
	 * The offsetX and offsetY for this argument will be given by the HUDSystem.
	 * This offsetX and offsetY will be determined by the RelativeDraw. For
	 * instance if the RelativeDraw enum is set to TOP_LEFT then the offsetX and
	 * offsetY will both be 0. However if it is BOTTOM_RIGHT then the HUDSystem
	 * will calculate the location of the bottom right hand side of the viewport
	 * and take into consideration this HudItem's width and height so that the
	 * HudItem will draw in the correct place, relative to the bottom right.
	 * 
	 * This means that there should be a level of consideration for keeping the
	 * Spatial field up to date
	 */
	@Override
	public abstract void draw(Graphics2D drawScreen, int offsetX, int offsetY);
}