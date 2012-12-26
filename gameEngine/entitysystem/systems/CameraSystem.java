package entitysystem.systems;

import engine.main.GameTime;
import engine.misc.GameViewPort;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.Camera;
import entitysystems.components.Spatial;

/**
 * The camera system has direct access to the viewport, and will update it to
 * whatever entity that this camera is attached to. The camera will focus on the
 * center of the object. This class is nice, as if there is no entity found with
 * the camera on it, it simply won't do anything. IE it won't complain that
 * there was no camera found and spit up null pointers or anything.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class CameraSystem extends ProcessEntitySystem {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * Direct access to the viewport so that we can directly move it to be
	 * centered on an entity's center
	 */
	private GameViewPort viewport;

	/**
	 * The boundaries of the camera. When the entity's middle x,y location is
	 * within the bounds of this boundary, the camera will move. If it isn't
	 * within these points, it will no longer follow the entity
	 */
	private Spatial boundaries;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * 
	 * @param entityManager
	 *            Access to the entity manager to get the newest entity that has
	 *            the camera component
	 * @param gameViewPort
	 *            This class requires direct access to the viewport to update
	 *            its location on the center of an entity
	 * @param boundaries
	 *            The areas where the camera will still follow the entity. If
	 *            the entity is not within these bounds, it will not move the
	 *            viewport.
	 */
	public CameraSystem(EntityManager entityManager, GameViewPort gameViewPort, Spatial boundaries) {
		super(entityManager);
		this.viewport = gameViewPort;
		this.boundaries = boundaries;
	}

	/**
	 * Within this method you should initialize any variables required, such as
	 * setting the required components needed from an entity to perform whatever
	 * action the system performs. This will get called once before it receives
	 * its first logic update.
	 * <p>
	 * Within this class we reset the viewport location to be 0,0, so that any
	 * logic placement of camera location can be handled correctly.
	 */
	@Override
	public void startUp(GameTime gameTime) {
		viewport.setLocation(0, 0);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Camera.class);
	}

	/**
	 * Process the entity that has the Camera component added to them
	 */
	@Override
	public void processEntity(Entity entity) {
		Spatial spatialComponent = entity.getComponent(Spatial.class, true);
		/**
		 * It is assumed, as everywhere (unless explicitly stated) that top left
		 * is 0,0. We check if either the center x location of the entity is
		 * within the camera's boundaries, and if it is we move the viewport.
		 * This is the same for the y value
		 **/
		if (spatialComponent.x + spatialComponent.width / 2 > boundaries.x &&
				spatialComponent.x + spatialComponent.width / 2 < boundaries.x + boundaries.width)
			viewport.setCenterX(spatialComponent.x + spatialComponent.width / 2);
		if (spatialComponent.y + spatialComponent.height / 2 > boundaries.y
				&& spatialComponent.y + spatialComponent.height / 2 < boundaries.y + boundaries.height)
			viewport.setCenterY(spatialComponent.y + spatialComponent.height / 2);
	}

	/**
	 * The boundaries assume top left to be 0,0
	 * 
	 * @param x
	 *            The x location of the boundary
	 * @param y
	 *            The Y location of the boundary
	 * @param width
	 *            The width of the boundary
	 * @param height
	 *            the height of the boundary
	 */
	public void updateBoundaries(float x, float y, float width, float height) {
		boundaries.x = x;
		boundaries.y = y;
		boundaries.width = width;
		boundaries.height = height;
	}
}