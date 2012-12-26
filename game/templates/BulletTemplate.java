package templates;

import java.awt.image.BufferedImage;

import world.DrawOrder;
import world.World;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntityTemplate;
import entitysystem.systems.ShootingSystem;
import entitysystems.components.Bullet;
import entitysystems.components.Collide;
import entitysystems.components.Damage;
import entitysystems.components.Draw;
import entitysystems.components.NameTag;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;

/**
 * This method is used to conceptually store a reproducible entity in a single
 * class.
 * <P>
 * Within this Template we offer a basic BulletTemplate. This contains all of
 * the components required for the system {@link ShootingSystem}, and any
 * additional components required such as the velocity and draw order of
 * bullets.
 * <p>
 * This class provides the basic entity with components expected for a bullet to
 * own
 * <p>
 * Note, this class is just a demonstration of how an entity template will work
 * in theory, and isn't a completed implementation 'generic'/'dynamic'
 * implementation, IE, no constructor set values etc. It's just an example of
 * how one might use the templating system.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class BulletTemplate extends EntityTemplate {
	static BufferedImage bulletGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "artillery");

	/**
	 * Creates a new entity which will be handled automatically by the entity
	 * system, and then returns the reference to it.
	 * <p>
	 * For instance in the case of {@link ShootingSystem}
	 */
	@Override
	public Entity createEntity() {
		Entity bulletEntity = new Entity(entityManager,
				entityManager.createEntity(),
				new Bullet(),
				new Spatial(0, 0, bulletGraphic.getWidth(), bulletGraphic.getHeight()),
				new Draw(bulletGraphic, DrawOrder.BULLETS),
				new NameTag("bullet"),
				new Collide(),
				new Velocity(0, 0),
				new Damage(0));
		return bulletEntity;
	}
}