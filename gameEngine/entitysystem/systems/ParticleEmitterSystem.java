package entitysystem.systems;

import java.awt.image.BufferedImage;
import java.util.Queue;

import world.DrawOrder;

import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystems.components.Draw;
import entitysystems.components.Particle;
import entitysystems.components.ParticleEmitter;
import entitysystems.components.Spatial;
import entitysystems.components.Velocity;

/**
 * Manages all entities which have the {@link ParticleEmitter} class component
 * and a {@link Spatial} class component.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @See ParticleEmitter
 * @see Spatial
 */
public class ParticleEmitterSystem extends EntitySystem {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(ParticleEmitterSystem.class);

	public ParticleEmitterSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(ParticleEmitter.class, Spatial.class);
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
		for (Entity entity : entityList) {
			customProcess(entity, gameTime);
		}
	}

	public void customProcess(Entity entity, GameTime gameTime) {
		Spatial spatial = entity.getComponent(Spatial.class);
		ParticleEmitter emitterDetails = entity.getComponent(ParticleEmitter.class);

		// If the first element of our list of current particles is empty, it's
		// safe to assume the rest are also. This will be the first time a
		// particle emitter has been added to the system, so we create the list
		// of possible particles so that random ones can be selected and used
		if (emitterDetails.cachedParticleImages[0] == null) {
			createCachedParticles(emitterDetails);
		}

		updateAllExistingParticles(emitterDetails, gameTime.getElapsedTimeMilli());
		createMoreParticles(emitterDetails, spatial.x, spatial.y);

		// Make an explicit call to the entity manager to update its lists
		entityManager.refreshEntityLists();
	}

	protected void updateAllExistingParticles(ParticleEmitter emitterDetails, long elapsedTimeMilliSecs) {
		// If this emitter detail slot is free create, grab a new bunch
		// of values and reset the particle, reusing the original entity
		// for (int j = 0; j < emitterDetails.maximumParticles; j++) {
		Queue<Entity> currentParticles = emitterDetails.currentParticles;
		int startingSize = currentParticles.size();
		int i = 0;
		while (!currentParticles.isEmpty() && i++ < startingSize) {
			Entity particleEntity = emitterDetails.currentParticles.poll();

			Particle particleDetails = particleEntity.getComponent(Particle.class);
			particleDetails.timeAlive -= elapsedTimeMilliSecs;

			// Decide either to add it to our dead queue, or to keep it for the
			// next round of updates
			if (particleDetails.timeAlive > 0) {
				currentParticles.offer(particleEntity);
			} else {
				emitterDetails.deadParticles.add(particleEntity);
			}
		}
	}

	protected void createMoreParticles(ParticleEmitter emitterDetails, float x, float y) {
		int particlesToCreate = Helpers.randomBetween(emitterDetails.minimumParticles, emitterDetails.maximumParticles);
		for (int i = 0; i < particlesToCreate && emitterDetails.currentParticles.size() < emitterDetails.maximumTotalParticles; i++) {
			// Poll a particle from the current dead particles
			Entity deadParticle = emitterDetails.deadParticles.poll();
			if (deadParticle == null) {
				deadParticle = createParticleEntity(emitterDetails.cachedParticleImages[0]);
			}
			/**
			 * Reset all of the dead particle's information to randomised values
			 */
			BufferedImage particleImage = emitterDetails.cachedParticleImages[Helpers.randomBetween(0, emitterDetails.cachedParticleImages.length)];

			// Get all of the details for this particle that we'll need
			double randomAngle = Helpers.randomBetween(emitterDetails.startAngle, emitterDetails.endAngle);
			int particleLife = Helpers.randomBetween(emitterDetails.minimumLife, emitterDetails.maximumLife);

			// Get all of the components added to the entity and reset their
			// details
			Spatial spatial = deadParticle.getComponent(Spatial.class);
			Draw draw = deadParticle.getComponent(Draw.class);

			draw.setRawImage(particleImage);

			deadParticle.getComponent(Particle.class).timeAlive = particleLife;

			spatial.x = x;
			spatial.y = y;
			spatial.width = particleImage.getWidth();
			spatial.height = particleImage.getHeight();
			spatial.setRotation(randomAngle);

			emitterDetails.currentParticles.offer(deadParticle);
		}
	}

	/**
	 * Iterates through all of the possible particle images, and all of the
	 * required colours that can be generated and stores them within the
	 * ParticleEmitter for reuse later. This takes a new copy of the image, and
	 * preserves the original alpha values and applies the new colour through
	 * bitmasking/bitshifting/bitwise operators
	 * 
	 * @param emitterDetails
	 */
	protected void createCachedParticles(ParticleEmitter emitterDetails) {
		int particleNumber = 0;
		for (int colorHex : emitterDetails.possibleColors) {
			for (BufferedImage particleImage : emitterDetails.particleGraphics) {
				// Take a copy of our image so we can freely modify it without
				// affecting the original one, which is essential for particle
				// effects. We are caching this newly created image, so it's not
				// /too/ much overhead
				BufferedImage newParticleImage = new BufferedImage(particleImage.getWidth(), particleImage.getHeight(), BufferedImage.TRANSLUCENT);

				for (int pixelX = 0; pixelX < particleImage.getWidth(); pixelX++) {
					for (int pixelY = 0; pixelY < particleImage.getHeight(); pixelY++) {
						int originalAlpha = (particleImage.getRGB(pixelX, pixelY) & 0xFF << 24);
						newParticleImage.setRGB(pixelX, pixelY, originalAlpha | colorHex);
					}
				}

				emitterDetails.cachedParticleImages[particleNumber++] = newParticleImage;
			}
		}
	}

	/**
	 * Creates a blank particle entity which has the basic basic components
	 * added to it.
	 * 
	 * @return
	 */
	protected Entity createParticleEntity(BufferedImage startingImage) {
		Entity particle = new Entity(entityManager, entityManager.createEntity(),
				new Spatial(0, 0, startingImage.getWidth(), startingImage.getHeight()),
				new Draw(startingImage, DrawOrder.PARTICLES),
				new Particle(),
				new Velocity(5, 2, 1, 5)
				);
		return particle;
	}

	/**
	 * 
	 * @param x
	 *            The X location that particles will be emitted from (top left
	 *            is 0, 0)
	 * @param y
	 *            The Y location that particles will be emitted from (top left
	 *            is 0, 0)
	 * @param startAngle
	 *            The starting angle from which particles can be emitted from.
	 *            This angle starts from the 'right' hand side.
	 * 
	 *            <pre>
	 *      |
	 * _____|_____ <-- Start Angle of 0 here
	 *      |
	 *      |
	 * </pre>
	 * @param endAngle
	 *            The end angle, this must be greater than the start angle
	 */
	protected void resetParticleDetails(Entity entity,
			float x, float y,
			BufferedImage[] particleImages,
			double startAngle, double endAngle,
			int minimumLife, int maximumLife) {

	}
}