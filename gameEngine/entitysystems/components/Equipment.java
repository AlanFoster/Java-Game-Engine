package entitysystems.components;

import entitysystem.core.IComponent;

/**
 * If an item is equippable when added to a parent entity it will be
 * relatively drawn. This will be useful for shields etc.
 * 
 * @author alan
 * 
 */
public class Equipment implements IComponent {
	public float x, y;

	public Equipment(float x, float y) {
		this.x = x;
		this.y = y;
	}
}