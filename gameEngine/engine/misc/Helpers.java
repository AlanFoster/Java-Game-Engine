package engine.misc;

/**
 * A collection of static helping methods which should be used by classes as
 * required. This classes constructor is private as it should not be
 * instantiated.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public final class Helpers {
	/**
	 * This class shouldn't be instantiated as it's just a bunch of static
	 * methods.
	 */
	private Helpers() {
	}

	/**
	 * The suggested method of concatenation for strings. Makes use of variadic
	 * args and StringBuffer to create a single concatenated string
	 * <p>
	 * Handles null objects just fine too.
	 * 
	 * @param args
	 *            Variadict args of the objects we want to call the toString
	 *            method of, for instance ("hello", foo, bar). The toString
	 *            methods are implicitly called within this method, so it is
	 *            fine to give this method null and no null point exceptions
	 *            will be thrown.
	 */
	public static String concat(Object... args) {
		StringBuilder sb = new StringBuilder();
		// Iterate all objects and append to our string buffer
		for (Object o : args)
			sb.append(o);
		return sb.toString();
	}

	/**
	 * Calculates the euclidean distance between two points
	 * 
	 * @param x1
	 *            The starting X
	 * @param y1
	 *            The starting y
	 * @param x2
	 *            The ending X
	 * @param y2
	 *            The ending Y
	 * @return The euclidean distance
	 */
	public static double calculateEuclideanDistance(double x1, double y1, double x2, double y2) {
		double dy = y2 - y1, dx = x2 - x1;
		return Math.abs(Math.sqrt((dy * dy) + (dx * dx)));
	}

	/**
	 * Calculates the distance squared between two points
	 * 
	 * @param x1
	 *            The starting X
	 * @param y1
	 *            The starting y
	 * @param x2
	 *            The ending X
	 * @param y2
	 *            The ending Y
	 * @return The distance squared
	 */
	public static double calculateDistanceSquared(double x1, double y1, double x2, double y2) {
		double dy = y2 - y1, dx = x2 - x1;
		return Math.abs((dy * dy) + (dx * dx));
	}

	/**
	 * Adds a leading zero to a number. Particularly useful for time. Easily
	 * adaptable to addLeadingZeros(num, amount) if required, by using
	 * String.format again
	 * 
	 * @param num
	 *            The number that needs to be formatted. IE, 8.
	 * @return Returns a string with a leading zero if it is 1 digit long. For
	 *         instance 8 returns "08"
	 */
	public static String addLeadingZero(long num) {
		return String.format("%02d", num);
	}

	/**
	 * @param min
	 *            The minimum number which can be returned
	 * @param max
	 *            THe maximum number which can be returned
	 * @returna A number between min and max
	 */
	public static double randomBetween(double min, double max) {
		if (max < min) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		double difference = max - min;
		return min + (Math.random() * difference);
	}

	/**
	 * @param min
	 *            The minimum number which can be returned
	 * @param max
	 *            THe maximum number which can be returned
	 * @returns A number between min and max
	 */
	public static int randomBetween(int min, int max) {
		if (max < min) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		double difference = max - min;
		return (int) (min + (Math.random() * difference));
	}

	/**
	 * Takes a radian value and makes sure it is between 0 and 2PI
	 * 
	 * @param r
	 *            The radian which needs to be 'corrected'
	 */
	public static double correctRadian(double r) {
		return r - Math.floor(r / (2 * Math.PI)) * (2 * Math.PI);
	}
}
