package entitysystem.systems;

import engine.misc.GameViewPort;
import engine.misc.Helpers;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.FollowMouse;
import entitysystems.components.Spatial;
import eventhandling.MouseEventHandler;

/**
 * All entities that wish to rotate towards the position of the mouse should
 * have the components FollowMouse and Spatial to do so.
 * <p>
 * This class takes into consideration the viewport offset whilst calculating
 * the atan2 required
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see FollowMouse
 */
public class FollowMouseSystem extends ProcessEntitySystem {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * Access the mouse event handler. We poll this to get the mouse's current
	 * location on each logic update when processing entities, so that we can
	 * rotate towards the mouse location using atan2
	 * */
	private MouseEventHandler mouseEventHandler;

	/**
	 * We need to make use of the viewport so that we can calculate the actual
	 * distance between an object and the mouse position, because remember the
	 * viewport makes things draw relatively
	 */
	private GameViewPort viewPort;

	/**
	 * 
	 * @param entityManager
	 *            We require access to the entity manager so that we can acquire
	 *            any entities from the entity manager when required. We use
	 *            this within the refreshList to get keep track of all entities
	 *            with specific components that we will look after and perform
	 *            logic on.
	 * @param mouseEventHandler
	 *            Access the mouse event handler so that we can update the
	 *            rotation of entities to follow the mouse's postiion
	 * @param viewPort
	 *            Werequire the viewport so that we can calculate the actual
	 *            distance between an object and the mouse position, because
	 *            remember the viewport makes things draw relatively
	 */
	public FollowMouseSystem(EntityManager entityManager, MouseEventHandler mouseEventHandler, GameViewPort viewPort) {
		super(entityManager);
		this.mouseEventHandler = mouseEventHandler;
		this.viewPort = viewPort;
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(FollowMouse.class, Spatial.class);
	}

	/**
	 * Process each entity and set their rotation to the atan2 of the mouse to
	 * entity
	 */
	@Override
	public void processEntity(Entity entity) {
		Spatial spatial = entity.getComponent(Spatial.class);

		// Calculate the rectangle coordinates between the mouse's 'actual'
		// position
		// within the world, taking into consideration the viewport, and to
		// the center of the component
		double coordX = (viewPort.getX() + mouseEventHandler.mousePosX) - (spatial.x + spatial.width / 2);
		double coordY = (viewPort.getY() + mouseEventHandler.mousePosY) - (spatial.y + spatial.height / 2);

		spatial.setRotation(Math.atan2(coordY, coordX));
	}

}