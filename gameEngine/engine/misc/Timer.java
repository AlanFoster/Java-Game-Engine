package engine.misc;

/**
 * This class is used as a simple count down timer. It will receive an initial
 * time in milliseconds to count down from, and this time can be reduced by
 * calling the method decreaseTimer() or increaseTimer().
 * <p>
 * This timer does not automatically decrease in time it self, it must
 * explicitly be reduced by the increase/decreaseTimer method. Therefore this
 * class is useful for when trying to do 'game related' time. IE, when we pause
 * a game layer which is storing the level time, we wouldn't want for it to keep
 * counting down the level whilst paused. This is a useful feature which can be
 * taken advantage of.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class Timer {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/** The time remaining in milliseconds */
	public long time;

	/**
	 * The time in seconds. Note, this is not the total time of milliseconds
	 * converted to seconds, instead it is the 'seconds hand' of a clock. IE,
	 * the number will be between 0 and 60. If you want milliseconds time in
	 * seconds, use the method getTimeAsSeconds() instead
	 */
	private long seconds;
	/**
	 * The remamining time in minutes, between 0-59
	 */
	private long minutes;
	/**
	 * The time remaining in hours, values 0 and above
	 */
	private long hours;

	/**
	 * This value is true if the hours, minutes, or seconds has changed since
	 * the previous call to decreaseTimer
	 */
	public boolean hasChanged;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new timer with the default time of 0 seconds
	 */
	public Timer() {
	}

	/**
	 * Creates a new timer with a specified starting time in milliseconds
	 * 
	 * @param time
	 *            The starting time of the timer in milliseconds
	 */
	public Timer(long time) {
		setTime(time);
	}
	/**
	 * Overwrites the existing time and replaces it with the given time in
	 * milliseconds
	 * 
	 * @param time
	 *            The new time in milliseconds that will be given to this timer
	 */
	public void setTime(long time) {
		this.time = time;
		updateTimeFields();
	}

	/**
	 * @return True if the timer has reached zero, false if zero has not yet
	 *         been reached
	 */
	public boolean isFinished() {
		return time <= 0;
	}

	/**
	 * Use this method when you want to decrease the current time represented by
	 * this object.
	 * 
	 * @param time
	 *            The milliseconds that we should decrease the current time
	 *            represented by this object by
	 */
	public void decreaseTimer(long time) {
		this.time -= time;
		updateTimeFields();
	}

	/**
	 * Use this method when you want to increase the current time represented by
	 * this object.
	 * 
	 * @param time
	 *            The milliseconds that we should increase the current time
	 *            represented by this object by
	 */
	public void increaseTimer(long time) {
		this.time -= time;
		updateTimeFields();
	}

	/**
	 * This method does all of the conversions of the current time in
	 * milliseconds to the other time formats. It is here that we set the value
	 * of 'hasChanged' which is used as an indication that the timer has
	 * decreased in time
	 */
	protected void updateTimeFields() {
		long timeAsSeconds = getTimeAsSeconds();

		// Store the starting seconds minutes and hours so that we can compare
		// this to the new minutes seconds and hours to set the hasChanged
		// variable
		long startS = seconds, startM = minutes, startH = hours;
		seconds = timeAsSeconds % 60;
		minutes = ((getTimeAsSeconds() / 60) % 60);
		hours = timeAsSeconds / 3600;

		// Change the boolean hasChanged to true if any of the starting values
		// are different to the new times we calculated
		hasChanged = startS != seconds || startM != minutes || startH != hours;
	}

	/**
	 * This method is different to getSeconds() as this method performs the
	 * actual calculation of the millisecond to second conversion, whilst the
	 * getSeconds method will only return the field set by the method
	 * updateTimeFields
	 * 
	 * @return The current time that this timer represents in seconds
	 */
	protected long getTimeAsSeconds() {
		return time / 1000;
	}

	/**
	 * 
	 * @return The current time that this timer represents in seconds
	 */
	public long getSeconds() {
		return seconds;
	}

	/**
	 * 
	 * @return The current time that this timer represents in minutes
	 */
	public long getMinutes() {
		return minutes;
	}

	/**
	 * 
	 * @return The current time that this timer represents in hour
	 */
	public long getHours() {
		return hours;
	}

	/**
	 * 
	 * @return A formatted string of the time that this timer represents in the
	 *         format 'H : M : S' with a single leading zero
	 */
	public String getAsString() {
		// Get the new String
		String currentFormattedString = Helpers.concat(Helpers.addLeadingZero(getHours()),
				" : ", Helpers.addLeadingZero(getMinutes()),
				" : ", Helpers.addLeadingZero(getSeconds()),
				" ");
		return currentFormattedString;
	}

}