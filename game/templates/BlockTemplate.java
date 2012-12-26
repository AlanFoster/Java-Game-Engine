package templates;

import java.awt.image.BufferedImage;

import world.DrawOrder;

import engine.misc.managers.GameAssetManager;
import entitysystem.core.Entity;
import entitysystem.core.EntityTemplate;
import entitysystems.components.Collide;
import entitysystems.components.Degrade;
import entitysystems.components.Draw;
import entitysystems.components.NameTag;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;

/**
 * This method is used to conceptually store a reproducible entity in a single
 * class.
 * <P>
 * Creates a basic entity which is used to demonstrate the 'Degrade' component,
 * which when being hit with another entity which has the collide component and
 * damage component. Visually this component will 'degrade' at the area in which
 * the collision has occurred.
 * <p>
 * Note, this class is just a demonstration of how an entity template will work
 * in theory, and isn't a completed implementation 'generic'/'dynamic'
 * implementation, IE, no constructor set values etc. It's just an example of
 * how one might use the templating system.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class BlockTemplate extends EntityTemplate {
	static BufferedImage brick_graphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "tiles_brick");

	@Override
	public Entity createEntity() {
		Entity bulletEntity = new Entity(entityManager,
				entityManager.createEntity(),
				new Spatial(450, 450, brick_graphic.getWidth(), brick_graphic.getHeight()),
				new Draw(brick_graphic, DrawOrder.DEGRADE_TESTS),
				new NameTag("shield"),
				new Collide(),
				new Velocity(0, 0),
				new Degrade());
		return bulletEntity;
	}
}