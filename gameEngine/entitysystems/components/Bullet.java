package entitysystems.components;

import engine.misc.Helpers;
import entitysystem.core.Entity;
import entitysystem.core.IComponent;

/**
 * A bullet currently only stores the entity which created it, so that the
 * bullet doesn't harm its owner, this logic can be removed depending on how we
 * need it by changing damageOwner to true
 * 
 */
public class Bullet implements IComponent {
	/**
	 * The tag of the thing that created this. We do this so a player's bullet
	 * doesn't hurt a bullet for instance. This can be overridden by setting
	 * damageOwner to true
	 */
	public NameTag ignoreOwnerTag;
	/**
	 * If this bullet comes in contact with the thing that fired it we will
	 * decide whether or not to hurt them
	 */
	public boolean damageOwner;

	public Bullet() {
	}

	public Bullet(NameTag ownerTag) {
		this.ignoreOwnerTag = ownerTag;
	}

	public Bullet(NameTag ownerTag, boolean damageOwner) {
		this(ownerTag);
		this.damageOwner = damageOwner;
	}

	@Override
	public String toString() {
		return Helpers.concat("[Bullet component ::",
				" owner - ", ignoreOwnerTag,
				" damageOwner ", damageOwner,
				"]");
	}
}
