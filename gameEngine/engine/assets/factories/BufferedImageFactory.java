package engine.assets.factories;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import src.TankSurvival;

import engine.interfaces.IAssetFactory;
import engine.misc.GameLogging;
import engine.misc.managers.FileManager;

/**
 * This factory loads images through the FileManager helper class. When they
 * have been loaded it will then cache the BufferedImage within the cache list
 * provided by the base class AssetFactory. When calling getObject it will
 * return an image from the cached list. When calling getClonedObject it will
 * return a new image reference based on the existing cached object.
 * <p>
 * It is expected that all other systems request this factory to do any sort of
 * image loading for them, through the GameAssetManager and not directly of
 * course, to allow for even higher levels of caching and reference reuse.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see FileManager
 * @see GameFontFactory
 */
public class BufferedImageFactory extends AssetFactory<BufferedImage> {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(BufferedImageFactory.class);

	/**
	 * This method is expected to return an instance of the desired type. When
	 * this object is created for the first time it should ideally save the
	 * object's reference to a cache list. This will save a lot of resources, as
	 * it means that if you request a graphic twice, the first time it will
	 * cache and return the new object, and the next time it will return the
	 * reference to the first created object. If you wish to have a new object
	 * created then you should use the method createNewObject instead!
	 */
	@Override
	public BufferedImage getObject(String name, Map<String, String> data) {
		// If a cache of this object already exists, return it
		if (cache.containsKey(name)) {
			return cache.get(name);
		}

		// Load the image if there is no cache of it
		BufferedImage loadedImage = FileManager.loadImage(data.get("fileLocation"));
		
		// Put it in our cache
		cache.put(name, loadedImage);
		return loadedImage;
	}
	/**
	 * Returns a new deep clone of an asset. For instance if we wanted to have
	 * an image that we plan to modify, we wouldn't use the method getObject
	 * because it will return a reference to the existing image, so any
	 * modifications will be apparent accross all objects that make use of the
	 * image asset.
	 * 
	 * Instead this returns a completely new object which can be modified freely
	 * and will not be an existing cached object.
	 */
	@Override
	public BufferedImage getClonedObject(String name, Map<String, String> data) {
		// Make a call to get an already loaded buffered image. If there isn't
		// already a buffered image, our getObject method will take care of this
		// logic for us by loading and caching it.
		BufferedImage cachedImage = getObject(name, data);

		// Return a new image reference
		return cachedImage.getSubimage(0, 0, cachedImage.getWidth(), cachedImage.getHeight());
	}
}
