package engine.components;

import java.awt.Graphics2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import engine.assets.GameGraphic;
import engine.interfaces.IDrawableLogical;
import engine.main.GameEngine;
import engine.main.GameObjectBase;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.Helpers;

/**
 * A GameGraphicComponent encapsulates a basic GameGraphic object within the
 * class and provides the new Methods inherited from GAmeSizeableComponent and
 * most importantly as it implements {@link IDrawableLogical} it can perform any
 * logic required. Presumably this will be the most used class in a 'normal'
 * inheritance based game. IE class Player extends GameGraphicComponent. But
 * note that such an OOP based system is deprecated as this GameEngine offers an
 * entity based system.
 * <p>
 * All drawing should be handled by the GameGraphic, thus the draw method will
 * simply pass the Graphics2D object onto the associated graphic.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public abstract class GameGraphicComponent extends GameSizeableComponent implements IDrawableLogical {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameSizeableComponent.class);

	/**
	 * The graphic associated with this Object. For instance an image of the
	 * player
	 */
	protected GameGraphic associatedGraphic;

	/**
	 * Creates a blank GameGraphicComponent with an asset name. At this point
	 * the associatedGraphic will be null and can be set explicitly with
	 * setCurrentGraphic()
	 * 
	 * @param name
	 *            The identifying name for this GameGraphicComponent
	 */
	public GameGraphicComponent(String name) {
		super(name);
	}

	/**
	 * 
	 * @param name
	 *            The identifying name for this GameGraphicComponent
	 * @param graphic
	 *            The gamegraphic that will be used to visually represent this
	 *            GameGraphicComponent.
	 * @param posX
	 *            The x position that will be used for this object, this will be
	 *            taken from the top left
	 * @param posY
	 *            The y position that will be used for this object, this will be
	 *            taken from the top left
	 * @param width
	 *            The width of this GameGraphicComponent, this can be different
	 *            to the visual representation of the GameGraphic under some
	 *            circumstances if required.
	 * @param height
	 *            The height of this GameGraphicComponent, this can be different
	 *            to the visual representation of the GameGraphic under some
	 *            circumstances if required.
	 */
	public GameGraphicComponent(String name, GameGraphic graphic,
			float posX, float posY,
			float width, float height) {
		this(name);
		setX(posX);
		setY(posY);
		setWidth(width);
		setHeight(height);
		setCurrentGraphic(graphic);
	}

	/**
	 * This constructor will set the width and height to the width and height of
	 * the BufferedImage passed into it, if you wish to explicitly set this you
	 * can call setWidth/setHeight directly
	 * 
	 * @param name
	 *            The identifying name for this GameGraphicComponent
	 * @param image
	 *            The bufferedImage that will be given to the representing
	 *            GameGraphic object. This will be created in a new GameGraphic
	 * @param posX
	 *            The x position that will be used for this object, this will be
	 *            taken from the top left
	 * @param posY
	 *            The y position that will be used for this object, this will be
	 *            taken from the top left
	 */
	public GameGraphicComponent(String name, BufferedImage image, float posX, float posY) {
		this(name);
		associatedGraphic = new GameGraphic(name, image);
		setX(posX);
		setY(posY);
		setWidth(associatedGraphic.getWidth());
		setHeight(associatedGraphic.getHeight());
	}

	/**
	 * * This constructor will set the width and height to the width and height
	 * of the graphic passed into it, if you wish to explicitly set this there
	 * is another constructor for that.
	 * 
	 * @param name
	 *            The identifying name for this GameGraphicComponent
	 * @param graphic
	 *            The gamegraphic that will be used to visually represent this
	 *            GameGraphicComponent.
	 * @param posX
	 *            The x position that will be used for this object, this will be
	 *            taken from the top left
	 * @param posY
	 *            The y position that will be used for this object, this will be
	 *            taken from the top left
	 */
	public GameGraphicComponent(String name, GameGraphic graphic, float posX, float posY) {
		this(name, graphic, posX, posY, graphic.getWidth(), graphic.getHeight());
	}

	/**
	 * 
	 * @return Returns the current associated GameGraphic which is visually
	 *         representing this object
	 */
	public GameGraphic getCurrentGraphic() {
		return associatedGraphic;
	}

	/**
	 * Changes the game graphic component to that of the object passed into it.
	 * After this has been set this component will be visually different.
	 * 
	 * @param gameGraphic
	 *            The new GameGraphic object which is representing this
	 *            component
	 */
	public void setCurrentGraphic(GameGraphic gameGraphic) {
		this.associatedGraphic = gameGraphic;
	}

	/**
	 * Modifies the existing associated graphic and sets the buffered image to
	 * that of the object passed in.
	 * 
	 * @param gameGraphic
	 *            The new BufferedImage that which will represent this component
	 */
	public void setCurrentGraphic(BufferedImage gameImage) {
		associatedGraphic.setImage(gameImage);
	}

	/**
	 * Returns a string representation of the object. In general, the toString
	 * method returns a string that "textually represents" this object. The
	 * result should be a concise but informative representation that is easy
	 * for a person to read. It is recommended that all subclasses override this
	 * method.
	 */
	@Override
	public String toString() {
		return Helpers.concat("\t- Name : \"", getName(), "\" dimensions : ", getDimensions());
	}

	/**
	 *
	 * @see engine.interfaces.IDrawable#draw(java.awt.Graphics2D, int, int)
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		getCurrentGraphic().draw(drawScreen, (int) (getX() + offsetX - (getCurrentGraphic().getWidth() / 2)), (int) (getY() + offsetY - (getCurrentGraphic().getHeight() / 2)));
	}
}
