package world;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.xml.DomDriver;

import engine.main.GameEngine;
import engine.misc.GameLogging;
import engine.misc.managers.FileManager;

/**
 * Creates a new World parser which will load all levels stored within an XML
 * file and populate a hashmap of levels to LevelDetails. The level details will
 * contain the map which will be used within the world, and spawn locations for
 * enemies etc.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see WorldManager
 * @see World
 */
public class WorldParser {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(WorldParser.class);

	/**
	 * A hashmap of all of the level data . The key is the identifying name for
	 * the level, and the value is the level details - which stores the map
	 * asset name etc within it
	 */
	private Map<String, LevelDetails> levelData;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	/**
	 * Creates a new world parser which will populate the levelData field with
	 * all loaded information for a level.
	 * 
	 * @param dataLocation
	 *            The XML file path that stores all of the level details
	 */
	public WorldParser(String dataLocation) {
		// Instantiate the hashmap that stores all of the level names to level
		// details
		levelData = new HashMap<String, LevelDetails>();
		
		loadAllLevelData(dataLocation);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Get the level details from the XML data
	 * 
	 * @param level
	 *            The level name that we want to load
	 * @return The LevelDetails that have been loaded from this level name
	 */
	public LevelDetails getData(String level) {
		// Lazy load the data if required
		if (levelData == null) {
			loadAllLevelData(level);
		}
		if (!levelData.containsKey(level)) {
			logger.error(new NullPointerException(), "Level not found within WorldParser");
		}

		LevelDetails levelDetails = levelData.get(level);
		return levelDetails;
	}

	/**
	 * This method populates the levelData with the loaded XML. Each call to
	 * this method will add more information to the levelData hashmap that this
	 * stores.
	 * 
	 * @param levelDetails
	 *            The XML location that we want to load many levels from
	 */
	private void loadAllLevelData(String levelDetails) {
		String xmlContents = FileManager.getFileContents(levelDetails);

		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(WorldParser.class);
		xstream.alias("levelCollection", Map.class);
		xstream.alias("level", Entry.class);
		xstream.alias("levelName", String.class);
		Object xmlResult = xstream.fromXML(xmlContents);

		levelData.putAll((HashMap<String, LevelDetails>) xmlResult);
	}

	/**
	 * A public data structure which stores the level details which will be used
	 * within a world, for instance the map asset name and enemylocation asset
	 * name.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	@XStreamAlias("levelDetails")
	public final static class LevelDetails {
		@XStreamAsAttribute
		private String mapAssetName;
		@XStreamAsAttribute
		private String enemyLocationAssetName;

		LevelDetails(String mapAssetName, String enemyLocationAssetName) {
			this.mapAssetName = mapAssetName;
			this.enemyLocationAssetName = enemyLocationAssetName;
		}

		public String getMapAssetName() {
			return mapAssetName;
		}

		public String getEnemyLocationAssetName() {
			return enemyLocationAssetName;
		}
	}
}
