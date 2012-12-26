package engine.components;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import engine.interfaces.ILogical;
import engine.interfaces.INameable;
import engine.interfaces.IPositionable;
import engine.main.GameEngine;
import engine.main.GameLayer;
import engine.main.GameObjectBase;
import engine.misc.GameLogging;
import engine.misc.Location;

/**
 * This class exists to produce 'sizeable' objects which will contain a location
 * and width/height. This class therefore implements {@link IPositionable} so
 * that it can offer these methods to any system within the game engine that
 * makes use the {@link IPositionable} interface.
 * <p>
 * A {@link GameSizeableComponent} component extends the GameObjectBase, which
 * currently implements {@link INameable}, which is a game engine requirement.
 * <p>
 * This object will receive logical updates within each game loop.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public abstract class GameSizeableComponent extends GameObjectBase implements IPositionable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The dimensions of this object. This object is used to store the x,y,
	 * width and height of this {@link GameSizeableComponent}
	 */
	protected Rectangle2D.Float dimensions = new Rectangle2D.Float();

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	/**
	 * Creates a new {@link GameSizeableComponent} with the required name
	 * 
	 * @param name
	 *            The name of this {@link GameSizeableComponent}
	 */
	public GameSizeableComponent(String name) {
		super(name);
	}

	/**
	 * Creates a new {@link GameSizeableComponent} component with the required
	 * name and X,Y location
	 * 
	 * @param name
	 *            The name of this {@link GameSizeableComponent}
	 * @param posX
	 *            The X location of this object, with 0,0 being top left
	 * @param posY
	 *            The Y location of this object, with 0,0 being top left
	 */
	public GameSizeableComponent(String name, float posX, float posY) {
		this(name);
		setX(posX);
		setY(posY);
	}

	/**
	 * Creates a new {@link GameSizeableComponent} component with the required
	 * name and X,Y, width and height
	 * 
	 * @param name
	 *            The name of this {@link GameSizeableComponent}
	 * @param posX
	 *            The X location of this object, with 0,0 being top left
	 * @param posY
	 *            The Y location of this object, with 0,0 being top left
	 * @param width
	 *            The height of this object
	 * @param height
	 *            The width of this object
	 */
	public GameSizeableComponent(String name, float posX, float posY, float width, float height) {
		this(name, posX, posY);
		setWidth(width);
		setHeight(height);
	}
	
	// ----------------------------------------------------------------
	// IPositionable implementations
	// ----------------------------------------------------------------
	/**
	 * The x position of the positionable object, with 0,0 being top left
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
	 * The Y position of the positionable object, with 0,0 being top left
	 */
	public float getY() {
		return dimensions.y;
	}

	/**
	 * Set the Y postiion of the object, with 0,0 beign the top left
	 */
	public void setY(float y) {
		dimensions.y = y;
	}

	/**
	 * The width of this positionable object
	 */
	public float getWidth() {
		return dimensions.width;
	}

	/**
	 * Set the new width of this object
	 */
	public void setWidth(float width) {
		dimensions.width = width;
	}

	/**
	 * get the height of this object
	 */
	public float getHeight() {
		return dimensions.height;
	}

	/**
	 * Change the height of this object
	 */
	public void setHeight(float height) {
		dimensions.height = height;
	}

	/**
	 * Update the location of this game graphic
	 * 
	 * @param x
	 *            The new X location, with 0,0 being the top left
	 * @param y
	 *            The new Y location, with 0,0 beign the top left
	 */
	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	/**
	 * Update the location of this game graphic, with 0,0 being top left
	 * 
	 * @param location
	 *            The new location object
	 */
	public void setLocation(Location location) {
		setX(location.getX());
		setY(location.getY());
	}

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
	@Override
	public void setDimensions(float x, float y, float width, float height) {
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}

	/**
	 * Return the dimensions of this object
	 */
	@Override
	public Float getDimensions() {
		return dimensions;
	}
}
