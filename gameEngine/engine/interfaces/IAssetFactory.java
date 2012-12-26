package engine.interfaces;

import java.util.Map;

import engine.misc.managers.GameAssetManager;

/**
 * All factories that wish to be registered with the {@link GameAssetManager}
 * must implement this interface. This interface outlines the expected methods
 * of getObject and getClonedObject.
 * <p>
 * Get Object will return a previously loaded asset if it already exists, and
 * getClonedObject should return a deep clone of an asset.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @param <T>
 *            The generic type of asset that this factory produces. For instance
 *            <GameGraphic>
 */
public interface IAssetFactory<T> {
	/**
	 * This method is expected to return an instance of the desired type. When
	 * this object is created for the first time it should ideally save the
	 * object's reference to a cache list. This will save a lot of resources, as
	 * it means that if you request a graphic twice, the first time it will
	 * cache and return the new object, and the next time it will return the
	 * reference to the first created object.
	 * 
	 * @param name
	 *            The name which will be given to the created asset name (for
	 *            the purposes of {@link INameable}
	 * @param data
	 *            The raw map of data. For instance this will contain our
	 *            fileLocation as a minimum and any additional information which
	 *            would be required, such as possible characters for GameFont
	 *            for instance
	 * @return an Object of type T which the asset (IE, we will not have a need
	 *         to typecast to our desired class type when returned). This object
	 *         will be a cached object.
	 * @see createNewObject
	 */
	T getObject(String name, Map<String, String> data);

	/**
	 * Returns a new deep clone of an asset. For instance if we wanted to have
	 * an image that we plan to modify, we wouldn't use the method getObject
	 * because it will return a reference to the existing image, so any
	 * modifications will be apparent accross all objects that make use of the
	 * image asset.
	 * <p>
	 * Instead this returns a completely new object which can be modified freely
	 * and will not be an existing cached object.
	 * 
	 * @param name
	 *            The name which will be given to the created asset name (for
	 *            the purposes of {@link INameable}
	 * @param data
	 *            The raw map of data. For instance this will contain our
	 *            fileLocation as a minimum and any additional information which
	 *            would be required, such as possible characters for GameFont
	 *            for instance
	 * @return an Object of type T which the asset (IE, we will not have a need
	 *         to typecast to our desired class type when returned). This object
	 *         will be a cached object.
	 * @return
	 */
	T getClonedObject(String name, Map<String, String> data);
}