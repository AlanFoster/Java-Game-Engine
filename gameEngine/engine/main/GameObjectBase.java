package engine.main;

import engine.interfaces.ILogical;
import engine.interfaces.INameable;

/**
 * This is the topmost super class that all logical GameEngine objects extend
 * from. This class implements the interface {@link INameable}, which as a
 * requirement to the game engine it is suggested that all objects implement
 * this interface for ease of debugging and retrieval of objects when required
 * 
 * @author Alan Foster
 * @version 1.0
 */
public abstract class GameObjectBase implements ILogical, INameable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The identifier for this game object within the game engine
	 */
	protected String name;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new {@link GameObjectBase} instance
	 * 
	 * @param name
	 *            The identifier for this game object within the game engine
	 */
	protected GameObjectBase(String name) {
		this.name = name;
	}

	/**
	 * <p>
	 * This method will be called when an object is first coming into
	 * realization.During this time it is expected that objects update their
	 * position if required, change states etc.
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
	@Override
	public void startUp(GameTime gameTime) {
	}

	/**
	 * This method will be called during the update cycle of the game loop.
	 * During this time it is expected that objects update their position if
	 * required, change states etc.
	 * 
	 * @param gameTime
	 *            The GameTime object that will store the current elapsed time
	 *            in milliseconds/nano seconds etc.
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
	}

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
	@Override
	public void cleanUp() {
	}

	/**
	 * All assets must have a name when being registered with the system. This method returns the name as required
	 */
	@Override
	public String getName() {
		return name;
	}
}
