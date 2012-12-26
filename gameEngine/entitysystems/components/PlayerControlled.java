package entitysystems.components;

import java.awt.event.KeyEvent;

import entitysystem.core.IComponent;
import entitysystem.systems.PlayerControlledSystem;

/**
 * Add this component to an entity that is controlled by a player. This class
 * processes each entity which has the {@link PlayerControlled} component added
 * to them, and a velocity.
 * <p>
 * Within the {@link PlayerControlledSystem} I mention how this implementation
 * of player controlled entities is too specific, and that this is something
 * that could be worked on.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class PlayerControlled implements IComponent {
	/**
	 * The default keys to listen for directional movement
	 */
	public int listenUp = KeyEvent.VK_W;
	public int listenDown = KeyEvent.VK_S;
	public int listenLeft = KeyEvent.VK_A;
	public int listenRight = KeyEvent.VK_D;

	/**
	 * The turning speed when the listenLeft or listenRight key is pressed
	 */
	public int turningSpeed;

	// Whether or not the entity can be controlled
	public PlayerControlled(int turningSpeed) {
		this.turningSpeed = turningSpeed;
	}

	/**
	 * Creates a new player controlled component
	 * 
	 * @param turningSpeed
	 *            The turning speed when the listenLeft or listenRight key is
	 *            pressed
	 * @param listenUp
	 *            The key which causes the entity to move forwards
	 * @param listenDown
	 *            The key which causes the entity to move backwards
	 * @param listenLeft
	 *            The key to rotate right
	 * @param listenRight
	 *            The key to rotate left
	 */
	public PlayerControlled(int turningSpeed, int listenUp, int listenDown, int listenLeft, int listenRight) {
		this(turningSpeed);

		this.listenUp = listenUp;
		this.listenDown = listenDown;
		this.listenLeft = listenLeft;
		this.listenRight = listenRight;
	}
}