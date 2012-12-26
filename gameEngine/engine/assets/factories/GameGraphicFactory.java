package engine.assets.factories;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import engine.assets.GameGraphic;
import engine.interfaces.IAssetFactory;
import engine.misc.GameLogging;
import engine.misc.managers.GameAssetManager;

public class GameGraphicFactory extends AssetFactory<GameGraphic> {
	private final static GameLogging logger = new GameLogging(GameGraphicFactory.class);

	/**
	 * This method is expected to return an instance of the desired type. When
	 * this object is created for the first time it should ideally save the
	 * object's reference to a cache list. This will save a lot of resources, as
	 * it means that if you request a graphic twice, the first time it will
	 * cache and return the new object, and the next time it will return the
	 * reference to the first created object.
	 */
	@Override
	public GameGraphic getObject(String name, Map<String, String> data) {
		// If a cache of this object already exists, return it
		if (cache.containsKey(name)) {
			return cache.get(name);
		}

		// Create a the new object and cache it We make a call to the asset
		// manager to get the buffered image we need. It is a good idea to do
		// this as it allows for the BufferedImage factory to cache the image,
		// potentially for a <b>different</b> factory. For instance a
		// circumstance may arise when two factories make use of the same
		// buffered image, so it's best to cache it at this point
		BufferedImage rawGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, name);
		GameGraphic gameGraphic = new GameGraphic(name, rawGraphic);
		cache.put(name, gameGraphic); // cache it

		return gameGraphic;
	}

	/**
	 * Returns a new deep clone of an asset. For instance if we wanted to have
	 * an image that we plan to modify, we wouldn't use the method getObject
	 * because it will return a reference to the existing image, so any
	 * modifications will be apparent accross all objects that make use of the
	 * image asset.
	 * <p>
	 * Instead this returns a completely new object which can be modified freely
	 * and will not be an existing cached object.
	 */
	@Override
	public GameGraphic getClonedObject(String name, Map<String, String> data) {
		// Attempt to get an existing graphic that we own. This method will also
		// handle the possibility that we may not have an existing object, in
		// which case it will grab the buffered image from the factory (it will
		// cache it for us too), then we'll cache the game graphic created too
		// within THIS factory. All in all, a nice system i'd say.
		GameGraphic existingGraphic = getObject(name, data);

		return existingGraphic.getShallowClone();
	}
}
