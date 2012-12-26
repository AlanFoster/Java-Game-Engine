package engine.main;

import engine.misc.GameLogging;

/**
 * This class offers a way of checking the total elapsed time since updates,
 * which is useful for animation classes etc. This class is based on the
 * GameTime class offered by C#.
 * <p>
 * I haven't really looked at the GameTime class offered with XNA But i'll just
 * guess based on lectures.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GameTime {
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
	private final static GameLogging logger = new GameLogging(GameTime.class);

	/**
	 * The total elapsed time which is incremented every game loop. This is
	 * incremented by the updateTime method, and is nanoseconds. Once a second
	 * has passed, this will be reset to 0, and take into consideration any
	 * excess time of one second too
	 */
	private long totalElapsedGameTime;

	/**
	 * The elapsed time of the game update cycle in nanoseconds
	 */
	private long elapsedGameTimeNano;

	/**
	 * The elapsed time of the game update cycle in milliseconds
	 */
	private long elapsedGameTimeMilli;

	/**
	 * Tracks the previous game time, used to work out the total elapsed time
	 * from the previous update cycle
	 */
	long previousGameTime;

	/**
	 * The type of update which occurred. For instance when we run slow, it will
	 * be LOGIC_ONLY, which means we did not do any rendering
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	public enum UpdateType {
		LOGIC_ONLY,
		RENDER_ONLY,
		RENDER_AND_LOGIC
	}

	/**
	 * Used to calculate the total amount of logic updates per second, this is
	 * our UPS
	 */
	private int logicUpdateCount;

	/**
	 * Used to calculate the total amount of renders per second, this is our FPS
	 */
	private int renderUpdateCount;

	// ----------------------------------------------------------------
	// Methods :: Updater
	// ----------------------------------------------------------------
	void updateTime(long nanoSecs, UpdateType updateType) {
		// Initialise the any variables to their starting values
		if (previousGameTime == 0) {
			previousGameTime = nanoSecs;
		}

		// Work out the elapsed time since the previous update
		elapsedGameTimeNano = nanoSecs - previousGameTime;
		elapsedGameTimeMilli = elapsedGameTimeNano / 1000000l;

		previousGameTime = nanoSecs;

		// Update our stats of updates per second and renders per second
		switch (updateType) {
			case LOGIC_ONLY:
				logicUpdateCount++;
				break;
			case RENDER_ONLY:
				renderUpdateCount++;
				break;
			case RENDER_AND_LOGIC:
				logicUpdateCount++;
				renderUpdateCount++;
				break;
		}

		// Increment the 'total' elapsed time, this is used to check if one
		// second has passed, which we can then use to get the FPS/UPS
		totalElapsedGameTime += elapsedGameTimeNano;

		// Work out if a second has passed, if so output the average FPS
		// I liked 1.7's use of underscores in numbers :(
		// which use to have :
		// if (nanoSecs > FPSRecordingTime + 1_000_000_000l) {
		if (totalElapsedGameTime > 1000000000l) {
			logger.info("FPS :: ", renderUpdateCount,
					" UPS :: ", logicUpdateCount);

			// Take away a second from our total recording time, but keep the
			// offset for more reliable results
			totalElapsedGameTime -= 1000000000l;
			// totalElapsedGameTime = 0;

			// reset all of our tracking
			logicUpdateCount = 0;
			renderUpdateCount = 0;
		}
	}

	// ----------------------------------------------------------------
	// Methods :: Getters
	// ----------------------------------------------------------------
	/**
	 * @return Defined as the total of all update times in nanoseconds
	 */
	public long getElapsedTimeNano() {
		return elapsedGameTimeNano;
	}

	/**
	 * @return Defined as the total of all update times in milliseconds
	 */
	public long getElapsedTimeMilli() {
		return elapsedGameTimeMilli;
	}
}
