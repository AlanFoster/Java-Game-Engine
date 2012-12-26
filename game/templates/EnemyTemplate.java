package templates;

import java.awt.image.BufferedImage;

import world.DrawOrder;
import world.World;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.Entity;
import entitysystem.core.EntityTemplate;
import entitysystems.components.AI;
import entitysystems.components.Children;
import entitysystems.components.Collide;
import entitysystems.components.Draw;
import entitysystems.components.Health;
import entitysystems.components.HealthBar;
import entitysystems.components.MinimapDrawn;
import entitysystems.components.NameTag;
import entitysystems.components.Shield;
import entitysystems.components.Shoot;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;

/**
 * Creates a new enemy entity when createEntity is called, this will
 * automatically be picked up by any entity systems.
 * <p>
 * By default this template creates an entity which will be drawn to the
 * minimap, and be drawn visually by the 'enemy' asset name graphic, and has a
 * basic AI added to it. All enemies can shoot bullets
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
public class EnemyTemplate extends EntityTemplate {
	BufferedImage enemyGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "enemy");

	@Override
	public Entity createEntity() {
		Entity enemyEntity = new Entity(entityManager,
				entityManager.createEntity(), new Spatial(0, 0,
						enemyGraphic.getWidth(), enemyGraphic.getHeight()),
				new Draw(enemyGraphic, DrawOrder.ENEMY),
				new MinimapDrawn(0xFFFFFF),
				// random AI, just for testing
				new NameTag("enemy"), new AI("player",
						Math.random() > 0.5 ? AI.AIState.FLEE : AI.AIState.ROAM, AI.AIState.ROAM, AI.AIState.SEEK),
				new Collide(),
				new Velocity(5, 2),
				new Shoot(new BulletTemplate(), 0, 0, 7,
						5000, 1, 1000),
				new Health(500),
				new HealthBar());

		// Randomly add a shield to an enemy
		if (Math.random() > 0.5) {
			Entity shield = new TankShieldTemplate().createEntity();
			enemyEntity.addComponentNoRefresh(new Children(shield));
		}
		return enemyEntity;
	}
}