package engine.assets;

import engine.interfaces.IAsset;

/**
 * This is the base class that all assets should extend from. This class should
 * not be instantiated directly (as it's an abstract class).
 * 
 * @author Alan Foster
 * @version 1.0
 */
public abstract class Asset<T> implements IAsset<T> {
	/**
	 * The stored name of this Asset. As it is required that all game related
	 * objects have a name. This field is used as the requirement for INameable
	 */
	protected String name;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new {@link Asset} instance
	 * 
	 * @param name
	 *            The identifier for this game object within the game engine
	 */
	public Asset(String name) {
		this.name = name;
	}

	// ----------------------------------------------------------------
	// INameable implementations
	// ----------------------------------------------------------------
	/**
	 * All assets must have a name when being registered with the system. This
	 * method returns the name as required
	 */
	@Override
	public String getName() {
		return name;
	}
}
