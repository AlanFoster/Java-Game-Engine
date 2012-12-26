package entitysystem.systems;

import java.util.List;

import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.Children;
import entitysystems.components.DeleteComponent;

/**
 * Deletes all entities which have the {@link DeleteComponent} added to them.
 * This just offers a means of logically removing everything that requires it at
 * a specific logical interval. For instance one system may wish to delete an
 * entity from the world, but it shouldn'd do immediately as other systems may
 * also wish to handle some logic to do with the entity too. This allows us to
 * 'delay' an entity's deletion to the next time the {@link DeleteSystem} gets
 * its logical update.
 * <p>
 * It is suggested that the {@link DeleteSystem} either runs last, or first, in
 * order to keep with the logical idea of a deleted entity still being available
 * throughout all systems until the next game loop.
 * <p>
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class DeleteSystem extends ProcessEntitySystem {
	public DeleteSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(DeleteComponent.class);
	}

	@Override
	public void processEntity(Entity entity) {
		// Ensure that we also remove any children entites owned by the parent
		Children childrenComponent = entity.getComponent(Children.class);
		if (childrenComponent != null) {
			for (Entity child : childrenComponent.children) {
				entityManager.removeEntity(child);
			}
		}
		entityManager.removeEntity(entity);
	}
}