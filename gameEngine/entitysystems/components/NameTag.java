package entitysystems.components;

import entitysystem.core.IComponent;

/**
 * For entities that we wish to conceptualize as being an identifiable
 * object should add this component. This tag will be useful for debugging,
 * and can be used for Hud purposes too possibly. For instance we may have a
 * powerup entity having tag of "Shield" which can be outputted directly to
 * the hud when collected. It will also be useful for debugging when things
 * go wrong.
 * <p>
 * It is therefore expected that many entities will share the same nametag,
 * so possibly we can share the same NameTag instance between multiple
 * entities.
 * 
 * @author alan
 * 
 */
public class NameTag implements IComponent {
	public String name;

	public NameTag(String name) {
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}