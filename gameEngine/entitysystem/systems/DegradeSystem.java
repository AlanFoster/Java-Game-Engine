package entitysystem.systems;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import src.TankSurvival;

import world.World;
import engine.misc.GameLogging;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.ProcessEntitySystem;
import entitysystems.components.Collide;
import entitysystems.components.Damage;
import entitysystems.components.Degrade;
import entitysystems.components.DeleteComponent;
import entitysystems.components.Draw;
import entitysystems.components.NameTag;
import entitysystems.components.Spatial;

/**
 * This system handles all entities which can 'degrade' when being hit with
 * another entity which has the collide component and damage component.
 * <p>
 * When procesing each entity it checks the entities that it has collided
 * against and if the entity that it has collided with has a damage component it
 * will calculate using pixel perfect collision detection where exactly two
 * pixels overlap which are both non-zero alpha.
 * <p>
 * If a location has been found on both the entity, and the entity that it has
 * collided with, in which a non-alpha spot exists, it will draw over processed
 * entity with a 'transparent' colour, simulating the effect that it has been
 * destroyed. The radius of this will be the collided with entity's width. If no
 * spot has been found, nothing will happen to the entity that it collided with,
 * however if it is a bullet then we will add the {@link DeleteComponent} to it,
 * so that the {@link DeleteSystem} can delete it when required, in case there
 * are any other systems which need to handle the object it collided with. *
 * <P>
 * Note1:: this 'degrading' is n-way, so it can be destroyed from any angle.
 * Note2:: If things which it have collided with are moving extremely fast, IE,
 * similar to the tunneling effect, this degrading effect will not be
 * 'accurate'. IE, if during two game loops an entity intersects through it in
 * the middle, once, it will only show that one intersection place, and not a
 * destroyed line through the entire entity.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see Degrade
 */
public class DegradeSystem extends ProcessEntitySystem {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(TankSurvival.class);

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	public DegradeSystem(EntityManager entityManager) {
		super(entityManager);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * It is through this method that you should consider adding your
	 * "getEntitiesContaining" method calls. This method will be called when an
	 * entity or new component has been added to the EntityManager. Obviously
	 * this will provide some overhead initially, but it is will be less
	 * overhead than calling the same method during every logicUpdate method
	 * call, so it is advised that this logic be placed into this method.
	 */
	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(Draw.class, Degrade.class, Collide.class);
	}

	/**
	 * Processes an entity which has the Draw, Degrade and Collide component
	 * added to them. *
	 * <p>
	 * When procesing each entity it checks the entities that it has collided
	 * against and if the entity that it has collided with has a damage
	 * component it will calculate using pixel perfect collision detection where
	 * exactly two pixels overlap which are both non-zero alpha.
	 * <p>
	 * If a location has been found on both the entity, and the entity that it
	 * has collided with, in which a non-alpha spot exists, it will draw over
	 * processed entity with a 'transparent' colour, simulating the effect that
	 * it has been destroyed. The radius of this will be the collided with
	 * entity's width. If no spot has been found, nothing will happen to the
	 * entity that it collided with, however if it is a bullet then we will add
	 * the {@link DeleteComponent} to it, so that the {@link DeleteSystem} can
	 * delete it when required, in case there are any other systems which need
	 * to handle the object it collided with.
	 * <P>
	 * Note1:: this 'degrading' is n-way, so it can be destroyed from any angle.
	 * Note2:: If things which it have collided with are moving extremely fast,
	 * IE, similar to the tunneling effect, this degrading effect will not be
	 * 'accurate'. IE, if during two game loops an entity intersects through it
	 * in the middle, once, it will only show that one intersection place, and
	 * not a destroyed line through the entire entity.
	 */
	@Override
	public void processEntity(Entity entity) {
		Collide entityCollideComponent = entity.getComponent(Collide.class);
		// Iterate over any enemy that it has collided with
		for (Entity collidedWith : entityCollideComponent.collisionList) {
			Damage collidedWithDamageComponent = collidedWith.getComponent(Damage.class);
			// If the eneity we've collided with is not ourself, and it has
			// the damage component
			if (collidedWithDamageComponent != null && collidedWith != entity) {
				Spatial collidedWithSpatial = collidedWith.getComponent(Spatial.class);
				Spatial entitySpatial = entity.getComponent(Spatial.class);

				// Calculate the areas in which the two images overlap so we
				// can reduce the total amount of pixels we test for 'pixel
				// perfect collision testing'
				int left = (int) (Math.max(collidedWithSpatial.x, entitySpatial.x));
				int right = (int) (Math.min(collidedWithSpatial.getRight(), entitySpatial.getRight()));
				int top = (int) (Math.max(collidedWithSpatial.y, entitySpatial.y));
				int bottom = (int) (Math.min(collidedWithSpatial.getBottom(), entitySpatial.getBottom()));

				// Get the buffered images from both the currently processed
				// entity and the entity we collided with
				BufferedImage entityImage = entity.getComponent(Draw.class).getImage();
				BufferedImage collidedWithImage = collidedWith.getComponent(Draw.class).getImage();

				// Create variables with a scope equal to that of our
				// drawing logic. If collisionDetected is set to true, it
				// means that two pixels were found to overlap within both
				// images which were non 0 alpha
				boolean collisionDetected = false;
				int destroyLocationX = 0, destroyLocationY = 0;

				// Add a label to the outter loop so that if we find the
				// pixel location we are looking for we can break out of the
				// outter loop
				outterLoop: for (int x = left; x < right; x++) {
					for (int y = top; y < bottom; y++) {
						// try/catch incase my maths is off, and I want to
						// get the logging info. I doubt this will get
						// thrown, but there's always something which could
						// slip by.
						try {
							// Get the relative X,Y ARGB pixel information,
							// and bitmask the alpha bit only.
							int alphaOne = entityImage.getRGB((int) (x - entitySpatial.x), (int) (y - entitySpatial.y)) & 0xFF << 24;
							int alphaTwo = collidedWithImage.getRGB((int) (x - collidedWithSpatial.x), (int) (y - collidedWithSpatial.y)) & 0xFF << 24;

							// If the two pixels are non alpha, we've found
							// our match!
							if (alphaOne != 0 && alphaTwo != 0) {
								// Set the location of our match, taking
								// away the collided with spatial's half
								// width
								destroyLocationX = (int) (x - entitySpatial.x - collidedWithSpatial.width / 2);
								destroyLocationY = (int) (y - entitySpatial.y - collidedWithSpatial.height / 2);
								collisionDetected = true;
								break outterLoop;
							}
						} catch (Exception e) {
							logger.error("Died within pixel perfect collision ", (x - entitySpatial.x), " ", (int) (y - entitySpatial.y));
						}
					}
				}

				// If two pixels were found to intersect which were
				// non-alpha, we will draw over that location with a
				// transparent colour using the fillOval method on our
				// graphic.
				if (collisionDetected) {
					Graphics2D graphicObject = (Graphics2D) entityImage.getGraphics();

					graphicObject.setColor(new Color(0xFF, true));
					graphicObject.setComposite(AlphaComposite.Src);
					graphicObject.fillOval(destroyLocationX, destroyLocationY,
							(int) collidedWithSpatial.width, (int) collidedWithSpatial.width);
					graphicObject.dispose();

					// Somewhat specific logic here, this would be replaced
					// with a component which represents whether or not an
					// entity system can directly move an entity when this
					// sort of logic occurs
					if (collidedWith.getComponent(NameTag.class).name.equals("bullet")) {
						collidedWith.addComponentNoRefresh(new DeleteComponent());
					}
				}
			}
		}
	}
}