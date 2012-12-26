package engine.misc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.plaf.SliderUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import world.WorldManager;

/**
 * This class makes useof the SL4J facade. I have introduced varidic methods in
 * order to provide easier means of logging information. There are different
 * scales of logging which can be set. These options can be set through the
 * static methods 'setLoggingToAll()' etc.
 * <p>
 * In order to combat Java's string immutability to save permgen a bit, each
 * logging method takes variadic args which allow for multiple values of
 * Different types, well Objects. primitives will be autoboxed, so this won't
 * cause a problem. This will handle null fine too. These logging methods all
 * call our Helpers.concat() method
 * 
 * @author alan
 * 
 * @see Helpers
 */
public class GameLogging {
	/**
	 * The actual logger which we will pass messages to. This is set in the
	 * constructor by the SL4J LoggerFactory
	 */
	protected final Logger logger;

	/**
	 * An enum collection of all the possible logging levels which can be set,
	 * and their logging precedence.
	 */
	public enum LoggingLevel {
		ALL(-1),
		INFO(1),
		DEBUG(2),
		ERROR(3),
		NONE(4);

		/**
		 * The logging value that this logging level has. This is the integer
		 * value of the log level requirement.
		 */
		int value;

		LoggingLevel(int loggingLevel) {
			this.value = loggingLevel;
		}
	}

	/**
	 * Array of classes that should only be listened to. If empty everything is
	 * logged.
	 */
	private static List<Class<?>> validClasses = new ArrayList<Class<?>>();

	/**
	 * The default logging level. See LoggingLevel for all possible logging
	 * types. This logging level defaults to 'ALL'. This can be changed by using
	 * the setLoggingToNone() etc methods
	 */
	private static LoggingLevel currentGlobalLoggingLevel = LoggingLevel.ALL;

	/**
	 * Creates a new logging object which can be used to log messages. It is
	 * through this object that you are expected to perform the actions of
	 * info/debug/error etc.
	 * 
	 * @param clazz
	 *            The class that this logger was created from
	 */
	public GameLogging(Class<?> clazz) {
		logger = LoggerFactory.getLogger(clazz);
	}

	/**
	 * Output logging of LoggingLevel info
	 * 
	 * @param objs
	 *            Variadict args for the logging types that we want to output.
	 *            For instance ("hello", foo, bar). The toString methods are
	 *            implicitly called within this method, so it is fine to give
	 *            this method null and no null point exeptions will be thrown.
	 */
	public void info(Object... objs) {
		if (LoggingLevel.INFO.value >= currentGlobalLoggingLevel.value) {
			logger.info(Helpers.concat(objs));
		}
	}

	/**
	 * Output logging of LoggingLevel debug
	 * 
	 * @param objs
	 *            Variadict args for the logging types that we want to output.
	 *            For instance ("hello", foo, bar). The toString methods are
	 *            implicitly called within this method, so it is fine to give
	 *            this method null and no null point exeptions will be thrown.
	 */
	public void debug(Object... objs) {
		if (LoggingLevel.DEBUG.value >= currentGlobalLoggingLevel.value) {
			logger.debug(Helpers.concat(objs));
		}
	}

	/**
	 * Output logging of LoggingLevel error
	 * 
	 * @param objs
	 *            Variadict args for the logging types that we want to output.
	 *            For instance ("hello", foo, bar). The toString methods are
	 *            implicitly called within this method, so it is fine to give
	 *            this method null and no null point exeptions will be thrown.
	 */
	public void error(Object... objs) {
		if (LoggingLevel.ERROR.value >= currentGlobalLoggingLevel.value) {
			logger.error(Helpers.concat(objs));
		}
	}

	/**
	 * Output logging of LoggingLevel error. This method requires the exception
	 * e to be given to it to so that a full stack trace can be outputted
	 * 
	 * @param e
	 *            The exception that will be traced. A full stack trace will be
	 *            shown to the logger.
	 * @param objs
	 *            Variadict args for the logging types that we want to output.
	 *            For instance ("hello", foo, bar). The toString methods are
	 *            implicitly called within this method, so it is fine to give
	 *            this method null and no null point exeptions will be thrown.
	 */
	public void error(Exception e, Object... objs) {
		if (LoggingLevel.ERROR.value >= currentGlobalLoggingLevel.value) {
			logger.error(Helpers.concat(objs), e);
		}
	}

	/**
	 * Changes The minimum logging level required for the message to be
	 * outputted.
	 * <p>
	 * This is the current logging level 'chart'
	 * 
	 * <pre>
	 * ALL,
	 * INFO,
	 * DEBUG,
	 * ERROR,
	 * NONE
	 * </pre>
	 * 
	 * If the logging level is ALL, then we can output logging messages of type
	 * INFO, DEBUG, ERROR. However in contrast to this if the messaging type is
	 * DEBUG, then we can only output messages higher and equal to the DEBUG
	 * level. IE DEBUG and ERROR. Therefore when the debugging level is NONE,
	 * nothing will be outputted to logging.
	 * 
	 * @param newLoggingLevel
	 *            The minimum logging level required for the message to be
	 *            outputted
	 */
	public static void setLoggingLevel(LoggingLevel newLoggingLevel) {
		currentGlobalLoggingLevel = newLoggingLevel;
	}
}
