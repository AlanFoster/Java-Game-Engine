package engine.interfaces;

/**
 * For all objects that wish to be identified with a String name, they should
 * implement this interface. As a requirement to the game engine it is suggested
 * that all objects implement this interface for ease of debugging and retrieval
 * of objects when required
 * 
 * @author Alan Foster
 * @version 1.0
 */
public interface INameable {
	/**
	 * All assets must have a name when being registered with the system. This
	 * method returns the name as required
	 * 
	 * @return The name of this identifiable object
	 */
	String getName();
}
