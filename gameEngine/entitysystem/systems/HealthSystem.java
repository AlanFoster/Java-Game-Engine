package entitysystem.systems;

import java.util.ArrayList;
import java.util.List;


import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.Bullet;
import entitysystems.components.Collide;
import entitysystems.components.Damage;
import entitysystems.components.DeleteComponent;
import entitysystems.components.Health;
import entitysystems.components.NameTag;
import entitysystems.components.Score;
import entitysystems.components.Shield;

/**
 * This system deals with the logic of when two entities with a damage component
 * and health component have collided. This system will test both of the
 * collided objects at once and remove from their health the amount of the
 * damage given. This class will also deal with 'shield' logic, IE things which
 * will reduce damage.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class HealthSystem extends ProcessEntitySystem {
	public HealthSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Health.class, Collide.class);
	}

	@Override
	public void processEntity(Entity entity) {
		// Our first test is to see whether or not we have been hit with an
		// entity which can damage us
		Collide collisions = entity.getComponent(Collide.class);
		entityLoop: for (Entity collidedEntity : collisions.collisionList) {
			Damage damageComponent = collidedEntity.getComponent(Damage.class);
			if (damageComponent != null) {
				// Test if we're a bullet explicitly. I'll change this soon.
				Bullet bulletComponent = collidedEntity.getComponent(Bullet.class);

				if (bulletComponent != null) {
					// Get the nametag of the objects we can't damage if we're a
					// bullet, IE it self or the owner
					NameTag ignoreNameTag = bulletComponent.ignoreOwnerTag;

					if (ignoreNameTag == entity.getComponent(NameTag.class)) {
						continue entityLoop;
					} else {
						// if we've hit something that we can damage, and it
						// isn't ourself, and we have a score component. give
						// ourselves a score. It's not exactly the most complex
						// score giving algorithm, I'll admit that.
						Score scoreComponent = entity.getComponent(Score.class);
						if (scoreComponent != null) {
							scoreComponent.totalScore += damageComponent.damageGiven * 20;
						}
					}
					collidedEntity.addComponentNoRefresh(new DeleteComponent());
				}

				Health entityHealthComponent = entity.getComponent(Health.class);
				List<Shield> entityShieldComponents = new ArrayList<Shield>();
				// Get all shields that may be attached to the entity
				ParentSystem.getAllChildrenTComponents(entity, Shield.class, entityShieldComponents);

				// We take the damage given to this entity. We do this so we can
				// take into account any shields etc that may reduce the damage
				// given
				int remainderDamage = damageComponent.damageGiven;

				/*
				 * Take into account any and all shields the entity is wearing.
				 * We take away the damage from the shield first of all. If a
				 * shield has enough reduceDamageAmount to make the
				 * remainderDamage 0 it will break. However if we end the loop
				 * and there's still damage remaining after damaging our
				 * shields, we take it away from the entity's health.
				 */
				for (Shield shield : entityShieldComponents) {
					if (shield.currentReduceDamageAmount > 0) {
						shield.currentReduceDamageAmount -= remainderDamage;

						if (shield.currentReduceDamageAmount < 0) {
							remainderDamage = Math.abs(shield.currentReduceDamageAmount);
							// We should remove this entity somehow
						} else {
							remainderDamage = 0;
							// Test for any more collisons
							continue entityLoop;
						}
					}
				}

				// Take away any left over damage if required
				if (remainderDamage > 0) {
					entityHealthComponent.currentHealth -= remainderDamage;
				}

				// If we're left over with negative health, we have been killed
				if (entityHealthComponent.currentHealth < 0) {
					entityHealthComponent.currentHealth = 0;
					// Add the delete component to the entity as it is dead
					entity.addComponentRefresh(new DeleteComponent());

					// We are dead, there's no need to test for any more
					// collisions
					break entityLoop;
				}
			}
		}
	}
}