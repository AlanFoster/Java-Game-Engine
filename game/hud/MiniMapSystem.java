package hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import world.WorldTile;
import world.World;

import engine.components.GameSizeableComponent;
import engine.interfaces.IDrawable;
import engine.interfaces.ILogical;
import engine.main.GameEngine;
import engine.main.GameObjectBase;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.GameViewPort;
import engine.misc.Helpers;
import engine.misc.Location;
import entitysystem.core.Entity;
import entitysystem.core.EntityManager;
import entitysystem.core.IEntitySystem;
import entitysystem.systems.HUDSystem;
import entitysystems.components.HUDableComponent;
import entitysystems.components.MinimapDrawn;
import entitysystems.components.Spatial;

/**
 * The minimap is split into two distinct sections. The bottom 'layer' consists
 * of the map details passed in about the terrain. This is then cached once into
 * a cachedMap. This layer will show the basic terrain of the map.
 * <p>
 * The rest of the data comes from the entity system. We request the list of
 * entities that want drawn to the minimap, if they have a component
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class MiniMapSystem extends HudItem implements IEntitySystem {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameSizeableComponent.class);
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The cached terrain map which sits underneath the updated object positions
	 */
	private BufferedImage cachedMap;
	/**
	 * The graphics 2D object for the cachedMap
	 */
	private Graphics2D graphicsCachedMap;

	// Calculated by working out what size each tile needs to be in order to
	// nicely fit inside the width/height
	private int minimapTileSizeWidth;
	private int minimapTileSizeHeight;

	/**
	 * The scale of the drawn minimap. IE if there is 100 map tiles width, and
	 * the scale factor is 2, then the width will be 200 pixels
	 */
	private int scaleFactor;

	/**
	 * Used to show the boundaries of view within the minimap.
	 */
	private ViewPortLocations viewPortLocations;

	/**
	 * A list of entities that want drawn to the minimap
	 */
	private List<Entity> entityList;
	private EntityManager entityManager;

	private int worldTileWidth;
	private int worldTileHeight;

	/**
	 * 
	 * @param entityManager
	 *            Access to the entity manager so we can get all entities that
	 *            have the {@link MinimapDrawn} component attached to them
	 * @param drawLocation
	 *            The relative drawing location used by the {@link HUDSystem} to
	 *            determine where the minimap should be drawn
	 * @param mapTiles
	 *            The worlds array of tiles which will be used to generate our
	 *            terrain map
	 * @param tileWidth
	 *            The width of the worlds tiles
	 * @param tileHeight
	 *            the world of the worlds height
	 * @param viewPort
	 *            Access to the viewport so that we can draw the viewport
	 *            position within the minimap
	 * @param scaleFactor
	 *            The scale of the drawn minimap. IE if there is 100 map tiles
	 *            width, and the scale factor is 2, then the width will be 200
	 *            pixels
	 */
	public MiniMapSystem(EntityManager entityManager, DrawRelativeToViewport drawLocation,
			IMiniMapTile[][] mapTiles,
			int tileWidth, int tileHeight,
			GameViewPort viewPort,
			int scaleFactor) {
		super(drawLocation, 0, 0, scaleFactor * mapTiles.length, scaleFactor * mapTiles[0].length);

		viewPortLocations = new ViewPortLocations(viewPort);
		this.scaleFactor = scaleFactor;

		this.worldTileWidth = tileWidth;
		this.worldTileHeight = tileHeight;

		entityList = new ArrayList<Entity>();

		this.entityManager = entityManager;
		entityManager.addObserver(this);
		refreshList();

		// Create our terrain map with the maptiles
		createCachedTerrainMap(mapTiles);
	}

	/**
	 * Creates the cached image which is drawn first. This is the bottom 'layer'
	 * of the minimap, this layer will shows the basic terrain of the map. The
	 * colour information is retrieved from the mapTiles of our interface type
	 * {@link IMiniMapTile}
	 * 
	 * @param mapTiles
	 *            The worlds terrain tiles which should be drawn to the minimap
	 *            to be cached
	 */
	protected void createCachedTerrainMap(IMiniMapTile[][] mapTiles) {
		cachedMap = new BufferedImage((int) spatial.width, (int) spatial.height, BufferedImage.TYPE_INT_ARGB);
		int totalTilesX = mapTiles.length;
		int totalTilesY = mapTiles[0].length;
		minimapTileSizeWidth = (int) (spatial.width / totalTilesX);
		minimapTileSizeHeight = (int) (spatial.height / totalTilesY);

		// Draw the map by iterating over each tile within the map and drawing
		// their required minimap colour.
		graphicsCachedMap = cachedMap.createGraphics();
		Color tileColor = null;
		for (int x = 0; x < totalTilesX; x++) {
			for (int y = 0; y < totalTilesY; y++) {
				try {
					tileColor = new Color(mapTiles[x][y].getColor().getRed(), mapTiles[x][y].getColor().getGreen(), mapTiles[x][y].getColor().getBlue(), 150);
					graphicsCachedMap.setColor(tileColor);
					graphicsCachedMap.fillRect(x * minimapTileSizeWidth, y * minimapTileSizeHeight, minimapTileSizeWidth, minimapTileSizeHeight);
				} catch (Exception e) {
					logger.info(Helpers.concat(x, " ", y));
				}
			}
		}
		graphicsCachedMap.dispose();
	}

	@Override
	public void startUp(GameTime gameTime) {
	}

	@Override
	public void cleanUp() {
		entityList.clear();
	}

	/**
	 * Draw the minimap in two stages. Firstly draw the terrain of the map
	 * (which we have cached during the minimap load) then we iterate over the
	 * list of entities that want to be drawn to the screen and draw them
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		// Draw our cached map, this is the 'terrain' layer
		drawScreen.drawImage(cachedMap, null, offsetX, offsetY);

		// Draw a nice border around the entire minimap
		drawScreen.setColor(Color.white);
		drawScreen.drawRect((int) (offsetX + spatial.x), (int) (spatial.y + offsetY), (int) spatial.width, (int) spatial.height);

		/**
		 * Iterate all of the entites which have the MinimapDrawn component
		 * attached to them
		 */
		for (Entity entity : entityList) {
			Spatial entitySpatial = entity.getComponent(Spatial.class);
			MinimapDrawn entityDetails = entity.getComponent(MinimapDrawn.class);

			drawScreen.setColor(entityDetails.minimapColor);
			drawScreen.fillRect((int) (offsetX + (entitySpatial.x / worldTileWidth) * minimapTileSizeWidth),
					(int) (offsetY + (entitySpatial.y / worldTileHeight) * minimapTileSizeHeight),
					minimapTileSizeWidth, minimapTileSizeHeight);
		}

		/**
		 * Draw the borders of our viewport in relation to the minimap
		 */
		drawScreen.setColor(Color.white);
		// Get the newest details about the viewport locations
		viewPortLocations.updateDetails();
		drawScreen.drawRect((int) ((offsetX + spatial.x) + (viewPortLocations.topLeftX * minimapTileSizeWidth)),
				(int) ((offsetY + spatial.y) + (viewPortLocations.topLeftY * minimapTileSizeHeight)),
				viewPortLocations.width, viewPortLocations.height);
	}

	/**
	 * Takes an actual X,Y location, and returns an X,Y tile location which is
	 * relative to the map's size
	 */
	public static void convertToTileLocation(int x, int y) {
	}

	@Override
	public void logicUpdate(GameTime gameTime) {

	}

	/**
	 * We observe the EntityManager for it to tell us when we need to refresh
	 * our entity lists
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof EntityManager) {
			refreshList();
		}
	}

	@Override
	public void refreshList() {
		entityList = entityManager.getEntitiesContaining(MinimapDrawn.class, Spatial.class);
	}

	/**
	 * A simple data structure to store the drawing details of the viewport.
	 * <p>
	 * This class converts the viewport's actual x,y, width, height into the
	 * tiles which they fall on within the minimap. Ie if the tile width is 100,
	 * and the viewport's top left x is at 1000, then we will return 1000/100 as
	 * being where the viewport falls in the tile location.
	 */
	private class ViewPortLocations {
		private GameViewPort viewPort;

		public int topLeftX, topLeftY;
		public int width, height;

		ViewPortLocations(GameViewPort viewPort) {
			this.viewPort = viewPort;
		}

		/**
		 * Recalculate all of the relative coordinates for the viewport location
		 */
		public void updateDetails() {
			Location topLeftCoords = viewPort.getTopLeftLocation();
			topLeftX = topLeftCoords.getX() / worldTileWidth;
			topLeftY = topLeftCoords.getY() / worldTileHeight;
			// Multiply width and height by two, to get the full viewport's size
			width = (int) ((viewPort.getWidth() * 2) / worldTileWidth);
			height = (int) ((viewPort.getHeight() * 2) / worldTileHeight);
		}
	}
}
