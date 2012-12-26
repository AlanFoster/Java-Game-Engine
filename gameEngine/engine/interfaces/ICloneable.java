package engine.interfaces;

/**
 * A class that implements this class should provide their logic for creating
 * and returning a clone of that same type (Type T).
 * <p>
 * Unlike Java's java.lang.Cloneable this method does not return an Object,
 * instead it will turn the object to the type that we require without an
 * explicit typecast.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @param <T>
 *            The generic type of the clone which will be returned
 */
public interface ICloneable<T> {
	/**
	 * Return a deep clone of the object that implements this class. This object
	 * will be of type T instead of being an object, so there will not be a need
	 * for an explicit type cast to the same object type.
	 * <p>
	 * Any shareable resources that an object has will be reused by this object
	 * 
	 * @return A new shallow clone of the object which will share any reusable
	 *         resources
	 */
	T getShallowClone();
}
