package entitysystem.systems;

import engine.main.GameTime;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystems.components.Bullet;
import entitysystems.components.Damage;
import entitysystems.components.NameTag;
import entitysystems.components.Shoot;
import entitysystems.components.Spatial;
import entitysystems.components.TimedDelete;
import entitysystems.components.Velocity;

/**
 * This system deals with an entity that has a shoot component attached to them,
 * and they have requested to fire a bullet. Note, this system has been made to
 * accept a template entity of the bullet, IE a bullet can have any component
 * logic binded to it. However it should contain the basic components such as
 * velocity, spatial and damage
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class ShootingSystem extends EntitySystem {

	public ShootingSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Shoot.class, Spatial.class);
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
		for (Entity entity : entityList) {
			processEntity(entity, gameTime.getElapsedTimeMilli());
		}
	}

	/**
	 * Iterates over each entity and checks if they wish to to fire. An entity
	 * makes a request to fire by setting their shootComponent.shootRequired
	 * field to true. One requirement to shooting is that there is a minimum
	 * elapsed time before an entity can shoot again. This logic applies to all
	 * entities with the shoot component. IE, it is here that the player is also
	 * limited to shoot things.
	 * <p>
	 * Note :: This time requirement is enforced by the elapsedTime of the game
	 * loop. IE, if the ShootingSystem is paused, or the GameLayer/GameScreen
	 * that it is registered to becomes paused then it will not take this time
	 * into consideration.
	 * 
	 * @param entity
	 * @param elapsedTime
	 */
	public void processEntity(Entity entity, long elapsedTime) {
		Shoot shootComponent = entity.getComponent(Shoot.class);
		// Decrease their delayed shooting time so that they may be able to
		// fire again
		shootComponent.currentWaitingTime -= elapsedTime;
		if (shootComponent.shootRequired
				&& shootComponent.currentWaitingTime <= 0) {
			// Create an instance of their bullet template
			Entity spawnedBullet = shootComponent.bulletTemplate.createEntity();
			Spatial bulletSpatial = spawnedBullet.getComponent(Spatial.class);
			Spatial entitySpatial = entity.getComponent(Spatial.class);

			// Update the bullet's position to where it has been fired from,
			// and set the rotation to be the same as that which fired it
			bulletSpatial.x = entitySpatial.x;
			bulletSpatial.y = entitySpatial.y;
			bulletSpatial.setRotation(entitySpatial.getRotation());

			// Set the velocity logic which is stored within the entity's
			// Shoot component
			Velocity bulletVelocity = spawnedBullet.getComponent(Velocity.class);
			bulletVelocity.acceleration = bulletVelocity.currentVelocity = bulletVelocity.velocityCap = shootComponent.bulletSpeed;

			// Give the bullet the damage details, this is what will be used
			// to reduce the health of an entity it collides with (assumming
			// it has a Health component)
			Damage bulletDamageComponent = spawnedBullet.getComponent(Damage.class);
			bulletDamageComponent.damageGiven = shootComponent.bulletDamage;

			// Add the timd delete component, so that it will be removed from
			// the system after the required duration in milliseconds has passed
			spawnedBullet.addComponentNoRefresh(new TimedDelete(shootComponent.bulletLife));

			// Start the bullet accelerating as expected, the VelocitySystem
			// will handle this for us
			bulletVelocity.currentState = Velocity.VelocityState.Accelerate;

			// Place a the tag of the entity that fired the bullet so that
			// other systems can base logic around the it. For instance the
			// damage system may consider if a bullet can hurt the owner, or for
			// things like friendly fire possibly
			Bullet bulletComponent = spawnedBullet.getComponent(Bullet.class);
			bulletComponent.ignoreOwnerTag = entity.getComponent(NameTag.class);

			// Reset the shootRequired to false, as we've dealt with this
			// bullet now. If we didn't do this it would continuously shoot.
			// At this point we also reset the shooting time so that another
			// bullet can't be fired right away
			shootComponent.shootRequired = false;
			shootComponent.currentWaitingTime = shootComponent.requiredWaitingTime;
		}
	}
}