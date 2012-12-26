package entitysystem.systems;

import java.util.ArrayList;
import java.util.List;

import src.TankSurvival;

import engine.misc.GameLogging;
import engine.misc.Helpers;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.AI;
import entitysystems.components.NameTag;
import entitysystems.components.Shoot;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;
import entitysystems.components.AI.AIState;

/**
 * Manages all entities which have the AI component added to them.
 * <p>
 * Note :: This system was not completed to its fullest sadly. Comments took
 * priority. I was able to make some last minute improvements however. But still
 * not enough for it to be considered completed.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class AISystem extends ProcessEntitySystem {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(TankSurvival.class);

	/**
	 * Stores all entities with a tag name that we are searching for
	 */
	protected List<Entity> taggedEntityList;

	/**
	 * Stores a list of entities with a specific tag name that we've searched
	 * for through the method getEntitiesWithTag
	 */
	protected List<Entity> entitysWithTagNames;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	public AISystem(EntityManager entityManager) {
		super(entityManager);
		taggedEntityList = new ArrayList<Entity>();
		entitysWithTagNames = new ArrayList<Entity>();
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(AI.class);
		taggedEntityList = entityManager.getEntitiesContaining(NameTag.class);
	}

	/**
	 * Searches all entities for a specific name tag. IE, the 'player' name tag.
	 * This will be used within the ai system to decide whether or not specific
	 * entities are close and we need to chase them for instance.
	 * 
	 * @param name
	 *            The tag name that we are searching for
	 * @return All entities which have the tag name we are searching for.
	 *         Technically this shouldn't return a list, as we are directly
	 *         editing the list, but I prefer this for simplicity.
	 */
	protected List<Entity> getEntitiesWithTag(String name) {
		entitysWithTagNames.clear();
		for (Entity taggedEntity : taggedEntityList) {
			if (name.equals(taggedEntity.getComponent(NameTag.class).name)) {
				entitysWithTagNames.add(taggedEntity);
			}
		}
		return entitysWithTagNames;
	}

	/**
	 * Process each entity with an {@link AI} component added to them one at a
	 * time.
	 */
	@Override
	public void processEntity(Entity entity) {
		AI aiComponent = entity.getComponent(AI.class);
		Spatial aiSpatial = entity.getComponent(Spatial.class);
		Velocity aiVelocity = entity.getComponent(Velocity.class);

		switch (aiComponent.currentState) {
			case ROAM: {
				// accelerate to our maximum speed
				aiVelocity.currentState = Velocity.VelocityState.Accelerate;

				// Move to a random point ahead of us if we do not have a target
				// location
				aiComponent.roamCounter = (aiComponent.roamCounter + 1) % 200;
				if (aiComponent.targetSpatial == null || (aiComponent.roamCounter == 0)) {
					aiComponent.targetSpatial = getRandomRoamLocation(aiSpatial, 500, 1000);
				}

				// A list of entities that matched the tag that we are
				// seeking for
				List<Entity> matchingSeekTagEntities = getEntitiesWithTag(aiComponent.seekTagName);
				// Iterate through all of the entities that have the same
				// tag that we are seeking for
				for (Entity taggedEntity : matchingSeekTagEntities) {
					// Get the euclidian distance between the
					// seekTagName and the AI entity, if it's within our
					// searching range we should change states to
					// seeking
					Spatial taggedSpatial = taggedEntity.getComponent(Spatial.class);

					// If this entity is within the bounds of our seeking
					// distance change states and keep track of this entity
					if (Helpers.calculateEuclideanDistance(aiSpatial.x,
							aiSpatial.y, taggedSpatial.x, taggedSpatial.y) < aiComponent.seekingDistance) {
						// Keep track of this entity and turn to our seek state
						aiComponent.targetSpatial = taggedSpatial;
						aiComponent.currentState = AIState.SEEK;
						// Keep track of the entity that we're hunting for, so
						// that we can get any information from it later, and
						// for testing if it still exist when seeking towards
						// it. If we didn't do this test, we could potentially
						// seek towards an entity that gets killed for instance.
						// Which would be silly.
						aiComponent.targetEntity = taggedEntity;
						break;
					}
				}

				aiSpatial.setRotation(AIHelpers.rotateTowardsSmooth(
						aiSpatial, aiComponent.targetSpatial, Math.toRadians(5),
						aiComponent.maxTurningDistance));
				break;
			}
			case SEEK: {
				aiVelocity.currentState = Velocity.VelocityState.Accelerate;

				// Used to make sure that the AI doesn't focus on something
				// which ends up being removed. If we didn't have this,
				// there would be the possibility of hunting a target which
				// has died for instance, but we remain in a state of
				// SEEKing and do nothing
				if (!entityManager.contains(aiComponent.targetEntity)) {
					aiComponent.currentState = AIState.ROAM;
					break;
				}

				aiSpatial.setRotation(AIHelpers.rotateTowardsSmooth(
							aiSpatial, aiComponent.targetSpatial,
							Math.toRadians(5),
							aiComponent.maxTurningDistance));

				double distanceAway = Helpers.calculateEuclideanDistance(aiSpatial.x, aiSpatial.y,
						aiComponent.targetSpatial.x, aiComponent.targetSpatial.y);

				if (distanceAway < aiComponent.nearDistance) {
					aiComponent.currentState = AIState.ARRIVE;
				} else if (distanceAway > aiComponent.seekingDistance) {
					aiComponent.currentState = AIState.ROAM;
				}
				
				break;
			}
			case ARRIVE: {
				// We've arrived so start breaking
				aiVelocity.currentState = Velocity.VelocityState.Break;

				// Used to make sure that the AI doesn't focus on something
				// which ends up being removed. If we didn't have this,
				// there would be the possibility of hunting a target which
				// has died for instance, but we remain in a state of
				// SEEKing and do nothing
				if (!entityManager.contains(aiComponent.targetEntity)) {
					aiComponent.currentState = AIState.ROAM;
					break;
				}

				aiSpatial.setRotation(AIHelpers.rotateTowards(
							aiSpatial, aiComponent.targetSpatial, Math.toRadians(0.2)));

				double distanceAway = Helpers
							.calculateEuclideanDistance(aiSpatial.x,
									aiSpatial.y, aiComponent.targetSpatial.x,
									aiComponent.targetSpatial.y);

				if (distanceAway > aiComponent.nearDistance) {
					aiComponent.currentState = AIState.SEEK;
				} else if (distanceAway > aiComponent.seekingDistance) {
					aiComponent.currentState = AIState.ROAM;
				}

				// Fire a bullet if it has the shoot component
				// attached to it. This is rather specific logic. I'd prefer to
				// have created a scripted event that is overridable for this
				// logic. Sadly there is no time however.
				Shoot aiShoot = entity.getComponent(Shoot.class);
				if (aiShoot != null) {
					aiShoot.shootRequired = true;
				}
				break;
			}
				/**
				 * Accelerate to our fullest and switch the arguments for atan2
				 * so that we are always going in the opposite way of our tagged
				 * entity
				 */
			case FLEE: {
				aiVelocity.currentState = Velocity.VelocityState.Accelerate;

				// Used to make sure that the AI doesn't focus on something
				// which ends up being removed. If we didn't have this,
				// there would be the possibility of hunting a target which
				// has died for instance, but we remain in a state of
				// SEEKing and do nothing
				if (!entityManager.contains(aiComponent.targetEntity)) {
					aiComponent.currentState = AIState.ROAM;
					break;
				}

				// switch the arguments for atan2
				// so that we are always going in the opposite way of our tagged
				// entity
				aiSpatial.setRotation(AIHelpers.rotateTowards(
							aiComponent.targetSpatial, aiSpatial,
							aiComponent.maxTurningDistance));

				// If we are now far away from our target entity, change to the
				// roaming state again
				if (Helpers.calculateEuclideanDistance(aiSpatial.x, aiSpatial.y,
							aiComponent.targetSpatial.x, aiComponent.targetSpatial.y) < aiComponent.seekingDistance) {
					aiComponent.currentState = AIState.ROAM;
				}
				break;
			}
		}
	}
	/**
	 * As mentioned within the comments I didn't have enough time to implement
	 * the proper roam logic, IE, only move ahead of it
	 * 
	 * @return The new spatial location that we will roam towards
	 */
	private Spatial getRandomRoamLocation(Spatial actualSpatial, int minimumDistance, int maxDistance) {
		int randomX = Helpers.randomBetween(-Helpers.randomBetween(minimumDistance, maxDistance), Helpers.randomBetween(minimumDistance, maxDistance));
		int randomY = Helpers.randomBetween(-Helpers.randomBetween(minimumDistance, maxDistance), Helpers.randomBetween(minimumDistance, maxDistance));

		// Get a random place within the bounds of the map. (Missing some info)
		if (actualSpatial.x + randomX < 100 || actualSpatial.x < 100) {
			randomX = Math.abs(randomX);
		}

		if (actualSpatial.y + randomY < 100 || actualSpatial.x < 100) {
			randomY = Math.abs(randomY);
		}

		Spatial newSpatial = new Spatial(actualSpatial.x + randomX,
				actualSpatial.y + randomY,
				0, 0);
		return newSpatial;
	}

	/**
	 * A private inner class with generic helpers which would be used many times
	 * within AI logic choices. These could be brought to a public class file
	 * eventually, but as stated the AI system wasn't fully completed and
	 * refined to a high standard sadly.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	public static final class AIHelpers {
		/**
		 * 
		 * @param spatialOned
		 * @param spatialTwo
		 * @param sensitivity
		 *            Defined in radians
		 * @return
		 */
		public static double rotateTowards(Spatial spatialOne,
				Spatial spatialTwo, double sensitivity) {
			double coordX = (spatialTwo.x + spatialTwo.width / 2) - (spatialOne.x + spatialOne.width / 2);
			double coordY = (spatialTwo.y + spatialTwo.height / 2) - (spatialOne.y + spatialOne.height / 2);

			double angleOfRotation = Helpers.correctRadian(Math.atan2(coordY, coordX));

			return Math.abs(angleOfRotation - spatialOne.getRotation()) >= sensitivity ? angleOfRotation : spatialOne.getRotation();
		}

		/**
		 * @param spatialOne
		 * @param spatialTwo
		 * @param sensitivity
		 *            Defined in radians
		 * @return
		 */
		public static double rotateTowardsSmooth(Spatial spatialOne,
				Spatial spatialTwo, double sensitivity,
				double maximumTurningDistance) {
			double newRotationAngle = rotateTowards(spatialOne, spatialTwo, sensitivity);
			double oldRotation = spatialOne.getRotation();

			if (newRotationAngle != oldRotation) {
				newRotationAngle = oldRotation + ((oldRotation >
						newRotationAngle
								+ Math.PI || oldRotation < newRotationAngle) ?
								maximumTurningDistance : -maximumTurningDistance);
			}
			return Helpers.correctRadian(newRotationAngle);
		}
	}
}