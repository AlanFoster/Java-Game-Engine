package world;

import hud.IMiniMapTile;

import java.awt.Color;
import java.awt.image.BufferedImage;

import pathfinding.ITile;
import pathfinding.PathFinding;

import engine.misc.GameLogging;

/**
 * A class which represents a single tile location within the world. This
 * implements the {@link IMiniMapTile} interface so that the terrain can be
 * drawn to the minimap within the hud system if required. And implements the
 * interface {@link ITile} so that it can be traversed within the Path finding
 * as required.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * 
 * @see ITile
 * @see PathFinding
 */
public class WorldTile implements IMiniMapTile, ITile {
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
	private final static GameLogging logger = new GameLogging(WorldManager.class);

	/**
	 * The graphical representatiopn of this tile within the world
	 */
	public BufferedImage image;
	
	/**
	 * Denotes whether or not a tile is passable, this is used for AI path
	 * finding. true if it is passable, false if it is not.
	 */
	public boolean passable;

	/**
	 * The path of passing through this tile, this is used during the AI path
	 * finding. IE for mud it will be high, but for ground it will be low.
	 */
	int cost;

	/**
	 * The colour of the tile which will be drawn on the minimap, this is the
	 * 'terrain colour'
	 */
	public Color minimapColor;

	/**
	 * The tile width
	 */
	public int tileWidth;

	/**
	 * The tile height
	 */
	public int tileHeight;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	/**
	 * Creates a new tile
	 * 
	 * @param image
	 *            The visual image represented by this tile
	 * @param passable
	 *            true if it is passable, false if it is not. This will be used
	 *            for path finding etc
	 * @param minimapColor
	 *            The colour of the tile which will be drawn on the minimap,
	 *            this is the 'terrain colour'
	 * @param cost
	 *            The path of passing through this tile, this is used during the
	 *            AI path finding. IE for mud it will be high, but for ground it
	 *            will be low.
	 */
	WorldTile(BufferedImage image, boolean passable, int minimapColor, int cost) {
		this.passable = passable;
		if (image == null) {
			logger.error(new NullPointerException(), "Attempted to add a null tile image to the map!");
		}
		this.image = image;
		tileWidth = image.getWidth();
		tileHeight = image.getHeight();

		this.minimapColor = new Color(minimapColor);
	}

	// ----------------------------------------------------------------
	// IMiniMapTile implementations
	// ----------------------------------------------------------------
	@Override
	public Color getColor() {
		return minimapColor;
	}

	// ----------------------------------------------------------------
	// ITile implementations -- Path Finding
	// ----------------------------------------------------------------
	/**
	 * The terrain cost of passing this tile, IE for mud it will be high, but
	 * for ground it will be low.
	 */
	@Override
	public int getTerrainCost() {
		return cost;
	}

	/**
	 * True if this tile is passable, false if it is not passable
	 */
	@Override
	public boolean isPassable() {
		return passable;
	}

	/**
	 * Get the width of the tile
	 */
	@Override
	public int getWidth() {
		return tileWidth;
	}

	/**
	 * Get the height of the tile
	 */
	@Override
	public int getHeight() {
		return tileHeight;
	}
}