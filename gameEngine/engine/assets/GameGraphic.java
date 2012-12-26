package engine.assets;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import engine.interfaces.IDrawable;
import engine.interfaces.IPositionable;
import engine.misc.Helpers;
import engine.misc.Location;

/**
 * This class allows you to create a new instance of a graphic which will be
 * drawn directly to the screen It is generally best to have the image exist
 * within the GameAssetFactory before trying to set the image, but it can be set
 * directly within this class

 * @author Alan Foster
 * @version 1.0
 * 
 */
public class GameGraphic extends Asset<GameGraphic> implements IDrawable, IPositionable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * Stores the dimensions of the object, IE x,y,width, height
	 */
	private Rectangle2D.Float dimensions = new Rectangle2D.Float();

	/**
	 * The raw image that this GameGraphic represents. This will be drawn to the
	 * screen during the draw method when required
	 */
	protected BufferedImage image;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * This constructor instantiates a GameGraphic object which will store a
	 * BufferedImage and location for drawing to the screen when required.
	 * 
	 * @param name
	 *            An identifiable name that will be picked up by the game engine
	 *            and is used for debugging
	 * @param image
	 *            The image that will be stored by this gamegraphic, and when
	 *            this classes's draw method is called it will draw this graphic
	 *            object
	 * @param x
	 *            The X location of this graphic. This is where the graphic will
	 *            be drawn to when asked to draw
	 * @param y
	 *            The Y location of this graphic. This is where the graphic will
	 *            be drawn to when asked to draw. This starts from top left
	 */
	public GameGraphic(String name, BufferedImage image, float x, float y) {
		super(name);
		setDimensions(x, y, image.getWidth(), image.getHeight());
		setImage(image);
	}

	/**
	 * This constructor instantiates a GameGraphic object which will store a
	 * BufferedImage and location for drawing to the screen when required.
	 * <p>
	 * This method will assume an X and Y location of 0,0 (the top left)
	 * 
	 * @param name
	 *            An identifiable name that will be picked up by the game engine
	 *            and is used for debugging
	 * @param image
	 *            The image that will be stored by this gamegraphic, and when
	 *            this classes's draw method is called it will draw this graphic
	 *            object
	 */
	public GameGraphic(String name, BufferedImage image) {
		this(name, image, 0, 0);
	}

	/**
	 * A clone constructor which will allow for a shallow clone to be returned
	 * from an existing GameGraphic. This object will share the same image
	 * resources
	 * 
	 * @param oldGameGraphic
	 *            The already existing object that needs to be cloned from
	 */
	public GameGraphic(GameGraphic oldGameGraphic) {
		this(oldGameGraphic.getName(), oldGameGraphic.getImage(), oldGameGraphic.getX(), oldGameGraphic.getY());
	}

	// ----------------------------------------------------------------
	// Drawing
	// ----------------------------------------------------------------
	/**
	 * Draws This GameGraphic to the game screen, taking into consideration the
	 * offsetX and offsetY, and the current GameGraphic's x,y location. (Top
	 * left being 0,0)
	 * 
	 * @param drawScreen
	 *            Direct access to the graphics2d object where all drawing
	 *            should appear
	 * @param offsetX
	 *            The x offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 * @param offsetY
	 *            The y offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		drawScreen.drawImage(image, offsetX + (int) getX(), offsetY + (int) getY(), null);
	}

	// ----------------------------------------------------------------
	// Getters/Setters
	// ----------------------------------------------------------------
	/**
	 * Return the current bufferedImage
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Set this GameGraphics BufferedImage
	 * 
	 * @param image
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}

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
	public Rectangle2D.Float getDimensions() {
		return dimensions;
	}

	/**
	 * Return a shallow clone of this object. This object will share the same
	 * image resources
	 */
	@Override
	public GameGraphic getShallowClone() {
		// Call our clone constructor, and return it
		return new GameGraphic(this);
	}

	@Override
	public String toString() {
		return Helpers.concat("\nName :: ", getName(), " x :: ", getX(), " y :: ", getY(), " width :: ", getWidth(), " Height :: ", getHeight());
	}

}
