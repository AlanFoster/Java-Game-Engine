package hud;

import java.awt.Color;

/**
 * This interface is used by the MiniMapSystem in order to access important
 * details of tiles, such as the terrain color. Collectively all of this
 * information will be used to pre-render the terrain map within the HUD
 * minimap.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public interface IMiniMapTile {
	/**
	 * This will be used to get the minimap colour which will be drawn to the
	 * screen. For instance the color representation of a graphical tile.
	 * 
	 * @return
	 */
	Color getColor();
}
