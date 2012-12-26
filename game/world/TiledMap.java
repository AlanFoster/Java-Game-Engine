package world;

import hud.IMiniMapTile;
import hud.MiniMapSystem;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import engine.components.GameGraphicComponent;
import engine.components.GameSizeableComponent;
import engine.interfaces.IDrawableLogical;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.GameSettings;
import engine.misc.GameViewPort;
import engine.misc.Location;
import engine.misc.managers.GameAssetManager;

/**
 * This class represents a tiled world which can be explored. This class makes
 * the assumption that all tiles are of equal width and height.
 * <p>
 * NOTE :: If i have time, i'll want to rewrite this to be populated with XML
 * etc. But I don't think that's important to work on currently, as it's easy to
 * change.
 * <p>
 * Note :: Even though this system caches the drawn tiles and saves tile... It
 * should've been taken a step further. IE, instead of only caching what is
 * currently shown each time the player moves, we should've split the world into
 * larger chunks which get cached once. IE, instead of drawing the many smaller
 * tiles to the screen, we should group them together and still draw many tiles
 * at once, but much bigger sections of tiles at once. This would be considered
 * a nicer system I believe.
 * <p>
 * This class was not finished to the standard that i'd expect it to be, and is
 * not a good demonstration of my programming ability
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see WorldTile
 * @see World
 * @see WorldManager
 */
public class TiledMap extends GameSizeableComponent implements IDrawableLogical {
	// ----------------------------------------------------------------
	// Inner Class :: TileSet
	// ----------------------------------------------------------------
	/**
	 * Stores the details about a TileSet, which a graphical assortment of tiles
	 * which can be used within the world. This tile set will be used when there
	 * is a matching colour associated within the loaded map detail and this
	 * TileSet. See the constructor of TiledMap to see how this is used
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	private static class TileSet {
		// ----------------------------------------------------------------
		// Fields
		// ----------------------------------------------------------------
		/**
		 * All of the tiles which this tile set has
		 */
		private BufferedImage[] tiles;
		/**
		 * Cached tiles. The key is the TileType, IE, MIDDLE_CENTER. and the
		 * value is the WorldTile. This map will only be populated when
		 * getTile() is called, and not all at once.
		 */
		private Map<TileType, WorldTile> tileMap;

		/**
		 * Denotes whether or not a tile is passable, this is used for AI path
		 * finding. true if it is passable, false if it is not.
		 */
		public boolean passable;

		/**
		 * The path of passing through this tile, this is used during the AI
		 * path finding. IE for mud it will be high, but for ground it will be
		 * low.
		 */
		int cost;

		/**
		 * The colour of the tile which will be drawn on the minimap, this is
		 * the 'terrain colour'
		 */
		public int minimapColor;

		// ----------------------------------------------------------------
		// Constructor
		// ----------------------------------------------------------------
		/**
		 * 
		 * @param minimapColor
		 *            The colour of the tile which will be drawn on the minimap,
		 *            this is the 'terrain colour'
		 * @param passable
		 *            Denotes whether or not a tile is passable, this is used
		 *            for AI path finding. true if it is passable, false if it
		 *            is not.
		 * @param cost
		 *            The path of passing through this tile, this is used during
		 *            the AI path finding. IE for mud it will be high, but for
		 *            ground it will be low.
		 * @param tiles
		 *            The sliced images which will be used within this tile set,
		 *            when a tile is created the required tile will be chosen
		 *            through the use of the TileType enum
		 */
		public TileSet(int minimapColor, boolean passable, int cost, BufferedImage[] tiles) {
			this.tiles = tiles;
			this.minimapColor = minimapColor;
			this.cost = cost;
			this.passable = passable;
			tileMap = new HashMap<TileType, WorldTile>();
		}

		/**
		 * 
		 * @param tileType
		 *            The tile type wanted, IE, BOTTOM_RIGHT
		 * @return
		 */
		public WorldTile getTile(TileType tileType) {
			// Try to get the tile, if it's null, create it and keep a reference
			// to it so that it can be returned next time.
			WorldTile foundTile = tileMap.get(tileType);
			if (foundTile == null) {
				foundTile = new WorldTile(tiles[tileType.val], passable, minimapColor, cost);
			}
			return foundTile;
		}
	}

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
	private final static GameLogging logger = new GameLogging(TiledMap.class);

	/**
	 * Stores the tiles that make up this world
	 */
	private WorldTile[][] tiles;

	/**
	 * it didn't make sense that I would redraw every single tile on each draw
	 * cycle... Therefore i cache it. this method also takes care of the culling
	 * required too. As each tile is in a 2d array of x,y tiles, I can work out
	 * the current visible X,Y tiles, and start drawing only them. Removing the
	 * need to iterate over all tiles and checking for visibility.
	 */
	private BufferedImage cachedTileImage;

	/**
	 * The width of each tile within the world
	 */
	private int tileSizeWidth;
	/**
	 * The height of each tile within the world
	 */
	private int tileSizeHeight;

	/**
	 * The total amount of tiles within the world, horizontally. IE tiles.length
	 */
	private int totalMapTilesWidth;
	/**
	 * The total amount of tiles within the world, horizontally. IE
	 * tiles[0].length
	 */
	private int totalMapTilesHeight;

	/**
	 * The total amount of tiles which can be visible at once within the
	 * viewport, in terms of width. This is used to only draw the maximum amount
	 * of tiles which could possibly fit within the viewport
	 */
	private int totalVisibleTilesWidth;
	/**
	 * The total amount of tiles which can be visible at once within the
	 * viewport. in terms of hegiht. This is used to only draw the maximum
	 * amount of tiles which could possibly fit within the viewport
	 */
	private int totalVisibleTilesHeight;

	/**
	 * The current topmost horizontal visible tile within the viewport
	 */
	private int currentVisibleTileX;
	/**
	 * The current topmost vertical visible tile within the viewport
	 */
	private int currentVisibleTileY;

	/**
	 * The X 'offset' between the viewport and the currentVisibleTileX's actual
	 * location
	 */
	private int cameraOffsetFromTileX;
	/**
	 * The Y 'offset' between the viewport and the currentVisibleTileY's actual
	 * location
	 */
	private int cameraOffsetFromTileY;

	/**
	 * Access to the viewport so we can calculate which tiles need drawn to the
	 * screen
	 */
	private GameViewPort viewPort;

	/**
	 * As we cache the drawn map, we keep track of the previous viewport
	 * location. If this differs from the previous game loop's viewport
	 * location, we will recache the map
	 */
	private Location previousViewPortLocation;

	/**
	 * Lookuptable of for tile list
	 * <p>
	 * Key : Colour
	 * <p>
	 * Value : Tile to be used
	 **/
	private Map<Integer, TileSet> tileSets;

	/**
	 * The default world tile that is used when there is no tile set registered
	 * with the colour found within the loaded map detail image.
	 */
	private WorldTile DEFAULT_GROUND_TILE;

	/**
	 * Stores the correlation between the desired tile and the index that it
	 * will be within the loaded array of images. This is used within the
	 * terraforming method to decide which tile of a tile set should be used
	 * <p>
	 * You will have to look at the example grass tiles image to see how this
	 * works.
	 */
	enum TileType {
		UPPER_LEFT(0),
		UPPER_CENTER(1),
		UPPER_RIGHT(2),

		MIDDLE_LEFT(3),
		MIDDLE_CENTER(4),
		MIDDLE_RIGHT(5),

		BOTTOM_LEFT(6),
		BOTTOM_CENTER(7),
		BOTTOM_RIGHT(8),

		CORNER_BOTTOM_LEFT(9),
		CORNER_BOTTOM_RIGHT(10),

		CORNER_TOP_LEFT(11),
		CORNER_TOP_RIGHT(12),
		SINGLE(13);

		int val;

		TileType(int location) {
			this.val = location;
		}
	}

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new tiled map
	 * 
	 * @param viewPort
	 *            Access to the viewport which we use to draw the world
	 *            relatively to the viewport, and perform tile culling when
	 *            drawing
	 */
	public TiledMap(GameViewPort viewPort) {
		super("tiledMap");

		this.viewPort = viewPort;

		tileSets = new HashMap<Integer, TileSet>();

		/**
		 * Grass tile set
		 */
		int grassColor = 0x00FF00;
		BufferedImage[] grassTileImages = GameAssetManager.getInstance().getObject(BufferedImage[].class, "grassTiles");
		TileSet grassTileSet = new TileSet(grassColor, true, 0, grassTileImages);
		tileSets.put(grassColor, grassTileSet);

		// set the default ground tile
		DEFAULT_GROUND_TILE = new WorldTile(GameAssetManager.getInstance().getObject(BufferedImage.class, "tiles_ground"), true, 0x996600, 0);

		tileSizeWidth = DEFAULT_GROUND_TILE.getWidth();
		tileSizeHeight = DEFAULT_GROUND_TILE.getHeight();
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Creates the tiled world based on the map asset name given to it, which we
	 * will retrieve from the {@link GameAssetManager}
	 * 
	 * @param mapName
	 *            The asset name of the world's map image
	 */
	public void loadMap(String mapName) {
		BufferedImage map = GameAssetManager.getInstance().getObject(BufferedImage.class, (mapName));

		tiles = new WorldTile[map.getWidth()][map.getHeight()];
		totalMapTilesWidth = tiles.length;
		totalMapTilesHeight = tiles[0].length;

		// Store the actual map width/height, taking into consideration tile
		// size
		setWidth(totalMapTilesWidth * tileSizeWidth);
		setHeight(totalMapTilesHeight * tileSizeHeight);

		// The total tiles that can be seen by the user, with added 2 tiles
		totalVisibleTilesWidth = (GameSettings.getGameWidth() / tileSizeWidth) + 2;
		totalVisibleTilesHeight = (GameSettings.getGameHeight() / tileSizeHeight) + 2;

		logger.info(totalVisibleTilesWidth, " ", totalVisibleTilesHeight);

		terraformMap(map);

		cachedTileImage = new BufferedImage(GameSettings.getGameWidth(), GameSettings.getGameHeight(), BufferedImage.TYPE_INT_RGB);
		createCachedMap();
	}

	/**
	 * Returns the maximum boundares that the player can travel. This will most
	 * likely be used to test whether or not the viewport is within the
	 * 'desired' range when tracking to objects
	 * 
	 * I've decided to use a hashmap for this representation so that new maximum
	 * boundaries can be added later, without affecting other code
	 * 
	 * @return
	 */
	public HashMap<String, Location> getMaximumBoundarys() {
		HashMap<String, Location> boundaries = new HashMap<String, Location>();

		// This location will apply for the top right of the map

		// store the center point for reference (in tiles)
		int centerTileX = totalVisibleTilesWidth / 2;
		int centerTileY = totalVisibleTilesHeight / 2;

		boundaries.put("topRightBoundary", new Location((totalMapTilesWidth - centerTileX) * tileSizeWidth, centerTileY));

		return boundaries;
	}

	@Override
	public void startUp(GameTime gameTime) {
		previousViewPortLocation = viewPort.getTopLeftLocation();
		updateMapPosition(viewPort.getTopLeftLocation());
	}

	@Override
	public void cleanUp() {
		cachedTileImage = null;
	}

	/**
	 * Most games rely on a text file to populate something like this, but
	 * where's the fun in that? This method takes a buffered image and using the
	 * tileList it calculates what tile it should be using to transition between
	 * tiles of different type. I liked using an image for storing this map
	 * editor, as it allowed for a cheap map editor - as i can open it up in MS
	 * Paint etc easily, and make drastic changes.
	 * <p>
	 * This terraforming works by iterating over each pixel in the loaded map.
	 * the rgb for the pixel is compared against the tile sets that have
	 * registered with this TileMap, and if one is found it will calculate which
	 * TileType is use. If no corresponding tileset is found for the rgb, then
	 * it assumed that it is our 'ground' tile.
	 * 
	 * @param map
	 */
	private void terraformMap(BufferedImage map) {
		boolean leftDifferent = false, rightDifferent = false, topDifferent = false, bottomDifferent = false;
		boolean leftDiagonalDifferent = false, rightDiagonalDifferent = false, bottomLeftDiagonalDifferent = false, bottomRightDiagonalDifferent = false;

		// terraforming / auto tiling
		for (int y = 0; y < totalMapTilesHeight; y++) {
			for (int x = 0; x < totalMapTilesWidth; x++) {
				Integer rgb = map.getRGB(x, y) & 0xFFFFFF;
				if (tileSets.containsKey(rgb)) {

					// decide what tile to use
					leftDifferent = (x == 0 || rgb != (map.getRGB(x - 1, y) & 0xFFFFFF));
					topDifferent = (y == 0 || rgb != (map.getRGB(x, y - 1) & 0xFFFFFF));
					rightDifferent = (x == totalMapTilesWidth - 1 || rgb != (map.getRGB(x + 1, y) & 0xFFFFFF));
					bottomDifferent = (y == totalMapTilesHeight - 1 || rgb != (map.getRGB(x, y + 1) & 0xFFFFFF));

					leftDiagonalDifferent = (x > 0 && y > 0 && rgb != (map.getRGB(x - 1, y - 1) & 0xFFFFFF));
					rightDiagonalDifferent = (x == totalMapTilesWidth - 1 || y == totalMapTilesHeight - 1 || rgb != (map.getRGB(x + 1, y - 1) & 0xFFFFFF));
					bottomLeftDiagonalDifferent = (x == totalMapTilesWidth - 1 || y == totalMapTilesHeight - 1 || rgb != (map.getRGB(x - 1, y + 1) & 0xFFFFFF));
					bottomRightDiagonalDifferent = (x == totalMapTilesWidth - 1 || y == totalMapTilesHeight - 1 || rgb != (map.getRGB(x + 1, y + 1) & 0xFFFFFF));

					// Default the tile cell to the center
					TileType tileTypeName = TileType.MIDDLE_CENTER;

					if (leftDifferent)
						tileTypeName = TileType.UPPER_LEFT;
					if (!leftDifferent && topDifferent)
						tileTypeName = TileType.UPPER_CENTER;
					if (leftDifferent && !topDifferent)
						tileTypeName = TileType.MIDDLE_LEFT;
					if (!leftDifferent && rightDifferent)
						tileTypeName = TileType.UPPER_RIGHT;
					if (!leftDifferent && !topDifferent && rightDifferent)
						tileTypeName = TileType.MIDDLE_RIGHT;
					if (leftDifferent && bottomDifferent)
						tileTypeName = TileType.BOTTOM_LEFT;
					if (!leftDifferent && bottomDifferent)
						tileTypeName = TileType.BOTTOM_CENTER;
					if (!leftDifferent && bottomDifferent && rightDifferent)
						tileTypeName = TileType.BOTTOM_RIGHT;
					if (leftDifferent && topDifferent && rightDifferent && bottomDifferent)
						tileTypeName = TileType.SINGLE;
					if (!leftDifferent && !rightDifferent && !topDifferent && leftDiagonalDifferent)
						tileTypeName = TileType.CORNER_TOP_LEFT;
					if (!topDifferent && !rightDifferent && rightDiagonalDifferent)
						tileTypeName = TileType.CORNER_TOP_RIGHT;
					if (!leftDifferent && !bottomDifferent && !topDifferent && bottomLeftDiagonalDifferent)
						tileTypeName = TileType.CORNER_BOTTOM_LEFT;
					if (!leftDifferent && !bottomDifferent && !topDifferent && !rightDifferent && bottomRightDiagonalDifferent)
						tileTypeName = TileType.CORNER_BOTTOM_RIGHT;

					tiles[x][y] = tileSets.get(rgb).getTile(tileTypeName);
				} else {
					// if it's not in the array of set tiles, then the ground
					// tile
					tiles[x][y] = DEFAULT_GROUND_TILE;
				}
			}
		}
	}

	/**
	 * Access to the tiled world, but return the datatype as minimaptiles so
	 * that the minimap system may use the tiles during the terrain draw of the
	 * minimap. This is done through polymorphism as {@link WorldTile}
	 * implements IMIN
	 * 
	 * @return a 2d tiled world with the implementation of {@link IMiniMapTile}
	 * 
	 * @see MiniMapSystem
	 */
	public IMiniMapTile[][] getMiniMapLocations() {
		return (IMiniMapTile[][]) tiles;
	}

	/**
	 * Within the logic loop we check to see if the viewport has chagned
	 * locations, and if it has we will recache the tiled world
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		if (!viewPort.getTopLeftLocation().equals(previousViewPortLocation)) {
			previousViewPortLocation = viewPort.getTopLeftLocation();
			// The 'previous'viewport location at this point really the current.
			updateMapPosition(previousViewPortLocation);
		}
	}

	/**
	 * When the viewport changes, we update any information based on the
	 * viewport location to redraw the map. When we have calculated ths we
	 * re-cache the map
	 * 
	 * @param location
	 *            The new location that the viewport is at
	 */
	private void updateMapPosition(Location location) {
		int x = previousViewPortLocation.getX();
		int y = previousViewPortLocation.getY();

		currentVisibleTileX = x / tileSizeWidth;
		currentVisibleTileY = y / tileSizeHeight;

		cameraOffsetFromTileX = x % tileSizeWidth;
		cameraOffsetFromTileY = y % tileSizeHeight;

		createCachedMap();
	}

	/**
	 * it didn't make sense that I would redraw every single tile on each draw
	 * cycle... Therefore i cache it. this method also takes care of the culling
	 * required too. As each tile is in a 2d array of x,y tiles, I can work out
	 * the current visible X,Y tiles, and start drawing only them. Removing the
	 * need to iterate over all tiles and checking for visibility.
	 */
	private void createCachedMap() {
		Graphics2D cachedTileImageGraphics = (Graphics2D) cachedTileImage.createGraphics();
		// Start x, y from 0, and draw the total amount of visible tiles.
		// But add the current viewport's x,y tiles to it and draw that tile
		// instead.
		for (int x = 0, y = 0, xPos = -cameraOffsetFromTileX; x < totalVisibleTilesWidth; x++, xPos += tileSizeWidth, y = 0) {
			for (int yPos = -cameraOffsetFromTileY; y < totalVisibleTilesHeight; y++, yPos += tileSizeHeight) {
				cachedTileImageGraphics.drawImage(tiles[x + currentVisibleTileX][y + currentVisibleTileY].image,
						xPos,
						yPos,
						null);
			}
		}
		cachedTileImageGraphics.dispose();
	}

	/**
	 * 
	 * @return One tile's width.
	 */
	public int getTileWidth() {
		return tileSizeWidth;
	}

	/**
	 * 
	 * @return One tile's height
	 */
	public int getTileHeight() {
		return tileSizeHeight;
	}

	/**
	 * Draw the tiled world to the drawScreen
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		drawScreen.drawImage(cachedTileImage, 0, 0, null);
	}
}