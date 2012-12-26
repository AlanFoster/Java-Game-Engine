package entitysystems.components;

import java.awt.image.BufferedImage;

import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.IComponent;
import entitysystem.systems.HealthBarSystem;
import entitysystem.systems.HealthSystem;

/**
 * A health component which stores the starting health and current health. This
 * will be used by any system which makes use of health, ie
 * {@link HealthBarSystem} and {@link HealthSystem}
 * <p>
 * This class also implements {@link IHUDable} which allows for its details to
 * be shown on the HUD screen if attached to an entity that has the camera
 * attached to it.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class Health implements IComponent, IHUDable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The starting health that was initally given to this component within the
	 * constructor. This will not change when the entity is hurt.
	 */
	public int startingHealth;
	/**
	 * Stores the current health, when the entity is hurt, this value will be
	 * decremented.
	 */
	public int currentHealth;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * creates a new Health component
	 * 
	 * @param startingHealth
	 *            The starting health of this component. This starting health
	 *            will also be set to the current health.
	 */
	public Health(int startingHealth) {
		this.currentHealth = this.startingHealth = startingHealth;
	}

	@Override
	public String toString() {
		return Helpers.concat("[health component, : Health : ", currentHealth, "]");
	}

	// ----------------------------------------------------------------
	// IHUDable implementations
	// ----------------------------------------------------------------
	/**
	 * The graphic that shows within the HUD System
	 */
	private static final BufferedImage hudGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "healthHud");

	/**
	 * Returns the current icon which will be used to represent an icon within
	 * the HUD
	 */
	@Override
	public BufferedImage getHUDIcon() {
		return hudGraphic;
	}

	/**
	 * A string of relevent information that wants to be drawn in combination
	 * with the hud icon. For instance it may be useful to present the
	 * statistics like "Health : 62/100"
	 */
	@Override
	public String getHUDDetails() {
		return Helpers.concat("health :: \n", currentHealth, " / ", startingHealth);
	}
}