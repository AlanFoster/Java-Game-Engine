package engine.misc.managers;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import engine.assets.GameGraphic;
import engine.assets.GameSound;
import engine.assets.factories.BufferedImageFactory;
import engine.assets.factories.GameFontFactory;
import engine.assets.factories.GameGraphicFactory;
import engine.assets.factories.GameGraphicStripFactory;
import engine.assets.factories.SlicedBufferedImageFactory;
import engine.assets.factories.GameSoundFactory;
import engine.gui.GameFont;
import engine.interfaces.IAssetFactory;
import engine.misc.GameLogging;

/**
 * This class takes care of loading and storing asset INFORMATION from xml
 * files. I mention information as this class does not handle any sort of
 * loading of actual assets, instead this is class only acts as an
 * 'intermediate' way of giving the loaded asset information loaded from xml to
 * the registered factories. For instance our XML will contain our file location
 * and asset name. When we are assets for asset type of Foo.class and asset name
 * of Foo, we will simply give the file location and any additional details
 * retrieved from the XML to the factory which has registered with the. This
 * factory will then return the asset that we were looking for.
 * <p>
 * All factories must implement our interface {@link IAssetFactory} and can
 * register themselves with with a particular asset type with registerFactory().
 * All of the factories offered by the game engine will cache assets which have
 * been previously loaded when calling the function getObject(). If instead you
 * want a deep clone of an asset you should call the method getClonedObject()
 * <p>
 * All XML asset details must contain an asset name (which must be unique) and a
 * filelocation, otherwise logging errors will be produced. If you wish to store
 * any asset information within this manager you should use the method
 * loadAssetDetailsFromXml. Any additional information required will depend on
 * the factory which produces the object type required, for instance GameFonts
 * {@link GameFontFactory}
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class GameAssetManager {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameAssetManager.class);

	/**
	 * String is the identifying name Key will contain something like
	 * 
	 * <pre>
	 * :: fileLocation
	 * :: A list of extra parameters, used by the instance that
	 * takes it. For instance, if there's font data it'll have something like
	 * {assetName : 'example', parameters : {location : foo.png, availableChars : a-z}}
	 * </pre>
	 */
	private Map<String, Map<String, String>> data;

	/**
	 * Stores the map of Class to Factories. In which Class is the type of
	 * object asset that we'd like returned, and the value is the factory which
	 * has registered with the GameAssetManager to take care of this object
	 * creation
	 * <p>
	 * For instance {BufferedImage.class, BufferedImageFactory.class}
	 */
	private HashMap<Class<?>, IAssetFactory<?>> factoryList;

	// ----------------------------------------------------------------
	// Constructor and singleton
	// ----------------------------------------------------------------
	/**
	 * Creates a new instance of the GameAssetManager. This has been made
	 * private so that it can not be instantiated directly, only through lazy
	 * instantiation when accessing the method getInstance()
	 */
	private GameAssetManager() {
		data = new HashMap<String, Map<String, String>>();
		factoryList = new HashMap<Class<?>, IAssetFactory<?>>();

		// Register default factories offered by the GameAssetManager
		createGraphicFactories();
		createSoundFactories();
		createFontFactories();
	}

	/**
	 * stores the instance, only when accessed will it be be instantiated
	 */
	private static class GameAssetManagerInstance {
		private static GameAssetManager instance = new GameAssetManager();
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------

	/**
	 * Gets the singleton instance of the GameAssetManager. This is lazily
	 * instantiated through the use of the private static class
	 * GameAssetManagerInstance
	 * 
	 * @return GameAssetManager's instance
	 */
	public static GameAssetManager getInstance() {
		return GameAssetManagerInstance.instance;
	}
	/**
	 * This method loads in an XML file and stores the information internally
	 * within an map. It should be noted that at this point no objects are
	 * actually created, the information of the asset name and the file location
	 * are only stored for instance.
	 * 
	 * @param xmlFilePaths
	 *            One or more xml file paths can be given to this method if
	 *            required, as it makes use of varidic args. For instance
	 *            "/images/foo.png"
	 */
	public final void loadAssetDetailsFromXml(String... xmlFilePaths) {
		XStream xstream = new XStream(new DomDriver());
		for (String xmlFilePath : xmlFilePaths) {
			String xmlContents = FileManager.getFileContents(xmlFilePath);
			// Cast the xml contents to the same as our data field
			Map<String, Map<String, String>> parsedData = (Map<String, Map<String, String>>) xstream.fromXML(xmlContents);

			// Iterate through the new data and ensure there are not duplicate
			// asset names. If there are, give an error and don't add the asset.
			// Otherwise add the asset details as expected
			for (String newAssetKey : parsedData.keySet()) {
				// If we have an asset with the same name, throw an exception
				if (data.containsKey(newAssetKey)) {
					logger.error(new IllegalArgumentException(), "Duplicate key value for assetname '",
							newAssetKey, "' asset NOT added. Original value kept");
				} else if (!parsedData.get(newAssetKey).containsKey("fileLocation")) {
					// If there was no file location specified within the asset,
					// give an exception
					logger.error(new NullPointerException(),
							"Loaded XML did not have expected fileLocation field! asset name :: ", newAssetKey);
				} else {
					// If the data has both a unique asset name and a file
					// location add it to our hashmap of data
					data.put(newAssetKey, parsedData.get(newAssetKey));
				}
			}
		}
	}

	/**
	 * Register some of the basic graphical related factories with the
	 * GameAssetManager
	 */
	private final void createGraphicFactories() {
		// Creates and stores BufferedImages
		registerFactory(BufferedImage.class, new BufferedImageFactory());
		// GameGraphic factory
		registerFactory(GameGraphic.class, new GameGraphicFactory());

		registerFactory(GameGraphic[].class, new GameGraphicStripFactory());

		// Used for loading image strips (see the class header for more
		// information on this)
		registerFactory(BufferedImage[].class, new SlicedBufferedImageFactory());
	}

	/**
	 * Register the basic sound related factories with the GameAssetManager
	 */
	private final void createSoundFactories() {
		GameSoundFactory soundFactory = new GameSoundFactory();
		registerFactory(GameSound.class, soundFactory);
	}

	/**
	 * Register the basic Font related factories with the GameAssetManager
	 */
	private final void createFontFactories() {
		GameFontFactory fontFactory = new GameFontFactory();
		registerFactory(GameFont.class, fontFactory);
	}

	/**
	 * Registers a new factory with the GameAssetManager
	 * <p>
	 * <b>Note, there is no attempt to stop a factory replacing an existing
	 * factory. For instance it's perfectly valid to register two factories with
	 * the same class type. This will allow you to replace the existing
	 * factories as required. It's expected that this won't be a problem, but
	 * adding restrictions will be a simple modification.
	 * 
	 * @param clazz
	 *            The class name that this factory creates objects for
	 * @param factory
	 *            The factory which will accomodate the object.
	 */
	public final void registerFactory(Class<?> clazz, IAssetFactory<?> factory) {
		factoryList.put(clazz, factory);
	}

	/**
	 * Get an asset of type T. The asset details should be populated using XML
	 * using the loadAssetsFromXML method.
	 * <p>
	 * <b>This method will return an existing cached object if there is already
	 * one present.</b>
	 * 
	 * @param clazz
	 *            The class of the object which is expected to be returned.
	 *            There should be a factory registered to this class name
	 * @param name
	 *            The assetname which was loaded as XML
	 * @return If the asset name existed, and there was a registered factory, an
	 *         object of type T will be returned.
	 *         <p>
	 *         If either of those conditions were not met, null will be
	 *         returned. Note, logging errors will be outputted by the method
	 *         isValidAssetType() if either of these preconditions are not met.
	 */
	public final <T> T getObject(Class<T> clazz, String name) {
		// If we have a valid factory for this clazz and valid asset name
		if (isValidAssetType(clazz, name)) {
			return clazz.cast(factoryList.get(clazz).getObject(name, data.get(name)));
		}

		return null;
	}

	/**
	 * Get an asset of type T. The asset details should be populated using XML
	 * using the loadAssetsFromXML method.
	 * <p>
	 * <b>This method method is expected to return a new deep cloned object.</b>
	 * 
	 * @param clazz
	 *            The class of the object which is expected to be returned.
	 *            There should be a factory registered to this class name
	 * @param name
	 *            The assetname which was loaded as XML
	 * @return If the asset name existed, and there was a registered factory, an
	 *         object of type T will be returned.
	 *         <p>
	 *         If either of those conditions were not met, null will be
	 *         returned. Note, logging errors will be outputted by the method
	 *         isValidAssetType() if either of these preconditions are not met.
	 */
	public final <T> T getClonedObject(Class<T> clazz, String name) {
		// If we have a valid factory for this clazz and valid asset name
		if (isValidAssetType(clazz, name)) {
			return clazz.cast(factoryList.get(clazz).getClonedObject(name, data.get(name)));
		}
		return null;
	}

	/**
	 * Returns whether or not an asset can be loaded by this system. Logging
	 * errors will be outputted if it is not valid. The current requirements for
	 * being a valid asset type are :: There is a factory registered with the
	 * type of class object, and the asset name exists within the loaded XML
	 * data.
	 * 
	 * @param clazz
	 *            The object type expected to be returned
	 * @param name
	 *            The assetname which was loaded as XML
	 * @return true if there is an asset name which matches the parameter name,
	 *         and if there is a registered factory for the class type.
	 */
	public final boolean isValidAssetType(Class<?> clazz, String name) {
		boolean isValid = true;

		// Check we have a matching factory for this class
		if (!factoryList.containsKey(clazz)) {
			logger.error(new NullPointerException(), "Factory for type ", clazz,
					" not registered with the asset manager!");
			isValid = false;
		}

		// Check the asset name exists
		if (!data.containsKey(name)) {
			logger.error(new NullPointerException(),
					"The asset name ", name, " did not exist!");
			isValid = false;
		}

		return isValid;
	}
}
