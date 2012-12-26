package entitysystems.components;

import entitysystem.core.IComponent;

/**
 * Attach this component to an entity when it wants to be removed from the
 * entity system. Remember, this can be done directly without the need for
 * the DeleteComponent and DeleteSystem, by doing
 * EntityManager.getInstance().deleteEntity(entity); This just offers a
 * means of logically removing everything that requires it at a specific
 * logical interval.
 */
public class DeleteComponent implements IComponent {
}