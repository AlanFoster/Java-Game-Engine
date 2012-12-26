package engine.misc;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;

import engine.interfaces.IPositionable;
import engine.main.GameLayer;
import entitysystem.systems.CameraSystem;

/**
 * The viewport is conceptually viewed as 'the viewing' area within this game
 * engine. This class is made use of by the {@link GameLayer} and any entity
 * systems which make use of this {@link GameViewPort} such as the
 * {@link CameraSystem}.
 * <p>
 * This class offers many differnet ways of checking whether or not an object is
 * currently 'visible' within this {@link GameViewPort}, which should be taken
 * advantage of when visually culling objects from being drawn. Currently the
 * {@link GameLayer} and any entity systems provided by this GameEngine do this
 * by default.
 * <p>
 * This viewport can either be centered on a specific X,Y or have its X,Y
 * coordinates be set in terms of the top left position if required through the
 * methods offered within this class.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameLayer
 */
public class GameViewPort implements IPositionable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameViewPort.class);

	/**
	 * Store the dimensions of this object, IE, the X,Y,width and height
	 */
	private Rectangle2D.Float dimensions;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * 
	 * @param x
	 *            The center X location of this viewport
	 * @param y
	 *            The center Y location of this viewport
	 * @param width
	 *            the width of this viewport
	 * @param height
	 *            The height of this viewport
	 */
	public GameViewPort(int x, int y, int width, int height) {
		dimensions = new Rectangle2D.Float();
		setDimensions(x, y, width, height);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Returns true if the viewport intersects with these required dimensions
	 * 
	 * @param x
	 *            The X coordinate, with 0,0 being top left
	 * @param y
	 *            The Y coordinate, with 0,0 being top left
	 * @param width
	 *            The width of the object
	 * @param height
	 *            The height of the object
	 * @return True if the dimensions intersect the viewport, false if they do
	 *         not.
	 */
	public boolean contains(float x, float y, float width, float height) {
		if (width == 0 || height == 0) {
			logger.error("GameViewPort width or height was 0 :: ", width, " ", height);
		}
		return dimensions.intersects(x, y, width, height);
	}

	/**
	 * Returns true if the viewport intersects with these required dimensions
	 * 
	 * @param location
	 *            The X,Y location of the point to check
	 * @param width
	 *            The width of the object
	 * @param height
	 *            The height of the object
	 * @return True if the dimensions intersect the viewport, false if they do
	 *         not.
	 */
	public boolean contains(Location location, float width, float height) {
		return contains((float) location.getX(), (float) location.getY(), width, height);
	}

	/**
	 * Returns true if the viewport intersects with these required dimensions
	 * 
	 * @param dimensions
	 *            The dimensions of the object
	 * 
	 * @return True if the viewport contains the dimensions, false if it it does
	 *         not contain these dimensions
	 */
	public boolean contains(Rectangle.Float dimensions) {
		if (dimensions.width == 0 || dimensions.height == 0) {
			logger.error("GameViewPort either width or height was 0");
		}
		return getDimensions().intersects(dimensions);
	}

	/**
	 * Sets the viewport's location, without affecting the width/height of this
	 * viewport
	 * 
	 * @param x
	 *            The center X location of this object
	 * @param y
	 *            The center Y location of this object
	 */
	public void setPosition(float x, float y) {
		setX(x);
		setY(y);
	}

	/**
	 * Gets the top left position of the viewport, considered the 'actual'
	 * position
	 * 
	 * @return
	 */
	public Location getTopLeftLocation() {
		return new Location((int) dimensions.x, (int) dimensions.y);
	}
	// ----------------------------------------------------------------
	// Getters/Setters
	// ----------------------------------------------------------------
	/**
	 * Sets the center X location of this object. This should be from the top
	 * left position.
	 */
	public void setCenterX(float x) {
		dimensions.x = x - getWidth() / 2;
	}

	/**
	 * Sets the center Y location of this object. This should be from the top
	 * left position.
	 */
	public void setCenterY(float y) {
		dimensions.y = y - getHeight() / 2;
	}

	/**
	 * The x position of the {@link GameViewPort} object, with 0,0 being top
	 * left
	 */
	public float getX() {
		return dimensions.x;
	}

	/**
	 * Sets the X location of this object. This should be from the top left
	 * position.
	 */
	public void setX(float x) {
		dimensions.x = x;
	}

	/**
	 * The Y position of the {@link GameViewPort} object, with 0,0 being top
	 * left
	 */
	public float getY() {
		return dimensions.y;
	}

	/**
	 * Set the Y position of the object, with 0,0 being the top left
	 */
	public void setY(float y) {
		dimensions.y = y;
	}

	/**
	 * The width of this {@link GameViewPort} object
	 */
	public float getWidth() {
		return dimensions.width;
	}

	/**
	 * Set the new width of this {@link GameViewPort}
	 */
	public void setWidth(float width) {
		dimensions.width = width;
	}

	/**
	 * get the height of this {@link GameViewPort}
	 */
	public float getHeight() {
		return dimensions.height;
	}

	/**
	 * Change the height of this {@link GameViewPort}
	 */
	public void setHeight(float height) {
		dimensions.height = height;
	}

	/**
	 * Update the location of this {@link GameViewPort}
	 * 
	 * @param x
	 *            The new X location, with 0,0 being the top left
	 * @param y
	 *            The new Y location, with 0,0 being the top left
	 */
	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	/**
	 * Update the location of this {@link GameViewPort}, with 0,0 being top left
	 * 
	 * @param location
	 *            The new location object
	 */
	public void setLocation(Location location) {
		setX(location.getX());
		setY(location.getY());
	}

	/**
	 * Set the dimensions of the {@link GameViewPort}
	 * 
	 * @param x
	 *            X location of this {@link GameViewPort}. This should be from
	 *            the top left position.
	 * @param y
	 *            Y location of this {@link GameViewPort}. This should be from
	 *            the top left position.
	 * @param width
	 *            The new width of the {@link GameViewPort}
	 * @param height
	 *            The new height of the {@link GameViewPort}
	 */
	@Override
	public void setDimensions(float x, float y, float width, float height) {
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}

	/**
	 * Return the dimensions of this {@link GameViewPort}
	 */
	@Override
	public Rectangle2D.Float getDimensions() {
		return dimensions;
	}

	@Override
	public String toString() {
		return Helpers.concat("x :: ", getX(), " y :: ", getY(), " width :: ", getWidth(), " height :: ", getHeight());
	}
}
