package entitysystem.systems;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.GameViewPort;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystems.components.Health;
import entitysystems.components.HealthBar;
import entitysystems.components.Spatial;

/**
 * Draws the health bars of all entities which have the components
 * {@link Health}, {@link HealthBar} and {@link Spatial}.
 * <p>
 * This class iterates through all entities during the draw method and
 * calculates where to draw the health bar, relative to the entity's spatial
 * location and draws a percentage bar depicting the entities health. The
 * Current health will be shown in green, and 'depleted' health will be shown in
 * red.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class HealthBarSystem extends EntitySystem implements IDrawableLogical {
	GameViewPort viewport;

	public HealthBarSystem(EntityManager entityManager, GameViewPort viewPort) {
		super(entityManager);
		this.viewport = viewPort;
	}

	@Override
	public void refreshList() {
		// We only care about entities with a HealthBar AND a healthbar, for
		// instance an entity
		// may have health, but no health bar associated with it
		entityList = entityManager.getEntitiesContaining(HealthBar.class, Health.class, Spatial.class);
	}

	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		for (Entity entity : entityList) {
			Spatial spatial = entity.getComponent(Spatial.class);
			Health health = entity.getComponent(Health.class);
			HealthBar healthBarInfo = entity.getComponent(HealthBar.class);
			
			// Draw the red
			drawScreen.setColor(Color.red);
			drawScreen.fillRect((int) ((spatial.x + spatial.width / 2 - healthBarInfo.healthBarWidth / 2) - viewport.getX()),
					(int) ((spatial.y - spatial.height / 2 + healthBarInfo.healthBarYOffset) - viewport.getY()),
					healthBarInfo.healthBarWidth, healthBarInfo.healthBarHeight);

			// Draw the green
			if (health.currentHealth > 0) {
				float healthPercentage = (float) health.currentHealth / health.startingHealth;
				int greenWidth = (int) (healthBarInfo.healthBarWidth * healthPercentage);

				drawScreen.setColor(Color.green);
				drawScreen.fillRect((int) ((spatial.x + spatial.width / 2 - healthBarInfo.healthBarWidth / 2) - viewport.getX()),
						(int) ((spatial.y - spatial.height / 2 + healthBarInfo.healthBarYOffset) - viewport.getY()),
						greenWidth, healthBarInfo.healthBarHeight);
			}

			// Draw the border
			drawScreen.setColor(Color.black);
			drawScreen.drawRect((int) ((spatial.x + spatial.width / 2 - healthBarInfo.healthBarWidth / 2) - viewport.getX()),
					(int) ((spatial.y - spatial.height / 2 + healthBarInfo.healthBarYOffset) - viewport.getY()),
					healthBarInfo.healthBarWidth, healthBarInfo.healthBarHeight);
		}
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
	}
}
