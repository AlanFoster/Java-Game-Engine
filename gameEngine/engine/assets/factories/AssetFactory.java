package engine.assets.factories;

import java.util.HashMap;
import java.util.Map;

import engine.interfaces.IAssetFactory;
import engine.misc.managers.GameAssetManager;

/**
 * This is the topmost super class for all asset factories. This class can not
 * be instantiated directly as it is abstract. ,p> This abstract class offers a
 * Map of String to <T> in which you should preferably use for caching objects
 * which have been loaded/created by the factory. IE if we load a GameGraphic we
 * should add to our cache the GameGraphic that we can return a reference to if
 * there's another request made for an asset type <T>
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @param <T>
 *            The generic type of the asset, for instance GameGraphic. This is
 *            used for the Cache type, and the return type for the
 *            {@link IAssetFactory} parameter
 * 
 * @see GameAssetManager
 * @see IAssetFactory
 */
public abstract class AssetFactory<T> implements IAssetFactory<T> {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * Stores the list of cached type <T> which have previously been loaded. The
	 * key is the asset name, and the value is the loaded type <T>. We do this
	 * so that if there is another request for the same asset name we can simply
	 * return a reference to our cached value, we will also make use of this
	 * when there is getClonedObject method call too, as copying a resource is
	 * less resourceful than completely reloading it with IO again.
	 * <p>
	 * It is expected that all factories which extends this AssetFactory class
	 * make use of this caching system.
	 */
	Map<String, T> cache;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new AssetFactory with a default cached Map of String, <T>. The
	 * String will be the asset name and the T will be the cached object that
	 * the extending factory class is supposed to maintain. It is expected that
	 * all extending factories will make full use of this for speed performance.
	 */
	AssetFactory() {
		cache = new HashMap<String, T>();
	}
}
