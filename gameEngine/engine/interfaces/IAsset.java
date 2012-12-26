package engine.interfaces;

/**
 * This is the interface that all assets are expected to implement. It provides
 * the requirement that all objects should be nameable, and adds the additional
 * requirement of an object needing to provide cloning methods for better memory
 * management.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @param <T>
 *            The generic type of the asset. We require this for the
 *            {@link ICloneable} interface implementation so that returned
 *            cloned types will be of the generic type, and not a useless Object
 */
public interface IAsset<T> extends INameable, ICloneable<T> {
}