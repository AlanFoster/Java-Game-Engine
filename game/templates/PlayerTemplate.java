package templates;

import java.awt.image.BufferedImage;

import world.DrawOrder;
import world.World;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntityTemplate;
import entitysystems.components.Camera;
import entitysystems.components.ChildSpatial;
import entitysystems.components.Children;
import entitysystems.components.Collide;
import entitysystems.components.Draw;
import entitysystems.components.FollowMouse;
import entitysystems.components.HUDableComponent;
import entitysystems.components.Health;
import entitysystems.components.HealthBar;
import entitysystems.components.MinimapDrawn;
import entitysystems.components.NameTag;
import entitysystems.components.ParticleEmitter;
import entitysystems.components.PlayerControlled;
import entitysystems.components.Score;
import entitysystems.components.Shoot;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;

/**
 * Creates a new Player tank entity with moving capabilities, a tank body, tank
 * barrel etc. This entity and all child entities will recieve the identifying
 * player tag. This entity will also recieve minimap drawn colour of white.
 * <p>
 * Note, this class is just a demonstration of how an entity template will work
 * in theory, and isn't a completed implementation 'generic'/'dynamic'
 * implementation, IE, no constructor set values etc. It's just an example of
 * how one might use the templating system.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class PlayerTemplate extends EntityTemplate {
	static BufferedImage playerBarrelGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "playerBarrel");
	static BufferedImage playerGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "player");
	static BufferedImage particleGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "particle");
	static NameTag playerNameTag = new NameTag("player");

	@Override
	public Entity createEntity() {
		// Create the name tag for the entity for debugging and for any systems
		// that require the logic

		// The player barrel which can shoot bullets
		Entity playerBarrel = new Entity(entityManager,
				entityManager.createEntity(), new Spatial(0, 0,
						playerBarrelGraphic.getWidth(),
						playerBarrelGraphic.getHeight()),
				new Draw(playerBarrelGraphic, DrawOrder.BARRELS),
				new FollowMouse(), new ChildSpatial(false),
				new Shoot(new BulletTemplate(), 0,
						0, 7, 2000, 50, 500),
				playerNameTag
				);

		// Entities created already exist within the entity manager, and are
		// thus handled automatically by any systems implicitly
		Entity playerTankBody = new Entity(entityManager,
				entityManager.createEntity(),
				new Spatial(0, 0, playerGraphic.getWidth(), playerGraphic
						.getHeight()), new Draw(playerGraphic,
						DrawOrder.PLAYER),
				new ChildSpatial(true),
				playerNameTag);

		// Tank!
		// The playerControlled is applied to the tank itself, and not any
		// of the entities it possess. We are able to do this as we add the
		// other entities as children, so what when the tank entity moves, the
		// entity parts move with it nicely
		Entity playerTank = new Entity(entityManager,
				entityManager.createEntity(),
				new Spatial(300, 200, playerGraphic.getWidth(),
						playerGraphic.getHeight()),
				new Health(1000),
				new Score(),
				new Velocity(7, 7),
				new Camera(),
				new Children(playerTankBody, playerBarrel),
				new HealthBar(),
				new ParticleEmitter(0, Math.PI * 2,
						200, 601,
						new int[] { 0xF9FF4F, 0xEBBF49, 0xf38918, 0xd25715 },
						10, 30,
						100,
						15, 25,
						particleGraphic
				),
				new PlayerControlled(5),
				new HUDableComponent(),
				new MinimapDrawn(0xFFFFFF),
				new Collide(),
				playerNameTag
				);

		return playerTank;
	}
}