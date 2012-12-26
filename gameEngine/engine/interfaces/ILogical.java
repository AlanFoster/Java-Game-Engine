package engine.interfaces;

import engine.main.GameLayer;
import engine.main.GameTime;
import entitysystem.core.EntitySystem;

/**
 * This method should be implemented when it will require logic updates from the
 * logical update cycle. This interface is a requirement of many of the systems
 * offered by the GameEngine, for instance the entity system and {@link GameLayer}
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameLayer
 * @see EntitySystem
 * */
public interface ILogical {
	/**
	 * <p>
	 * This method will be called when an object is first coming into
	 * realization.
	 * </p>
	 * <p>
	 * Examples of when this will be called are :
	 * <ul>
	 * <li>GameLayer startup - IE, when added to GameScreen, or when GameScreen
	 * is changed</li>
	 * <li>First time being added to a game layer</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Examples of what to do during this method call include loading the
	 * required content from asset managers, or recording the starting game time
	 * for animation perhaps. Or restarting locations back to their default
	 * place.
	 * </p>
	 * 
	 * @param gameTime
	 *            The GameTime object that will store the current elapsed time
	 *            in milliseconds/nano seconds etc.
	 */
	void startUp(GameTime gameTime);

	/**
	 * This method will be called during the update cycle of the game loop.
	 * During this time it is expected that objects update their position if
	 * required, change states etc.
	 * 
	 * @param gameTime
	 *            The GameTime object that will store the current elapsed time
	 *            in milliseconds/nano seconds etc.
	 */
	void logicUpdate(GameTime gameTime);

	/**
	 * <p>
	 * This method will be called when an object is about to be removed from its
	 * parent
	 * </p>
	 * Examples of when this will be called are :
	 * <ul>
	 * <li>GameLayer CleanUp - IE, when the gameScreen is being changed to
	 * another</li>
	 * <li>When the object is being removed from the parent</li>
	 * </ul>
	 * </p>
	 * <p>
	 * An example of what to do during this method call may be to set objects
	 * fields to null to allow for garbage collection, and removing from any
	 * event handling systems that the object may have registered for.
	 * </p>
	 */
	void cleanUp();
}
