package templates;

import java.awt.image.BufferedImage;

import world.DrawOrder;
import world.World;
import engine.assets.Asset;
import engine.assets.GameGraphic;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntityTemplate;
import entitysystems.components.Animate;
import entitysystems.components.ChildSpatial;
import entitysystems.components.Children;
import entitysystems.components.Collectable;
import entitysystems.components.Collide;
import entitysystems.components.Degrade;
import entitysystems.components.Draw;
import entitysystems.components.Equipment;
import entitysystems.components.NameTag;
import entitysystems.components.Shield;
import entitysystems.components.Spatial;

/**
 * Creates a new shield entity which can be equiped to any entity as required.
 * Just add this entity as a child entity, using {@link Children}
 * <p>
 * Note, this class is just a demonstration of how an entity template will work
 * in theory, and isn't a completed implementation, IE, it would obviously be
 * expected that enemies will be visually different etc, but it's easy to add
 * more adjustable information through this class for extensibility.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class TankShieldTemplate extends EntityTemplate {
	private static BufferedImage[] shieldGraphics = GameAssetManager.getInstance().getObject(BufferedImage[].class, "animatedPlayerShield");

	@Override
	public Entity createEntity() {
		Entity shield = new Entity(
				entityManager,
				entityManager.createEntity(),
				new Spatial(0, 0, shieldGraphics[0].getWidth(), shieldGraphics[0].getHeight()),
				new ChildSpatial(false),
				new Draw(shieldGraphics[0],
						DrawOrder.SHIELDS),
				new Animate(shieldGraphics, 0,
						0, true, false, 50),
				new Collide(),
				new Collectable(),
				new Shield(500),
				new Equipment(-27, -27),
				new NameTag("shield"));
		return shield;
	}
}