package entitysystem.systems;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import engine.interfaces.IDrawable;
import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.GameViewPort;
import engine.misc.Location;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystems.components.Collide;
import entitysystems.components.Spatial;

/**
 * This collision detection system currently doesn't stop tunneling sadly. This
 * may or may not be a problem, it hugely depends on numerous factors. I can
 * code this if it does prove a requirement.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class CollisionSystem extends EntitySystem implements IDrawableLogical {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * When this is true we will draw the buckets that each entity has been
	 * placed into, when false we will not draw debg information
	 */
	final static boolean debug = false;

	/**
	 * Stores a hashmap of all object locations that an entity resides within.
	 * An entity can exist within more than one location.
	 */
	private HashMap<Location, List<Entity>> objectLocations;

	/**
	 * Used for spatial hashing. This should be a size in which the maximum
	 * amount of 'buckets' that a entity fits into is four. For further details
	 * on the specifics of this see
	 * http://www.cs.qub.ac.uk/~P.Hanna/CSC3049/Resources.htm section 2.7
	 */
	private int bucketSize;

	/**
	 * Access to the viewport. We need this when drawing the debug lines, so
	 * that they are correctly in line with the viewport's offset
	 */
	private GameViewPort viewport;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * 
	 * @param viewport
	 *            Access to the viewport. We need this when drawing the debug
	 *            lines, so that they are correctly in line with the viewport's
	 *            offset
	 * @param entityManager
	 *            Access to the entity manager
	 * @param bucketSize
	 *            Used for spatial hashing. This should be a size in which the
	 *            maximum amount of 'buckets' that a entity fits into is four.
	 *            For further details on the specifics of this see
	 *            http://www.cs.qub.ac.uk/~P.Hanna/CSC3049/Resources.htm section
	 *            2.7
	 */
	public CollisionSystem(GameViewPort viewport, EntityManager entityManager, int bucketSize) {
		super(entityManager);
		objectLocations = new HashMap<Location, List<Entity>>();

		this.viewport = viewport;
		this.bucketSize = bucketSize;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Collide.class);
	}

	/**
	 * My current collision detection is as follows! Sort each entity into
	 * spatial buckets, and then test for collisions If there is a possibility
	 * of a collision based on the entity's spatial, then we devise an even
	 * further investigation into whether or not these entities have collided.
	 */
	@Override
	public void logicUpdate(GameTime time) {
		// Clear our bucket list, it's not exactly an efficient choice, but,
		// 'it'll do for now!'
		objectLocations.clear();

		// Add each entity to buckets, and clear their previously collided
		// with entities list
		for (Entity entity : entityList) {
			entity.getComponent(Collide.class).clearDetails();
			addToBucket(entity);
		}

		// Perform the collision tests
		for (List<Entity> bucket : objectLocations.values()) {
			// There is no point checking buckets of size 1
			if (bucket.size() > 1) {
				for (Entity entityA : bucket) {
					Collide collideDetailsA = entityA.getComponent(Collide.class);
					Spatial spatialDetailsA = entityA.getComponent(Spatial.class);
					for (Entity entityB : bucket) {
						if (entityA != entityB) {
							Collide collideDetailsB = entityA.getComponent(Collide.class);
							Spatial spatialDetailsB = entityB.getComponent(Spatial.class);

							// Let both objects know about the fact that
							// they collided with eachother
							if (spatialDetailsA.intersects(spatialDetailsB)) {
								collideDetailsA.addCollidedWith(entityB);
								collideDetailsB.addCollidedWith(entityA);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Logically places an entity into the buckets that it overlaps.
	 * 
	 * @param entity
	 *            The entity which will be placed into the buckets
	 */
	public void addToBucket(Entity entity) {
		Spatial spatial = entity.getComponent(Spatial.class);
		// Get all of the buckets that this spatial spans
		for (Location bucketLocation : getBucketLocations(spatial)) {
			// If it doesn't exist within the object locations array, create
			// it, so we don't get null point exception when attempting to
			// add
			if (!objectLocations.containsKey(bucketLocation)) {
				objectLocations.put(bucketLocation, new ArrayList<Entity>());
			}
			// Add the this entity to the bucket that it exists in
			objectLocations.get(bucketLocation).add(entity);

			// Store which bucket the entity was in back int othe entity
			// component, good for debugging, and it allows for other
			// systems to use this information potentially (like visual
			// culling perhaps)
			entity.getComponent(Collide.class).addBucketLocation(
					bucketLocation);
		}
	}

	//
	/**
	 * Returns a list of the buckets that this spatial fits into
	 * 
	 * @param spatial
	 *            The spatial (which stores the x,y,width and height) of the
	 *            entity that we are testing against
	 * @return A list of every tiled location that this object overlapped with
	 */
	public List<Location> getBucketLocations(Spatial spatial) {
		ArrayList<Location> bucketLocations = new ArrayList<Location>();

		int tempX = (int) spatial.x;
		while ((tempX / bucketSize) <= (spatial.x + spatial.width)
				/ bucketSize) {
			int bucketLocationX = tempX / bucketSize;
			int tempY = (int) spatial.y;
			while (tempY / bucketSize <= (spatial.y + spatial.height)
					/ bucketSize) {
				int bucketLocationY = tempY / bucketSize;
				bucketLocations.add(new Location(bucketLocationX,
						bucketLocationY));

				tempY += bucketSize;
			}
			tempX += bucketSize;
		}

		return bucketLocations;
	}

	/**
	 * Debug the collision system by drawing the collision buckets that we
	 * placed each entity inside. We will draw these bucket's borders with a
	 * colour of red if there was a possible collision (ie two entities in the
	 * same bucket), and a colour of blue if there was no possible collision.
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		if (debug) {
			for (Entity entity : entityList) {
				// Get the areas that this entity is going to collide with
				Collide collideDetails = entity.getComponent(Collide.class);

				drawScreen.setColor(Color.white);
				// Draw the bucket they are in
				for (Location bucketLocation : collideDetails.bucketLocations) {
					drawScreen
							.drawRect((int) (offsetX + (bucketLocation.getX() * bucketSize) - viewport.getX()),
									(int) (offsetY + (bucketLocation.getY() * bucketSize) - viewport.getY()),
									bucketSize, bucketSize);
				}

				// Draw a red line if there was a collision, draw a blue
				// line if there wasn't a collision
				Color lineColor = collideDetails.collisionList.size() > 0 ? Color.red : Color.blue;

				Spatial s = entity.getComponent(Spatial.class);

				drawScreen.setColor(lineColor);
				drawScreen.drawRect(
						(int) (s.x + offsetX - viewport.getX()), (int) (s.y
								+ offsetY - viewport.getY()),
						(int) s.width, (int) s.height);

			}
		}
	}
}