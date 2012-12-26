package pathfinding;

/**
 * This interface is the requirement implementation for tiles to have when
 * wanting to be used in conjunction with the PathFinding class
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public interface ITile {
	/**
	 * The terrain cost of passing this tile, IE for mud it will be high, but
	 * for ground it will be low.
	 */
	int getTerrainCost();
	/**
	 * True if this tile is passable, false if it is not passable
	 */
	boolean isPassable();

	/**
	 * Get the width of the tile
	 * 
	 * @return The width of the tile in pixels
	 */
	int getWidth();
	/**
	 * Get the height of the tile
	 * 
	 * @return The height of the tile in pixels
	 */
	int getHeight();
}
