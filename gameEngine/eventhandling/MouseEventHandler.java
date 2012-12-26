package eventhandling;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import engine.interfaces.ILogical;
import engine.main.GameEngine;
import engine.main.GameTime;
import engine.misc.GameQueue;
import engine.misc.SynchronisedGameQueue;

/**
 * This EventHandler receives AWT mouse events and stores them for the next time
 * the game loop's logicUpdate is fired. It will then call all of the relevant
 * event methods through all objects that have registered with this system
 * (Requires the IEventable interface to be implemented)
 * <p>
 * This MouseMotionListener, MouseListener will be instantiated by the
 * GameEngine, and the key events will be sent to it automatically.
 * <p>
 * This class borrows the event notation from JavaScript (IE onmousefocus etc)
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameEngine
 * @see KeyEventHandler
 * 
 */
public class MouseEventHandler implements ILogical, MouseMotionListener, MouseListener {
	// ----------------------------------------------------------------
	// Public accessible fields
	// ----------------------------------------------------------------
	// Fields that other classes can have access to. I would potentially like to
	// pull this logic out of this class as it clutters it up for no reason. But
	// that's not hugely important currently.
	/**
	 * Stores the X position of the last mouse click
	 * */
	public int mouseClickPosX;
	/**
	 * Stores the Y position of the last mouse click
	 * */
	public int mouseClickPosY;

	/**
	 * Stores the current X position of the mouse
	 */
	public int mousePosX;
	/**
	 * Stores the current Y position of the mouse
	 */
	public int mousePosY;

	/**
	 * Returns whether or not the left mouse click button was down
	 */
	public boolean leftClickDown;

	/**
	 * Returns whether or not the right click was down
	 */
	public boolean rightClickDown;

	// ----------------------------------------------------------------
	// Event Handler fields
	// ----------------------------------------------------------------

	/**
	 * A list of all of the eventable objects within the system that want to
	 * receive their events when the mouse . When an object is registered with
	 * this event handler it will be this list that it iterates over and checks
	 * the boundaries for each object.
	 * <p>
	 * To add/remove from this list you should be calling the methods
	 * registerEvents() or removeEvents()
	 * <p>
	 * This is a SynchronisedGameQueue so that we can stop concurrent
	 * modifications when firing events, and potentially adding more through the
	 * AWT event thread
	 */
	private SynchronisedGameQueue<IMouseEventable> registeredListeningObjects;

	/**
	 * Stores a list of objects the user's mouse is currently over their
	 * dimensions. This list is used to keep track of when the mouse then leaves
	 * the bounds of the eventable object, so that we can then fire its mouseout
	 * event. When the mouse moves out of the dimensions of the IEventable we
	 * will fire the MOUSE_OUT events
	 */
	private SynchronisedGameQueue<IMouseEventable> currentMouseOver;

	/**
	 * Stores a list of objects the user's mouse clicked on at one point. This
	 * list is used to keep track of when the mouse clicks out of the bounds of
	 * that object, when this happens we will then fire the MOUSE_BLUR events
	 */
	private SynchronisedGameQueue<IMouseEventable> currentMouseFocus;

	/**
	 * Stores the list of all events which were collected during the previous
	 * logic update. During the next logic update to this class it will fire all
	 * of these events to the registered objects.
	 * <P>
	 * The implementations of this field are concurrent and thread safe
	 */
	private Map<MouseEventType, Collection<IMouseEventable>> waitingEventLists;

	/**
	 * The list of MouseEventType events that this system currently manages. It
	 * is expected that any of the extra logic required for the mouse event
	 * handler will be placed in here. I have chosen an enum with the public
	 * abstract class to be able to easily add/remove features as required.
	 * <p>
	 * Note :: All MouseEventTypes will be automatically instantiated and
	 * available to add to waitingEventLists as required
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	private enum MouseEventType {
		/**
		 * Fired When the mouse enters the dimension of an object, this is only
		 * fired once. IE, if a mouse is over an object for more than 1 game
		 * update, it will only be fired once and not every time until the user
		 * moves their mouse out
		 */
		MOUSE_OVER {
			@Override
			public void fireEvent(IMouseEventable eventableObject) {
				eventableObject.mouseOver();
			}
		},
		/**
		 * Fired When the mouse LEAVES the dimension of an object, this is only
		 * fired once. IE, if a the mouse entered a game object, and left the
		 * game object it is only fired on the one game loop update
		 */
		MOUSE_OUT {
			@Override
			public void fireEvent(IMouseEventable eventableObject) {
				eventableObject.mouseOut();
			}
		},
		/**
		 * This is fired when a mouse is clicked down, and is in the dimensions
		 * of the registered eventable object
		 */
		MOUSE_FOCUS {
			@Override
			public void fireEvent(IMouseEventable eventableObject) {
				eventableObject.mousePressed();
			}
		},
		/**
		 * This is fired when an object which originally gained MOUSE_FOCUS (ie
		 * mouse clicked on the object), and then loses focus by the mouse then
		 * being clicked elsewhere
		 */
		MOUSE_BLUR {
			@Override
			public void fireEvent(IMouseEventable eventableObject) {
				eventableObject.mouseBlur();
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
		 *            The object which will receive the events
		 */
		public abstract void fireEvent(IMouseEventable eventableObject);
	}

	// ----------------------------------------------------------------
	// Constructor and Singleton methods
	// ----------------------------------------------------------------
	/**
	 * A private constructor for the singleton
	 */
	private MouseEventHandler() {
		// Instaniate the mouseListeningObjects, IE every
		registeredListeningObjects = new SynchronisedGameQueue<IMouseEventable>();

		// Create a new list for each type of event and add it to our map of
		// EventType -> list of waiting events
		waitingEventLists = Collections.synchronizedMap(new ConcurrentHashMap<MouseEventType, Collection<IMouseEventable>>());

		for (MouseEventType eventType : MouseEventType.values()) {
			waitingEventLists.put(eventType, Collections.synchronizedList(new ArrayList<IMouseEventable>()));
		}

		currentMouseOver = new SynchronisedGameQueue<IMouseEventable>();
		currentMouseFocus = new SynchronisedGameQueue<IMouseEventable>();
	}

	/**
	 * stores the instance, only when accessed will it be be instantiated
	 */
	private static class NewGameEventHandlerInstance {
		private static MouseEventHandler instance = new MouseEventHandler();
	}

	/**
	 * Gets the singleton instance of the GameAssetManager. This is lazily
	 * instantiated through the use of the private static class
	 * NewGameEventHandlerInstance
	 * 
	 * @return NewGameEventHandler's instance
	 */
	public static MouseEventHandler getInstance() {
		return NewGameEventHandlerInstance.instance;
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
	public void registerEvents(IMouseEventable mouseEventable) {
		registeredListeningObjects.add(mouseEventable);
	}

	/**
	 * Removes the eventable object from receiving any fired events. This
	 * removal will happen at during the start of the next logical update
	 * 
	 * @param mouseEventable
	 *            The object that wants to unregister for events
	 */
	public void unregisterEvents(IMouseEventable mouseEventable) {
		registeredListeningObjects.remove(mouseEventable);
	}

	/**
	 * Removes all eventable objects from the event system entirely. This will
	 * happen during the next logical update.
	 */
	public void removeAllEvents() {
		registeredListeningObjects.clear();
	}

	// ----------------------------------------------------------------
	// ILogical interface implementations
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
		updateLists();
	}

	/**
	 * Updates all of the lists required. In this update we will release any
	 * objects that didn't want to be registered with the system, and release
	 * any events which were fired etc
	 */
	private void updateLists() {
		/*
		 * Synchronise and call the update methods of our lists which will
		 * handle the moving of waiting objects to be added to the
		 * currentObjects list, and for all deletedObjects to be removed.
		 */
		synchronized (registeredListeningObjects) {
			registeredListeningObjects.updateLists();
		}

		synchronized (currentMouseOver) {
			currentMouseOver.updateLists();
		}

		synchronized (currentMouseFocus) {
			currentMouseFocus.updateLists();
		}
	}

	/**
	 * Fire all of the events within the waiting list. The waiting list is for
	 * objects that wanted their events only fired during logical updates.
	 */
	public void fireWaitingEvents() {
		synchronized (waitingEventLists) {
			for (Entry<MouseEventType, Collection<IMouseEventable>> kvp : waitingEventLists.entrySet()) {
				for (IMouseEventable eventableObject : kvp.getValue()) {
					kvp.getKey().fireEvent(eventableObject);
				}
			}

			// Clear all of the waiting event lists as we have just fired them
			for (Collection<IMouseEventable> eventHolderList : waitingEventLists.values()) {
				eventHolderList.clear();
			}
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
		synchronized (registeredListeningObjects) {
			registeredListeningObjects.clear();
		}
		synchronized (currentMouseOver) {
			currentMouseOver.clear();
		}
		synchronized (waitingEventLists) {
			for (Collection<IMouseEventable> list : waitingEventLists.values()) {
				list.clear();
			}
		}
	}

	// ----------------------------------------------------------------
	// Mouse interface implementations
	// ----------------------------------------------------------------
	@Override
	public synchronized void mouseMoved(MouseEvent e) {
		Point mousePoint = e.getPoint();
		// Test all objects to see if the mouse position is within the bounds of
		// the eventable object. If it is, add their event to the
		// waitingEventsMouseOver list. We must also add it to the list of
		// currentMouseOver, so that we can fire the mouse out events when the
		// mouse is outside of the dimensions of the eventable object
		synchronized (registeredListeningObjects) {
			for (IMouseEventable eventable : registeredListeningObjects) {
				// If the mouse is within the dimensions of the object, and if
				// the
				// object hasn't already received its mouse over event before.
				// And isEventsDisabled is called to make sure that it actually
				// wants to recieve the events still
				if (eventable.getDimensions().contains(mousePoint)
						&& !currentMouseOver.contains(eventable)
						&& !eventable.isEventsDisabled()) {
					synchronized (waitingEventLists) {
						waitingEventLists.get(MouseEventType.MOUSE_OVER).add(eventable);
					}
					synchronized (currentMouseOver) {
						currentMouseOver.add(eventable);
					}
				}
			}
		}

		synchronized (currentMouseOver) {
			for (IMouseEventable eventable : currentMouseOver) {
				if (!eventable.getDimensions().contains(mousePoint)) {
					synchronized (waitingEventLists) {
						waitingEventLists.get(MouseEventType.MOUSE_OUT).add(eventable);
					}
					synchronized (currentMouseOver) {
						currentMouseOver.remove(eventable);
					}
				}
			}
		}

		// Update the latest position of the mouse moved x,y
		mousePosX = e.getX();
		mousePosY = e.getY();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Iterate through all of the objects that are registered with the
		// system and check if the mouse has been clicked within the bounds of
		// the IMouseEventable
		synchronized (registeredListeningObjects) {
			for (IMouseEventable eventable : registeredListeningObjects) {
				// Check if the mouse is within the bounds of the object
				// Also check that the object is wanting to recieve the events
				if (eventable.getDimensions().contains(e.getPoint())
						&& !eventable.isEventsDisabled()) {
					synchronized (waitingEventLists) {
						waitingEventLists.get(MouseEventType.MOUSE_FOCUS).add(eventable);
					}
					synchronized (currentMouseFocus) {
						currentMouseFocus.add(eventable);
					}
				}
			}
		}

		// When a mouse is clicked we will iterate over the list of objects
		// which were already clicked, if we find an object in which the mouse
		// position is no longer within the bounds of we will fire the
		// MOUSE_BLUR event
		for (IMouseEventable eventable : currentMouseFocus) {
			if (!eventable.getDimensions().contains(e.getPoint())) {
				synchronized (waitingEventLists) {
					waitingEventLists.get(MouseEventType.MOUSE_BLUR).add(eventable);
				}
				synchronized (currentMouseFocus) {
					currentMouseFocus.remove(eventable);
				}
			}
		}

		leftClickDown = e.getButton() == MouseEvent.BUTTON1;
		rightClickDown = e.getButton() == MouseEvent.BUTTON3;

		mouseClickPosX = e.getX();
		mouseClickPosY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public final void mouseDragged(MouseEvent e) {
		// update the current mouse position
		mousePosX = e.getX();
		mousePosY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// XAND our left click down and right click down with the button that
		// fired to check if our mouse is still down or not
		leftClickDown ^= e.getButton() == MouseEvent.BUTTON1;
		rightClickDown ^= e.getButton() == MouseEvent.BUTTON3;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}