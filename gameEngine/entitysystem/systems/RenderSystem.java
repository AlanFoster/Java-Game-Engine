package entitysystem.systems;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import world.DrawOrder;
import engine.interfaces.IDrawable;
import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.GameViewPort;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystems.components.Draw;
import entitysystems.components.Spatial;

/**
 * This is the main rendering class for the entity system. This system will 
 * @author 40040345
 *
 */
public class RenderSystem extends EntitySystem implements IDrawableLogical {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	final static GameLogging logger = new GameLogging(RenderSystem.class);

	DrawOrder[] drawOrders;
	GameViewPort viewPort;
	// Visible sprites denoted as the Draw sprite and Spatial drawing
	// location
	Map<DrawOrder, List<RenderSystem.VisibleSprite>> visibleSprites;

	int totalVisibleEnities;

	public RenderSystem(EntityManager entityManager, DrawOrder[] drawOrders, GameViewPort viewPort) {
		super(entityManager);

		this.drawOrders = drawOrders;
		this.viewPort = viewPort;

		visibleSprites = new HashMap<DrawOrder, List<RenderSystem.VisibleSprite>>();

		// Iterate our draw layers and create maps for them
		for (DrawOrder drawOrder : this.drawOrders) {
			visibleSprites.put(drawOrder, new ArrayList<RenderSystem.VisibleSprite>());
		}
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContainingAny(Draw.class);
	}

	// This system is dedicated solely to drawing and requires no logic
	// updates. Perhaps. Maybe culling, is that logic?...
	@Override
	public void logicUpdate(GameTime time) {
	}

	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		totalVisibleEnities = 0;

		clearMaps();
		sortEntitiesToMap();

		// At this point we will have a list of only visible sprites, and
		// the map should contain the draw order required. Remember that not
		// every key will match up with the required draw order!
		for (DrawOrder drawOrder : drawOrders) {
			// Because of the lazy initialization of keys/values
			// visibleSprites.get(drawOrder) might actually be null
			if (visibleSprites.containsKey(drawOrder)) {
				for (RenderSystem.VisibleSprite visibleSprite : visibleSprites.get(drawOrder)) {
					Spatial spatial = visibleSprite.spatialComponent;
					BufferedImage drawImage = visibleSprite.drawComponent.getImage();

					// If the spatial's location has changed we must investigate
					// whether or not the rotation has also changed, if it is we
					// shall apply the required affine transformation to the
					// Draw component for re-caching
					if (spatial.spatialChanged) {
						if (spatial.previousSpatialWasNull || spatial.previousSpatial.getRotation() != spatial.getRotation()) {
							AffineTransform tx = AffineTransform.getRotateInstance(spatial.getRotation(), spatial.width / 2, spatial.height / 2);
							AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
							// Get the raw image and apply the transformation,
							// then set its new graphic to the transformed image
							visibleSprite.drawComponent.setImage(op.filter(visibleSprite.drawComponent.getRawImage(), null));
						}
					}

					// Draw the required sprite
					drawScreen.drawImage(drawImage,
							(int) (spatial.x + offsetX - viewPort.getX()),
							(int) (spatial.y + offsetY - viewPort.getY()),
							null);
				}
			}
		}
	}

	private void clearMaps() {
		// Iterate our draw layers and create maps for them
		for (List<VisibleSprite> visibleSpriteList : visibleSprites.values()) {
			visibleSpriteList.clear();
		}
	}

	@Override
	public void cleanUp() {
		super.cleanUp();

		clearMaps();
	}

	// Iterates over all entities and checks if its visible
	private void sortEntitiesToMap() {
		// Iterate the entities that need rendered and check if they're
		// visible
		for (Entity entity : entityList) {
			Spatial entitySpatial = entity.getComponent(Spatial.class);
			// I'll obviously offer an overloaded contains method for
			// spatials in the future (potentially)
			if (viewPort.contains(entitySpatial.x, entitySpatial.y, entitySpatial.width, entitySpatial.height)) {
				// Add it to be rendered
				addToRenderMap(entity, entitySpatial);
			}
		}
	}

	private void addToRenderMap(Entity entity, Spatial spatialComponent) {
		// If the sprite is visible, get its drawing order and add
		// it to our drawing list
		// visibleSprites.put(entityDraw.drawOrder, value)
		Draw drawComponent = entity.getComponent(Draw.class);
		// Get the draw order and get where it should be drawn within the
		// map
		visibleSprites.get(drawComponent.drawOrder).add(new VisibleSprite(drawComponent, spatialComponent));

		// Increase our total visible entity count, just for debugging purposes,
		// it's not really used anywhere other than a logger.info within the
		// drawMethod()
		totalVisibleEnities++;
	}

	/**
	 * This is just a basic structure to store information on objects that are
	 * currently visible. It allows us to store the drawComponent and
	 * spatialComponent, which saves us the hassle of getting it from the
	 * entityManager again. Direct field access is provided for speed. The class
	 * is private so others can't use this structure.
	 */
	private static class VisibleSprite {
		Draw drawComponent;
		Spatial spatialComponent;

		VisibleSprite(Draw drawComponent, Spatial spatialComponent) {
			this.drawComponent = drawComponent;
			this.spatialComponent = spatialComponent;
		}
	}
}