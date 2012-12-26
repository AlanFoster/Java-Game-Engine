package eventhandling;

import engine.interfaces.INameable;
import engine.interfaces.IPositionable;

/**
 * In order to register with the MouseEventHandler objects must implement this
 * interface to be able to register. To register with the event system they must
 * do so explicitly by calling the methods * To add/remove from this list you
 * should be calling the methods registerEvents() or removeEvents() as required.
 * <p>
 * Remember that this interface also extends IPositionable and INameable. The
 * positional interface is used to check if the mouse point intersects the
 * boundaries of an object, and the INameable interface a requirement of the
 * GameEngine, and is really only for debugging. IE INameable isn't used at all
 * within the MouseEventHandler.
 * <p>
 * This class borrows the event notation from JavaScript (IE onmousefocus etc)
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see MouseEventHandler
 */
public interface IMouseEventable extends IPositionable, INameable {
	/**
	 * Fired When the mouse enters the dimension of an object, this is only
	 * fired once. IE, if a mouse is over an object for more than 1 game update,
	 * it will only be fired once and not every time until the user moves their
	 * mouse out
	 */
	void mouseOver();
	/**
	 * Fired When the mouse LEAVES the dimension of an object, this is only
	 * fired once. IE, if a the mouse entered a game object, and left the game
	 * object it is only fired on the one game loop update
	 */
	void mouseOut();
	/**
	 * This is fired when a mouse is clicked down, and is in the dimensions of
	 * the registered eventable object
	 */
	void mousePressed();
	/**
	 * This is fired when an object which originally gained MOUSE_FOCUS (ie
	 * mouse clicked on the object), and then loses focus by the mouse then
	 * being clicked elsewhere
	 */
	void mouseBlur();

	/**
	 * Decides whether or not this IMouseEventable object still wants to receive
	 * any mouse events still.
	 * 
	 * @return True if the IMouseEventable object doesn't need to receive
	 *         events. False if the IMouseEventable object still wants to
	 *         receive any events still.
	 */
	boolean isEventsDisabled();
}
