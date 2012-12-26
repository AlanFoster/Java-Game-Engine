package entitysystem.systems;

import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.Spatial;

public class PreviousSpatialSystem extends ProcessEntitySystem {
	public PreviousSpatialSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Spatial.class);
	}

	@Override
	public void processEntity(Entity entity) {
		Spatial spatial = entity.getComponent(Spatial.class);

		/**
		 * If the previous spatial was null, this is the first time it's been
		 * through this PreviousSpatialSystem. We therefore create its new
		 * Spatial. We couldn't do this within the Spatial class because it
		 * would cause recursion. IE, doing Spatial() { previousSpatial = new
		 * Spatial(); } would not be a good idea!
		 * <p>
		 * Our second test is, if it different to before we should update it,
		 * and change the boolean flags to let any other system that cares about
		 * the change. Possibly the rendering system and collision detection
		 * system etc.
		 * <p>
		 * Our last else is simply to say there was no change, and we reset the
		 * flags to false.
		 */
		if (spatial.previousSpatial == null) {
			spatial.previousSpatial = new Spatial(spatial.x, spatial.y, spatial.width, spatial.height, spatial.getRotation());

			spatial.spatialChanged = true;
			spatial.previousSpatialWasNull = true;
		} else if (!spatial.equals(spatial.previousSpatial)) {
			spatial.previousSpatial.x = spatial.x;
			spatial.previousSpatial.y = spatial.y;
			spatial.previousSpatial.width = spatial.width;
			spatial.previousSpatial.height = spatial.height;
			spatial.previousSpatial.setRotation(spatial.getRotation());

			spatial.spatialChanged = true;
			spatial.previousSpatialWasNull = false;
		} else {
			spatial.spatialChanged = false;
			spatial.previousSpatialWasNull = false;
		}
	}
}