package entitysystems.components;

import hud.MiniMapSystem;

import java.awt.Color;

import entitysystem.core.IComponent;
import entitysystem.systems.HUDSystem;

/**
 * If an entity has this component attached it will be drawn to the minimap
 * within the hud. Currently the only useful information i can think of putting
 * here is the colour which will be represented on the minimap.
 * <p>
 * Note, an entity must also contain spatial and MinimapDrawn components to be
 * used by the MiniMapSystem
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see HUDSystem
 * @see MiniMapSystem
 */
public class MinimapDrawn implements IComponent {
	/**
	 * The colour used within the minimap.
	 */
	public Color minimapColor;

	public MinimapDrawn(Color minimapColor) {
		this.minimapColor = minimapColor;
	}

	public MinimapDrawn(int minimapColor) {
		this.minimapColor = new Color(minimapColor);
	}
}
