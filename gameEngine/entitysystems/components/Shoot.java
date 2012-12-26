package entitysystems.components;

import entitysystem.core.EntityTemplate;
import entitysystem.core.IComponent;

/**
 * If
 * 
 * 
 */
public class Shoot implements IComponent {
	public EntityTemplate bulletTemplate;

	public int shootingX, shootingY;

	// Information about the type of bullet it will create
	public int bulletSpeed;
	// The duration of the bullet, in milliseconds. IE a value of 1000 will
	// cause the bullet to be removed in one second.
	public int bulletLife;
	public int bulletDamage;

	// The firing restriction in milliseconds
	public int requiredWaitingTime;

	// After a bullet has been fired, this value is set to the
	// requiredWaitingTime and each game update this value decreases. If
	// this value is greater than 0, the entity with this component will not
	// be able to fire their bullet
	public int currentWaitingTime;

	// A flag picked up by the ShootingSystem. When true it will fire a
	// bullet.
	public boolean shootRequired;

	/**
	 * 
	 * @param bulletTemplateint
	 *            The template of the entity that will be created when the
	 *            'fire' is issued. This entity should contain Spatial,
	 *            Damage, and Velocity. Once a request is made to fire,
	 *            through setting 'shootRequired' to false, the shooting
	 *            system will create this template and update the entitie's
	 *            component values with those of the constructor.
	 * @param shootingX
	 *            The X location to shoot the bullet from, from the center
	 *            of the the spatial that this component is attached to.
	 * @param shootingY
	 *            The Y location to shoot the bullet from, from the center
	 *            of the the spatial that this component is attached to.
	 * @param bulletSpeed
	 *            The starting velocity that will be given to the created
	 *            bullet entity
	 * @param bulletLife
	 *            The life of the bullet, in milliseconds. After this time
	 *            has surpassed it will be removed from the entity system
	 * @param bulletDamage
	 *            The damage given to an object when a collision occurs
	 *            between the bullet and an entity that can be damaged
	 *            (Currently defined as having the Health.class component)
	 * @param requiredWaitingTime
	 *            The minimum waiting time before an entity can fire again.
	 *            In milliseconds.
	 */
	public Shoot(EntityTemplate bulletTemplate,
			int shootingX, int shootingY, int bulletSpeed, int bulletLife,
			int bulletDamage, int requiredWaitingTime) {
		this.bulletTemplate = bulletTemplate;
		this.shootingX = shootingX;
		this.shootingY = shootingY;
		this.bulletSpeed = bulletSpeed;
		this.bulletLife = bulletLife;
		this.bulletDamage = bulletDamage;
		this.requiredWaitingTime = requiredWaitingTime;
	}
}