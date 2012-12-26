package entitysystem.systems;

import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;
import entitysystems.components.Velocity.VelocityState;

/**
 * Maintains and moves all entities which have both the {@link Velocity}
 * component and {@link Spatial} component added to them.
 * <p>
 * This class decides how to manipulate the entities spatial based on the
 * velocity components currentState. For more details on this see
 * {@link VelocityState}
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class VelocitySystem extends ProcessEntitySystem {
	public VelocitySystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Spatial.class, Velocity.class);
	}

	@Override
	public void processEntity(Entity entity) {
		Velocity velocity = entity.getComponent(Velocity.class);
		Spatial spatial = entity.getComponent(Spatial.class);

		switch (velocity.currentState) {
			case Accelerate:
				velocity.currentVelocity = Math.min(velocity.velocityCap, velocity.currentVelocity + velocity.acceleration);
				break;
			case Reverse:
				velocity.currentVelocity = Math.max(-velocity.velocityCap, velocity.currentVelocity - velocity.acceleration);
				break;
		}

		// Decide if the body has a velocity, if it does, start to calculate
		// the friction applied to it, and the spatials new x,y coordinates as
		// required
		if (velocity.currentVelocity != 0) {
			if (Math.abs(velocity.currentVelocity) < 0.001f) {
				velocity.currentVelocity = 0;
			} else {
				spatial.x += velocity.currentVelocity * Math.cos(spatial.getRotation());
				spatial.y += velocity.currentVelocity * Math.sin(spatial.getRotation());
			}

			velocity.currentVelocity *= velocity.friction;
		}
	}
}