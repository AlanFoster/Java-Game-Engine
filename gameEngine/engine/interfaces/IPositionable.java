package engine.interfaces;

import java.awt.geom.Rectangle2D;

/**
 * This interface allows access to get and set the x,y, width and height.
 * Perhaps the meaning of 'IPositionable' is a bit of a loose name for this
 * class, as it allows access to the width and height.
 * <p>
 * This interface also requires the methods for setting x,y,width,height which
 * is also 'wrong', but I feel it potentially allowed more freedom, and I'm
 * sure there will be a situation in which an object which receives
 * {@link IPositionable} objects will require the need to set the values it
 * requires too. Although currently such a thing does not occur within the game
 * engine.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public interface IPositionable {
	// ----------------------------------------------------------------
	// Setters
	// ----------------------------------------------------------------
	/**
	 * Sets the X location of this object. This should be from the top left
	 * position.
	 * 
	 * @param x
	 *            the new x position
	 */
	void setX(float x);
	/**
	 * Sets the Y location of this object. This should be from the top left
	 * position.
	 * 
	 * @param y
	 *            the new Y position
	 */
	void setY(float y);
	/**
	 * Sets the width of this object
	 * 
	 * @param width
	 *            The new width of this object
	 */
	void setWidth(float width);
	/**
	 * Set the heigh of the object
	 * 
	 * @param height
	 *            The new height of the object
	 */
	void setHeight(float height);
	/**
	 * Set the dimensions of the object
	 * 
	 * @param x
	 *            X location of this object. This should be from the top left
	 *            position.
	 * @param y
	 *            Y location of this object. This should be from the top left
	 *            position.
	 * @param width
	 *            The new width of the object
	 * @param height
	 *            The new height of the object
	 */
	void setDimensions(float x, float y, float width, float height);

	// ----------------------------------------------------------------
	// Getters
	// ----------------------------------------------------------------
	/**
	 * @return The x position of the positionable object, this will of course be
	 *         from the left of the screen unless otherwise stated
	 */
	float getX();
	/**
	 * @return The y position of the positionable object, this will of course be
	 *         from the top of the screen unless otherwise stated
	 */
	float getY();
	/**
	 * @return The width of this positionable object
	 */
	float getWidth();
	/**
	 * @return The width of this positionable object
	 */
	float getHeight();
	/**
	 * @return The dimensions of the positional object within a
	 *         Rectangle2D.Float object, in which will contain the
	 *         x,y,width,height of the object
	 */
	Rectangle2D.Float getDimensions();
}
