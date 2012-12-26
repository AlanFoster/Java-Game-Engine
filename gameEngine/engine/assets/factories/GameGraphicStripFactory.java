package engine.assets.factories;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import engine.assets.GameGraphic;
import engine.interfaces.IAssetFactory;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;

public class GameGraphicStripFactory extends AssetFactory<GameGraphic[]> {
	private final static GameLogging logger = new GameLogging(GameGraphicStripFactory.class);

	/**
	 * Store a reference to the GameAssetManager, we do this as we will make use
	 * of any of the other factories caching for loading things which will make
	 * things faster and less memory demanding.
	 */

	@Override
	public GameGraphic[] getObject(String name, Map<String, String> data) {
		// If a cache of this object already exists, return it
		if (cache.containsKey(name)) {
			return cache.get(name);
		}

		/*
		 * Make a call to our asset manager to get a 2d array of BufferedImage.
		 * IE a sliced image.
		 */
		BufferedImage[] slicedImages = GameAssetManager.getInstance().getObject(BufferedImage[].class, name);
		GameGraphic[] gameGraphicArrayStrip = convertToGameGraphicArray(name, slicedImages);
		cache.put(name, gameGraphicArrayStrip); // cache it too!
		return gameGraphicArrayStrip;
	}
	@Override
	public GameGraphic[] getClonedObject(String name, Map<String, String> data) {
		// Get an object if it already exists so that we can clone it
		GameGraphic[] oldGraphicArray = getObject(name, data);
		// create a new array of GameGraphic so that we can begin creating new
		GameGraphic[] newGraphicArray = new GameGraphic[oldGraphicArray.length];
		for (int i = 0; i < oldGraphicArray.length; i++) {
			newGraphicArray[i] = oldGraphicArray[i++].getShallowClone();
		}

		return newGraphicArray;
	}

	private GameGraphic[] convertToGameGraphicArray(String name, BufferedImage[] bufferedImages) {
		GameGraphic[] gameGraphicArray = new GameGraphic[bufferedImages.length];
		
		int i = 0; 
		for(BufferedImage bufferedImage : bufferedImages){
			gameGraphicArray[i++] = new GameGraphic(Helpers.concat(name, "#", i), bufferedImage);
		}
		
		return gameGraphicArray;
	}
}
