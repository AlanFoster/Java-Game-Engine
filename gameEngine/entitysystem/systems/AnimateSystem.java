package entitysystem.systems;

import engine.main.GameTime;
import engine.misc.GameLogging;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystems.components.Animate;
import entitysystems.components.Draw;

/**
 * This animation system handles all of the animation of entities with the
 * components {@link Animate} and {@link Draw}.
 * <p>
 * This system will iterate through each entity and update all of the raw fields
 * that the {@link Animate} component provides.
 * <p>
 * See the super class for what an EntitySystem is.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see Draw
 * @see Animate
 */
public class AnimateSystem extends EntitySystem {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(AnimateSystem.class);

	/**
	 * 
	 * @param entityManager
	 *            We require access to the entity manager so that we can acquire
	 *            any entities from the entity manager when required. We use
	 *            this within the refreshList to get keep track of all entities
	 *            with specific components that we will look after and perform
	 *            logic on.
	 */
	public AnimateSystem(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public void refreshList() {
		// Get all entities that have an animate and draw component
		// Note :: The component 'Animate' does not store the 'current' visual
		// graphic, instead the draw component does :) Therefore we set the
		// graphic of the Draw class to be equal to the current frame that we
		// want to show for the animation, so that the actual render system can
		// take care of drawing for us
		entityList = entityManager.getEntitiesContaining(Animate.class, Draw.class);
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
		// iterate over each enemy and process them individually with the
		// elapsed time in milliseconds
		for (Entity entity : entityList) {
			processEntity(entity, gameTime.getElapsedTimeMilli());
		}
	}

	public void processEntity(Entity entity, long elapsedTime) {
		// ac standing for 'animateComponent'.
		Animate ac = entity.getComponent(Animate.class);
		ac.totalTimeShown += elapsedTime;
		// If we have been runing slow we will iterate over each graphic as
		// required until our total time shown is less than our desired time per
		// image
		while (ac.totalTimeShown > ac.timePerImage) {
			ac.totalTimeShown -= ac.timePerImage;
			setNextGraphic(entity, ac);
		}
	}

	/**
	 * Sets the draw component graphic to be the next graphical image which
	 * needs to be shown next. It is here that the animation will be removed
	 * from the system if removeWhenDone is set to true.
	 * 
	 * @param entity
	 *            The entity that needs to have its current graphic changed to
	 *            the next possible animation
	 * @param ac
	 *            The Animate component that we will directly manipulate.
	 *            Normally i wouldn't name things like that, but it would look
	 *            horribly verbose otherwise if it was fully named
	 */
	private void setNextGraphic(Entity entity, Animate ac) {
		// Set the draw component's current drawn image to the be current index
		// of the animation
		entity.getComponent(Draw.class).setImage(ac.children[ac.index]);
		// Calculuate the new index position of the image
		if (ac.index < ac.children.length && !ac.isReversing) {
			ac.index++;
		} else if (ac.index >= 0 && ac.isReversing) {
			ac.index--;
		} else {
			// Give a logging error if somehow we have recieved an array out of
			// bounds. Logically speaking this /should/ never been thrown, but
			// best to have it
			logger.error(new ArrayIndexOutOfBoundsException(), "Animation index ", ac.index,
					" out of bounds for element with ", ac.children.length, " children.");
		}

		// If we've hit the bounds required, test for removal
		// Remember index++ will it make equal to children size if at
		// the end..
		if ((ac.index == 0 && ac.isReversing) || (ac.index == ac.children.length && !ac.isReversing)) {
			if (++ac.currentLoopCount == ac.desiredLoopAmount && ac.removeWhenDone) {
				// Once the loop has been finished, and it has requested to be
				// removed when done, tell the entity manager to remove this
				// entity
				entityManager.removeEntity(entity);
			} else {
				ac.index = ac.reverseWhenDone && !ac.isReversing ? ac.children.length - 1 : 0;
				if (ac.reverseWhenDone)
					ac.isReversing = !ac.isReversing;
			}
		}
	}
}