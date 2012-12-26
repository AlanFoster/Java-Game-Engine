package eventhandling;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import engine.interfaces.ILogical;
import engine.main.GameTime;
import engine.misc.SynchronisedGameQueue;

/**
 * This EventHandler receives AWT key events and stores them for the next time
 * the game loop's logicUpdate is fired. It will then call all of the relevant
 * event methods through all objects that have registered with this system
 * (Requires the IEventable interface to be implemented).
 * <p>
 * This KeyListener will be instantiated by the GameEngine, and the key events
 * will be sent to it automatically.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class KeyEventHandler implements ILogical, KeyListener {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * A list of all registered IKeyboardEventable that want to recieve key
	 * events during the next logic update.
	 * <p>
	 * To register/remove from this system you should call the method
	 * sregisterEvents(IKeyboardEventable keyboardEventable) and
	 * removeEvents(IKeyboardEventable keyboardEventable)
	 * <p>
	 * This is a SynchronisedGameQueue so that we can stop concurrent
	 * modifications when firing events, and potentially adding more through the
	 * AWT event thread
	 */
	private SynchronisedGameQueue<IKeyboardEventable> registeredListeningObjects;

	/**
	 * Stores the list of all events which were collected during the previous
	 * logic update. During the next logic update to this class it will fire all
	 * of these events to the registered objects.
	 * <P>
	 * The implementations of this field are concurrent and thread safe
	 */
	private ConcurrentHashMap<KeyEventType, Collection<EventHolder>> waitingEventLists;

	/**
	 * Stores the list of currently pressed down keys. This is added to when the
	 * keyPressed event is fired, and removed from when keyReleased is fired. If
	 * an external class wants to see if a key is currently down, they should
	 * use isKeyDown() for that purpose
	 */
	private List<Integer> currentlyDownKeys;

	/**
	 * The list of Key Events events that this system currently manages. It is
	 * expected that any of the extra logic required for the key events will be
	 * placed in here. I have chosen an enum with the public abstract class to
	 * be able to easily add/remove features as required.
	 * <p>
	 * Note :: All KeyEventType will be automatically instantiated and available
	 * to add to waitingEventLists as required
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	private enum KeyEventType {
		/**
		 * Invoked when a key has been pressed (but obviously during a logical
		 * update only). See the Java AWT class description for KeyEvent for a
		 * definition of a key pressed event.
		 */
		KEY_PRESSED {
			@Override
			public void fireEvent(IKeyboardEventable eventableObject, KeyEvent e) {
				eventableObject.keyPressed(e);
			}
		};

		/**
		 * A public method for this enum, so it is accessible to this class, as
		 * we must remember that the class header is actually a private inner
		 * class. This abstract method requires all enums to implement it, and
		 * which will allow us to interact with the enum members to fireEvents
		 * when required
		 * 
		 * @param eventableObject
		 *            The object which will recieve the events
		 * @param e
		 *            The Java AWT KeyEvent
		 */
		public abstract void fireEvent(IKeyboardEventable eventableObject, KeyEvent e);
	}

	// ----------------------------------------------------------------
	// Constructor and Singleton methods
	// ----------------------------------------------------------------
	private KeyEventHandler() {
		registeredListeningObjects = new SynchronisedGameQueue<IKeyboardEventable>();

		waitingEventLists = new ConcurrentHashMap<KeyEventType, Collection<EventHolder>>();
		for (KeyEventType eventType : KeyEventType.values()) {
			waitingEventLists.put(eventType, Collections.synchronizedList(new ArrayList<EventHolder>()));
		}

		currentlyDownKeys = new ArrayList<Integer>(10);
	}

	/**
	 * stores the instance, only when accessed will it be be instantiated
	 */
	private static class LogicalKeyEventHandlerInstance {
		private static KeyEventHandler instance = new KeyEventHandler();
	}

	/**
	 * Gets the singleton instance of the GameAssetManager. This is lazily
	 * instantiated through the use of the private static class
	 * LogicalKeyEventHandlerInstance
	 * 
	 * @return NewGameEventHandler's instance
	 */
	public static KeyEventHandler getInstance() {
		return LogicalKeyEventHandlerInstance.instance;
	}

	// ----------------------------------------------------------------
	// Offered methods to external classes for registering/unregistering
	// ----------------------------------------------------------------
	/**
	 * When an object wants to be registered for events they will be considered
	 * during the next logical update. After this points the event type method
	 * of the interface will be called directly by the fired event during the
	 * next logical update.
	 * 
	 * @param mouseEventable
	 *            The object that wants to register for events
	 */
	public void registerEvents(IKeyboardEventable keyboardEventable) {
		registeredListeningObjects.add(keyboardEventable);
	}

	/**
	 * Removes the eventable object from receiving any fired events. This
	 * removal will happen at during the start of the next logical update
	 * 
	 * @param mouseEventable
	 *            The object that wants to unregister for events
	 */
	public void removeEvents(IKeyboardEventable keyboardEventable) {
		registeredListeningObjects.remove(keyboardEventable);
	}

	/**
	 * Removes all eventable objects from the event system entirely. This will
	 * happen during the next logical update.
	 */
	public void removeAllEvents() {
		registeredListeningObjects.clear();
	}

	// ----------------------------------------------------------------
	// Offered methods for checking for keys that are pressed
	// ----------------------------------------------------------------
	/**
	 * Use this method to check if a key is currently pressed down. Remember
	 * that Key combinations which do not result in characters, such as action
	 * keys like F1 and the HELP key, do not generate KEY_TYPED events so they
	 * can not be checked against.
	 * 
	 * @param keyCode
	 *            The AWT KeyCode
	 * @return A boolean of true if this key is currently down
	 */
	public boolean isKeyDown(int keyCode) {
		return currentlyDownKeys.indexOf(keyCode) > -1;
	}

	// ----------------------------------------------------------------
	// ILogicalObject interface implementations
	// ----------------------------------------------------------------
	@Override
	public void startUp(GameTime gameTime) {
	}

	/**
	 * This method will be called during the update cycle of the game loop.
	 * During this time we will fire all waiting events, and release any objects
	 * that didn't want to be registered with the system
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		fireWaitingEvents();
		updateAndClearLists();
	}

	/**
	 * Fire all of the events within the waiting list. The waiting list is for
	 * objects that wanted their events only fired during logical updates.
	 */
	public void fireWaitingEvents() {
		// Synchronise the waitingEventLists which stores the list of all events
		// which were collected during the previous logic update and fire them
		synchronized (waitingEventLists) {
			for (Entry<KeyEventType, Collection<EventHolder>> kvp : waitingEventLists.entrySet()) {
				for (EventHolder eventHolder : kvp.getValue()) {
					kvp.getKey().fireEvent(eventHolder.eventableObject, eventHolder.e);
				}
			}
		}
	}

	/**
	 * Clears and updates all of the lists required. In this update we will
	 * release any objects that didn't want to be registered with the system,
	 * and release any events which were fired etc
	 */
	public void updateAndClearLists() {
		// Synchronise the waitingEventLists which stores the list of all events
		// which were collected during the previous logic update and clear each
		// individual key
		synchronized (waitingEventLists) {
			for (Collection<EventHolder> eventHolderList : waitingEventLists.values()) {
				eventHolderList.clear();
			}
		}

		// Update the list of all registered IKeyboardEventable that want to
		// recieve key events during the next logic update. It is here that we
		// add those waiting, and remove those that wanted to be removed
		synchronized (registeredListeningObjects) {
			registeredListeningObjects.updateLists();
		}
	}

	/**
	 * This method will perform all of the necessary clearing of any event lists
	 * used. It is unlikely that this will be called, as I don't see many events
	 * in which there will need to be a cleanup for an event handler, but it is
	 * added just in case.
	 */
	@Override
	public void cleanUp() {
		// Synchronise the waitingEventLists which stores the list of all events
		// which were collected during the previous logic update and clear each
		// individual key
		synchronized (waitingEventLists) {
			for (Collection<EventHolder> eventHolderList : waitingEventLists.values()) {
				eventHolderList.clear();
			}
		}

		// Clear the list of all registered IKeyboardEventable that wanted to
		// recieve key events during the next logic update.
		synchronized (registeredListeningObjects) {
			registeredListeningObjects.clear();
		}
	}

	// ----------------------------------------------------------------
	// KeyListener interface implementations
	// ----------------------------------------------------------------
	/**
	 * Invoked when a key has been pressed. See the class description for
	 * KeyEvent for a definition of a key pressed event.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		// Synchronise the registeredListeningObjects
		synchronized (registeredListeningObjects) {
			for (IKeyboardEventable keyListener : registeredListeningObjects) {
				synchronized (waitingEventLists) {
					waitingEventLists.get(KeyEventType.KEY_PRESSED).add(new EventHolder(e, keyListener));
				}
			}
		}

		if (!currentlyDownKeys.contains(e.getKeyCode())) {
			currentlyDownKeys.add(e.getKeyCode());
		}
	}

	/**
	 * Invoked when a key has been released. See the class description for
	 * KeyEvent for a definition of a key released event.s
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		// Remove the key from the currently down keys. We test if the key is
		// was previously tested first because of Key combinations which do not
		// result in characters, such as action keys like F1 and the HELP key,
		// do not generate KEY_TYPED events.
		// We use indexOf to get the position of the element so we can remove it
		int index = currentlyDownKeys.indexOf(e.getKeyCode());
		if (index > -1) {
			currentlyDownKeys.remove(index);
		}
	}

	/**
	 * Invoked when a key has been typed. See the class description for KeyEvent
	 * for a definition of a key typed event.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * A basic structure that stores a KeyEvent and a reference to the object
	 * that registered to the system to be fired the event. We use this
	 * structure within waitingEventLists to fire the events to the required
	 * objects during the next logical update
	 * 
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	private static final class EventHolder {
		// ----------------------------------------------------------------
		// Fields
		// ----------------------------------------------------------------
		/**
		 * The java AWT Key Event
		 */
		public KeyEvent e;
		/**
		 * The eventable object that wants to recieve the event during the next
		 * logical update
		 */
		public IKeyboardEventable eventableObject;

		// ----------------------------------------------------------------
		// Constructor
		// ----------------------------------------------------------------
		public EventHolder(KeyEvent e, IKeyboardEventable eventableObject) {
			this.e = e;
			this.eventableObject = eventableObject;
		}
	}
}
