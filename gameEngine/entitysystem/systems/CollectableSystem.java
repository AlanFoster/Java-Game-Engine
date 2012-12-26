package entitysystem.systems;

import hud.DrawRelativeToViewport;
import hud.HUDTimer;

import java.util.Observable;

import world.World;
import engine.misc.Timer;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.EntitySystem;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.Camera;
import entitysystems.components.Children;
import entitysystems.components.Collectable;
import entitysystems.components.Collide;
import entitysystems.components.Equipment;
import entitysystems.components.NameTag;
import entitysystems.components.Spatial;

/**
 * This system manages 'collectables' within the game. When a collectable comes
 * into contact with another entity they will be 'given' the collectable, what
 * ever it is. Currently collectables only consist of equitable items, which
 * will automatically be added as children to the entity that acquired it.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class CollectableSystem extends ProcessEntitySystem {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(HUDSystem.class);

	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * Direct access to the hudSystem so that we can request for timed messages
	 * to appear on the screen.
	 */
	private HUDSystem hudSystem;
	/**
	 * The amount of time a collected message will appear on the screen for.
	 * This could be added to the constructor if required, but you'd expect all
	 * hud messages from collecting items to be the same i'd assume? This is
	 * easy to change however, so it's not that important.
	 */
	private static final int messageLength = 2000;
	/**
	 * The location of the drawn hud message. This could be added to the
	 * constructor if required, but you'd expect all hud messages from
	 * collecting items to be the same i'd assume? This is easy to change
	 * however, so it's not that important.
	 */
	private static final DrawRelativeToViewport relativeDraw = DrawRelativeToViewport.CENTER;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	public CollectableSystem(EntityManager entityManager, HUDSystem hudSystem) {
		super(entityManager);
		this.hudSystem = hudSystem;
	}

	// ----------------------------------------------------------------
	// Entity System implementations
	// ----------------------------------------------------------------
	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Collectable.class, Collide.class, NameTag.class);
	}

	/**
	 * Process the entities that are registered to our system
	 */
	@Override
	public void processEntity(Entity collectableEntity) {
		Collide collideComponent = collectableEntity.getComponent(Collide.class);

		// Poll the list of entities it collided with, i'll want to rewrite
		// this to be observer based so it will only call this logic when a
		// collectable has actually been collided with, instead of every game
		// loop update..
		for (Entity collidedEntity : collideComponent.collisionList) {
			Children childComponent = collidedEntity.getComponent(Children.class);
			// Check if the entity can equip the collectable, and if it is
			// remove the collectable component from this entity, and pass
			// the entity to the entity's child list that it collided with
			if (childComponent != null) {
				// If the collectable was equipment, reset the spatial's x,y to
				// the desired x,y of the equipment so that when added to the
				// parent it will be dealt with accordingly by the system
				Equipment equipment = collectableEntity.getComponent(Equipment.class);
				if (equipment != null) {
					Spatial spatial = collectableEntity.getComponent(Spatial.class);
					spatial.x = equipment.x;
					spatial.y = equipment.y;
				}

				childComponent.addChildEntity(collectableEntity);

				// Remove the collectable component from the entity
				collectableEntity.removeComponent(Collectable.class);

				// If this entity currently has the camera on it, show the hud
				// message that they collected a the entity
				if (collectableEntity.getComponent(Camera.class) != null) {
					addHudMessage(Helpers.concat("collected item :: ", collectableEntity.getComponent(NameTag.class).name));
					refreshList();
				}

				// Break the loop as we don't want multiple entities to be given
				// the same collectable...
				break;
			}
		}
	}
	/**
	 * Adds a timed message of 2 seconds to the center of the draw screen. It's
	 * not really that important to put the magic number and draw location as
	 * fields I don't believe...
	 * 
	 * @param message
	 *            The message that needs to be shown for 2 seconds
	 */
	protected void addHudMessage(String message) {
		hudSystem.addTimedMessage(message, messageLength, relativeDraw);
	}
}