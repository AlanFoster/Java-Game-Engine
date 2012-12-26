package entitysystems.components;

import entitysystem.core.IComponent;

public class HealthBar implements IComponent {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The width of the health bar shown for this entity. The default value is
	 * 100px
	 */
	public int healthBarWidth = 100;
	/**
	 * The height of the health bar shown for this entity. The default value is
	 * 10 px;
	 */
	public int healthBarHeight = 10;
	/**
	 * The Y offset, relative to the entity's spatial. The default value is
	 * -10px
	 */
	public int healthBarYOffset = -10;

	
	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new health bar component with the default health bar data sizes
	 */
	public HealthBar() {
	}

	/**
	 * Creates a new health bar component with the required data
	 * 
	 * @param healthBarWidth
	 *            The width of the health bar shown for this entity
	 * @param healthBarHeight
	 *            The height of the health bar shown for this entity
	 * @param healthBarYOffset
	 *            The Y offset, relative to the entity's spatial
	 */
	public HealthBar(int healthBarWidth, int healthBarHeight, int healthBarYOffset) {
		this.healthBarWidth = healthBarWidth;
		this.healthBarHeight = healthBarHeight;
		this.healthBarYOffset = healthBarYOffset;
	}
}
