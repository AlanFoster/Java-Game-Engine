package hud;

/**
 * Where to draw an item in respect to the game window. These enums are used by
 * the HUDSystem to decide where to relatively draw the HUDItem to the screen
 */
public enum DrawRelativeToViewport {
	// Draws to the top left of the screen
	TOP_LEFT,
	// Draws to the top right of the screen
	TOP_RIGHT,
	// Draws to the bottom left of the screen
	BOTTOM_LEFT,
	// Draws to the bottom right of the screen
	BOTTOM_RIGHT,
	// Draws to the center of the screen
	CENTER
}