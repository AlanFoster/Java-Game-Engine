package entitysystem.systems;

import java.util.ArrayList;
import java.util.List;

import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.PlayerControlled;
import entitysystems.components.Shoot;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;
import eventhandling.KeyEventHandler;
import eventhandling.MouseEventHandler;

/**
 * Add this component to an entity that is controlled by a player. This class
 * processes each entity which has the {@link PlayerControlled} component added
 * to them, and a velocity.
 * <p>
 * The Key values that are used to control this entity are set within the
 * {@link PlayerControlled} entity itself, and are not enforced within this
 * class.
 * <p>
 * In a sense this class is generic, as it just tells whatever entity that its
 * given to 'accelerate' if the key required is pressed etc. But I do not like
 * this implementation, it is not generic enough. Perhaps there should be an
 * interface to interact with components and perform logic... But this goes
 * against the 'purist' component idea that this entity system makes use of.
 * This is potentially something that could be worked on.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class PlayerControlledSystem extends ProcessEntitySystem {
	MouseEventHandler mouseEventHandler;
	KeyEventHandler keyEventHandler;

	/**
	 * 
	 * @param entityManager
	 *            We require access to the entity manager so that we can acquire
	 *            any entities from the entity manager when required. We use
	 *            this within the refreshList to get keep track of all entities
	 *            with specific components that we will look after and perform
	 *            logic on.
	 * @param mouseEventHandler
	 *            Access to the mouse event handler so that we can perform any
	 *            logic clicking. We poll this system for this information.
	 * @param keyEventHandler
	 *            Access to the key event handler which we poll for information
	 *            about whether or not the required key has been pressed down
	 *            (the key value required within the {@link PlayerControlled}
	 *            component)
	 */
	public PlayerControlledSystem(EntityManager entityManager, MouseEventHandler mouseEventHandler, KeyEventHandler keyEventHandler) {
		super(entityManager);
		this.mouseEventHandler = mouseEventHandler;
		this.keyEventHandler = keyEventHandler;
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(PlayerControlled.class, Velocity.class);
	}

	@Override
	public void processEntity(Entity entity) {
		Spatial spatial = entity.getComponent(Spatial.class);
		Velocity velocity = entity.getComponent(Velocity.class);
		PlayerControlled playerControlled = entity.getComponent(PlayerControlled.class);

		// Poll the event handelr and check if the key required for moving up
		// has been pressed
		boolean moveUp = keyEventHandler.isKeyDown(playerControlled.listenUp);
		boolean moveDown = keyEventHandler.isKeyDown(playerControlled.listenDown);
		boolean moveLeft = keyEventHandler.isKeyDown(playerControlled.listenLeft);
		boolean moveRight = keyEventHandler.isKeyDown(playerControlled.listenRight);

		if (moveUp) {
			velocity.currentState = Velocity.VelocityState.Accelerate;
		} else if (moveDown) {
			velocity.currentState = Velocity.VelocityState.Reverse;
		} else {
			velocity.currentState = Velocity.VelocityState.Idle;
		}

		if (moveLeft) {
			spatial.incrementDegrees(-playerControlled.turningSpeed);
		}

		if (moveRight) {
			spatial.incrementDegrees(playerControlled.turningSpeed);
		}

		// If the mouse is down, iterate through all children and check if they
		// wish to fire a bullet.
		if (mouseEventHandler.leftClickDown) {
			// As this entity may be a parent entity, grab every single Shoot
			// component that either the parent or its child may have, and allow
			// it to shoot.
			List<Shoot> shootComponentList = new ArrayList<Shoot>();
			ParentSystem.getAllChildrenTComponents(entity, Shoot.class, shootComponentList);

			for (Shoot shootComponent : shootComponentList) {
				shootComponent.shootRequired = true;
			}
		}
	}
}