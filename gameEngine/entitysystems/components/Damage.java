package entitysystems.components;

import entitysystem.core.IComponent;

/**
 * Attach this to an entity that wishes to give damage when it comes into
 * contact with any other entity that has a Health component attached to the
 * entity
 * 
 */
public class Damage implements IComponent {
	public int damageGiven;

	/**
	 * 
	 * @param damageGiven
	 *            The damage given on contact with an entity with a health
	 *            component
	 */
	public Damage(int damageGiven) {
		this.damageGiven = damageGiven;
	}
}