package entitysystem.systems;

import engine.main.GameTime;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystems.components.TimedDelete;

/**
 * Manages any entities with the TimedDelete component added to them. On each
 * logical update the component's lifeDuration will be decremented by teh
 * elapsed game time. When this is less than 0 it will be deleted from the
 * entity manager and an explicit refresh to the entity lists will occur.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class TimedDeleteSystem extends EntitySystem {
	public TimedDeleteSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(TimedDelete.class);
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
		for (Entity entity : entityList) {
			processEntity(entity, gameTime.getElapsedTimeMilli());
		}
	}

	/**
	 * Process all entites that are part of our system
	 */
	public void processEntity(Entity entity, long elapsedTime) {
		TimedDelete deleteComponent = entity.getComponent(TimedDelete.class);
		deleteComponent.lifeDuration -= elapsedTime;
		if (deleteComponent.lifeDuration < 0) {
			entityManager.removeEntity(entity);
		}
	}
}