package engine.assets.factories;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import engine.assets.GameGraphic;
import engine.gui.GameFont;
import engine.interfaces.IAssetFactory;
import engine.main.GameEngine;
import engine.misc.GameLogging;
import engine.misc.managers.GameAssetManager;

/**
 * This class manages and deals with the creation of GameFonts. This class
 * interacts with the BufferedImage
 * <p>
 * Get Object will return a previously loaded asset if it already exists, and
 * getClonedObject should return a deep clone of an asset.
 * <p>
 * This factory is automatically registered to the {@link GameAssetManager}
 * directly through the method createFontFactories();
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GameFontFactory extends AssetFactory<GameFont> {
	private final static GameLogging logger = new GameLogging(GameFontFactory.class);

	/**
	 * A list of the required fields needed for this font to be considered
	 * 'valid'. If none of these fields are found within the "data" map
	 * (populated by the XML, and given to GameFontFactory by the
	 * GameAssetManager), then a logger exception will be shown. When a font is
	 * not considered valid, the factory will return null. This is checked by
	 * the method isValid.
	 */
	private static final String[] REQUIRED_XML_FIELDS = { "fileLocation", "availableChars" };

	@Override
	public GameFont getObject(String name, Map<String, String> data) {
		// Check our cache first if we have already created the object. As
		// always.
		if (cache.containsKey(name)) {
			return cache.get(name);
		}

		// Check if the font is valid, IE contains all of our
		// REQUIRED_XML_FIELDS
		if (!isValidFont(name, data)) {
			return null;
		}

		// Ask the asset manager to slice our asset image for us
		BufferedImage[] charImages = GameAssetManager.getInstance().getObject(BufferedImage[].class, name);
		// The XML stores data on many new lines, so we must use regex to strip
		// white space to get one long string, so that our indexOf(char) is
		// reliable
		String availableChars = data.get("availableChars").replaceAll("\\s+", "");

		// Test expliclitly at this point whether or not the amount of
		// charGraphics equals that of the amount of available chars. Stops any
		// potential out of bound problems etc
		if (charImages.length != availableChars.length()) {
			logger.error(new Exception(), "font ", name, " was invalid! \n",
					"Total chars required : ", availableChars.length(), " :: ", availableChars, "\n",
					"Total graphic chars found : ", charImages.length);
		}

		// Create the new GameFont with all of our loaded information
		GameFont gameFont = new GameFont(name, charImages, availableChars);
		// Apply any additional non-core data that the XML may or may not have
		// to the font
		setAdditionalFields(gameFont, data);

		// Add the new gameFont to the cache system
		cache.put(name, gameFont);

		return gameFont;
	}

	@Override
	public GameFont getClonedObject(String name, Map<String, String> data) {
		// Get an existing GameFont, remember this can possibly return null if
		// it wasn't a validFont.
		GameFont cachedFont = getObject(name, data);
		if (cachedFont == null) {
			return cachedFont;
		}

		return cachedFont.getShallowClone();
	}
	/**
	 * Apply any additional non-core data that the XML may or may not have
	 * 
	 * @param gameFont
	 *            The GameFont object that we will modify with the additional
	 *            information
	 * @param data
	 *            The raw XML laoded in fro the XML file sent with the asset
	 */
	public void setAdditionalFields(GameFont gameFont, Map<String, String> data) {
		if (data.containsKey("charSpacing")) {
			gameFont.setCharSpacing(Integer.parseInt(data.get("charSpacing")));
		}

		if (data.containsKey("verticalSpacing")) {
			gameFont.setVerticalSpacing(Integer.parseInt(data.get("verticalSpacing")));
		}
	}

	/**
	 * Checks if an asset's data map (Which is loaded in xml) contains all of
	 * the required fields (a field of this GameFontFactory class). Returns
	 * whether or not a font is valid, based on the loaded XML map data only
	 * 
	 * @param name
	 *            The asset name, only used for the logging error when the font
	 *            was not valid
	 * @param data
	 *            The raw map of data which was given to us from the
	 *            {@link GameAssetManager} when we requested a game font
	 * @return True if the asset's data map contains all of the required fields,
	 *         false if any field was missing
	 */
	private boolean isValidFont(String name, Map<String, String> data) {
		StringBuilder errorList = new StringBuilder();

		// Iterate through the required fields and make sure we have the field
		// we need! Otherwise append to the string builder, at this point we
		// don't kill it and throw an exception. I believe it's more 'user
		// friendly' this way, as it allows the developer to know everything
		// that's went wrong at once.
		for (String requiredField : REQUIRED_XML_FIELDS) {
			if (!(data.containsKey(requiredField))) {
				errorList.append("No field found for '").append(requiredField).append("'\n");
			}
		}

		// If we received any errors whilst validating, output all of the found
		// errors and return false
		if (errorList.length() > 0) {
			logger.error(new Exception(), "Failed at loading font for asset name ", name, "\n", errorList.toString());
			return false;
		}
		return true;
	}
}
