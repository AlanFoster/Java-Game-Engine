package entitysystems.components;

import java.awt.image.BufferedImage;

import entitysystem.core.IComponent;
import entitysystem.systems.RenderSystem;

import world.DrawOrder;

/**
 * When an entity has this component, it will handled by the RenderSystem. For
 * this component to 'work', it will also require a spatial component for the
 * render system to know where the sprite will be drawn. This component
 * internally stores two versions of the sprite given to it originally. A
 * reference to the 'raw' image, and an a 'modifiedImage'. The raw image is what
 * was given to it originally, and the modified image is one that has been given
 * a transformation to it, for instance rotation or colour effects. It is useful
 * to store these two separate bufferedimages so that we can manipulate the
 * images and apply effects easily, with getting the offer head of trying to get
 * another image from the GameAssetManager.
 * 
 * @see RenderSystem
 */
public class Draw implements IComponent {
	/**
	 * This stores a reference to the original buffered image that was given to
	 * it through the constructor. This will be the 'base'/'raw' image which
	 * will be used when making any image modifications such as affine changes,
	 * within setImage()
	 */
	private BufferedImage rawImage;

	/**
	 * This is the image that will be given out to external systems when they
	 * request the graphics of this component, unless they make an explicit call
	 * to the 'getRawImage()' method instead of 'getImage()'
	 */
	private BufferedImage modifiedImage;

	public DrawOrder drawOrder;

	// the entity it's binded to
	// Entity entity;

	public Draw(BufferedImage gameGraphic, DrawOrder drawOrder) {
		// Set the raw image and the modified image
		this.rawImage = modifiedImage = gameGraphic;
		this.drawOrder = drawOrder;
	}

	public BufferedImage getImage() {
		return modifiedImage;
	}

	public void setImage(BufferedImage image) {
		this.modifiedImage = image;
	}

	public BufferedImage getRawImage() {
		return rawImage;
	}

	public void setRawImage(BufferedImage bufferedImage) {
		this.rawImage = bufferedImage;
	}

	@Override
	public String toString() {
		return "[draw component]";
	}
}