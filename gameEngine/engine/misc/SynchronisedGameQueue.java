package engine.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class offers a synchronised ArrayList alternative to GameQueue's
 * non-synchronised offerings. For more details on this class, please read the
 * comments of the super class {@link GameQueue}
 * 
 * @param <T>
 *            The generic type of the lists
 * 
 * @see GameQueue
 */
public class SynchronisedGameQueue<T> extends GameQueue<T> {
	/**
	 * "Override" the constructor of the GameQueue and provide the new
	 * Collections.synchronisedList implementations instead of the
	 * non-synchronised lists which the GameQueue originally uses. This will
	 * allow for us to synchronise the list for threading, and as GameQueue
	 * simply stores its object's data type as the interface List, we can do
	 * this. Taking full advtange of polymorphism.
	 */
	public SynchronisedGameQueue() {
		waitingObjects = Collections.synchronizedList(new ArrayList<T>());
		currentObjects = Collections.synchronizedList(new ArrayList<T>());
		deletionObjects = Collections.synchronizedList(new ArrayList<T>());
	}
}
