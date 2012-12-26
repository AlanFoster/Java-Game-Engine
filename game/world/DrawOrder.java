package world;

/**
 * The draw order of the world, remember the basic GameLayer drawing logic still
 * applies however. For instance if we have two GameLayers A and B, and if A
 * contains all of our entity rendering systems, B will always render over A.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public enum DrawOrder {
	ENEMY,
	PARTICLES,
	PLAYER,
	BARRELS,
	SHIELDS,
	// Currently it just tests the objects which can degrade, and is used just for an example.
	// IE BlockTemplate
	DEGRADE_TESTS,
	BULLETS,
	EXPLOSIONS
}