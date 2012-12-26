package engine.misc;

import java.awt.Point;

/**
 * Represents an integer 2d location. I decided not to make use of {@link Point}
 * within this GameEngine as it allowed me to have a lot more potential
 * flexibility when the GameEngine changes over time.
 * <p>
 * This class simply stores an X,Y location.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class Location {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The X point that this Location represents
	 */
	private int x;
	/**
	 * The Y point that this Location represents
	 */
	private int y;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new location object
	 * 
	 * @param x
	 *            The X point that this Location represents
	 * @param y
	 *            The Y point that this Location represents
	 */
	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * 
	 * @return The X position that this location represents
	 */
	public int getX() {
		return x;
	}

	/**
	 * 
	 * @return The Y location that this location represents
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set the current X
	 * 
	 * @param x
	 *            The X point that htis location will now represent
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Set the current Y postion
	 * 
	 * @param y
	 *            The Y location that this point will represent
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Object's hashCode override so that it can be used in hashmaps etc
	 */
	@Override
	public int hashCode() {
		return x << 22 ^ y ^ 13379001;
	}

	/**
	 * Objects equals override so that we can do locationA.equals(locationB) as
	 * expected.
	 */
	@Override
	public boolean equals(Object foo) {
		if (foo instanceof Location) {
			Location fooLocation = (Location) foo;
			return fooLocation.getX() == x && fooLocation.getY() == y;
		}
		return super.equals(foo);
	}

	public String toString() {
		return Helpers.concat("x :: ", x, " y :: ", y);
	}
}
