package engine.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import engine.interfaces.ILogical;

/**
 * This class has been created in order to provide a means of stopping
 * concurrent modifications. Concurrent modifications occur when you are
 * iterating over a collection and then you attempt to delete an item when
 * iterating over it. The easiest solution to this problem is to use an
 * iterator, and calling iterator.remove() rather than list.remove(item);
 * <p>
 * However an iterator is not suitable if for instance the list is modified
 * externally whilst iterating over it. For instance calling methods of items of
 * a list, which may modify the list.
 * <p>
 * Another solution to this concurrent modification problem is to use a CoW list
 * (<a href="http://download.oracle.com/javase/6/docs/api/java/util/concurrent/
 * CopyOnWriteArrayList.html">Class CopyOnWriteArrayList<E>l</a>). However, the
 * main drawback to this solution is the fact it is immutable... Which isn't
 * exactly great for a game.
 * <p>
 * My 'solution' to this is to create a class which will allow me to implement 3
 * Separate lists. One containing the 'current' objects, one containing the
 * 'waiting' objects and one containing the 'deleted' objects. Obviously having
 * three lists is not exactly the best solution in the world. BUT, as it is
 * suggested that other classes make use of this GameQueue class, if a better
 * implementation comes along, it'll be easy to change it in this one
 * Centralized place. To make things easier in terms of flexibility, we can take
 * advantage of polymorphism and just implement List<T>
 * <p>
 * Any methods which are written 'deprecated' haven't been implemented, and at
 * the time of writing there was no need to. Another thing to note is the the
 * name is potentially misleading. This does not implement a Queue FIFO
 * structure, but instead it uses ArrayLists internally. The 'queue' name arises
 * from the fact items are not immediately added/deleted, but instead added to
 * the queue of objects waiting to be added/delete
 * <p>
 * Below is an example of the GameQueue will work.
 * 
 * <pre><code>
 * 	GameQueue<String> testing = new GameQueue<String>();
		String a = "a";
		String b = "b";
		String c = "c";

		testing.add(a);
		System.out.println(testing);
		testing.updateLists();
		System.out.println(testing);
		testing.remove(a);
		System.out.println(testing);
		testing.updateLists();
		System.out.println(testing);
 * </code>
 * <pre>
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GameQueue<T> implements List<T> {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The list of objects which have not yet been added tot he 'currentObjects'
	 * list. On the next updateLists call these will be added to the
	 * currentObjects list
	 */
	protected List<T> waitingObjects;
	/**
	 * The list of current objects which will be returned when other classes ask
	 * for objects
	 */
	protected List<T> currentObjects;
	/**
	 * Updates that need to be removed from the currentObjects on the next
	 * refreshList call
	 */
	protected List<T> deletionObjects;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new GameQueue object. Read the header for further details.
	 */
	public GameQueue() {
		waitingObjects = new ArrayList<T>();
		currentObjects = new ArrayList<T>();
		deletionObjects = new ArrayList<T>();
	}

	// ----------------------------------------------------------------
	// GameQueue Methods
	// ----------------------------------------------------------------
	/**
	 * Call this method explicitly when you want for all of the lists to be
	 * updated. For instance when you want for all waiting objects to be added
	 * to the currentObjects list, and for all deletedObjects to be removed.
	 * <p>
	 * Since this class has been created solely to stop concurrent modification
	 * ... PLEASE do not call it when iterating your GameQueue...
	 */
	public void updateLists() {
		currentObjects.addAll(waitingObjects);
		currentObjects.removeAll(deletionObjects);

		waitingObjects.clear();
		deletionObjects.clear();
	}

	// ----------------------------------------------------------------
	// List implementations
	// ----------------------------------------------------------------
	/**
	 * Gets all of the currently waiting objects which will be added the next
	 * time updateLists() is called
	 */
	public List<T> getAllWaiting() {
		return waitingObjects;
	}

	/**
	 * Get all of the 'current' objects on the list
	 */
	public List<T> getAllCurrent() {
		return currentObjects;
	}

	/**
	 * Returns all of list of objects which will be deleted the next time
	 * updateLists() is called
	 */
	public List<T> getAllWaitingDeletion() {
		return deletionObjects;
	}

	@Override
	public boolean add(T arg1) {
		return waitingObjects.add(arg1);
	}

	@Override
	public void add(int arg0, T arg1) {
	}

	@Override
	public boolean addAll(Collection collection) {
		return waitingObjects.addAll(collection);
	}

	@Override
	public boolean addAll(int arg0, Collection collection) {
		return waitingObjects.addAll(arg0, collection);
	}

	/**
	 * Clears all three lists. Waiting objects, current objects and deleted
	 * objects. Potentially the desired outcome may be for all current and
	 * waiting objects to be deleted. This will be easy to change.
	 */
	@Override
	public void clear() {
		waitingObjects.clear();
		currentObjects.clear();
		deletionObjects.clear();
	}

	/**
	 * Returns if the current object list contains this object
	 */
	@Override
	public boolean contains(Object arg0) {
		return currentObjects.contains(arg0);
	}

	/**
	 * Returns if the current object list contains all of these objects
	 */
	@Override
	public boolean containsAll(Collection arg0) {
		return currentObjects.contains(arg0);
	}

	@Override
	public T get(int arg0) {
		return currentObjects.get(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		return currentObjects.indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		return currentObjects.size() > 0;
	}

	@Override
	public Iterator<T> iterator() {
		return currentObjects.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return currentObjects.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return currentObjects.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return currentObjects.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		int index = currentObjects.indexOf(o);
		if (index > -1) {
			T removedObject = currentObjects.get(index);
			if (removedObject != null) {
				deletionObjects.add(removedObject);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public T remove(int index) {
		T removedElem = currentObjects.remove(index);
		deletionObjects.add(removedElem);
		return removedElem;
	}

	@Override
	public boolean removeAll(Collection arg0) {
		return deletionObjects.addAll(currentObjects);
	}

	@Override
	public int size() {
		return currentObjects.size();
	}
	// ----------------------------------------------------------------
	// Object Overrides
	// ----------------------------------------------------------------
	@Override
	public String toString() {
		return Helpers.concat("{waitingObjects :: ", waitingObjects, "},\n",
				"{current object :: ", currentObjects, "},\n",
				"{deleted objects :: ", deletionObjects, "}\n");
	}

	// ----------------------------------------------------------------
	// Not Supported
	// -----------------------------------------------------------------
	// Any methods which are annotated as 'deprecated' haven't been
	// implemented fully, and at the time of writing there was no need to add
	// these.
	// However they are easy to add by calling the methods of the lists.
	// ----------------------------------------------------------------
	@Deprecated
	@Override
	public boolean retainAll(Collection arg0) {
		return false;
	}

	@Deprecated
	@Override
	public T set(int arg0, Object arg1) {
		return null;
	}
	@Deprecated
	@Override
	public List<T> subList(int arg0, int arg1) {
		return null;
	}

	@Deprecated
	@Override
	public Object[] toArray() {
		return null;
	}

	@Deprecated
	@Override
	public T[] toArray(Object[] arg0) {
		return null;
	}
}
