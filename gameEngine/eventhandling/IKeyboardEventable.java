package eventhandling;

import java.awt.event.KeyEvent;

/**
 * For any object that wishes to listen and receive keyboard events during
 * logical updates, they should implement this interface. After implementing
 * this interface they must explicitly register with the LogicalKeyEventHandler
 * <p>
 * Note, this interface does not extend KeyListener as I didn't want to
 * implement all of the events the events, but it would be extremely easy to do
 * this if required due to the nice abstract enum implementation i have created
 * within the {@link KeyEventHandler}
 * 
 * 
 * @see KeyEventHandler
 * @see MouseEventHandler
 * @see IMouseEventable
 */
public interface IKeyboardEventable {
	/**
	 * Invoked when a key has been pressed. See the Java AWT class description
	 * for KeyEvent for a definition of a key pressed event.
	 * 
	 * @param e
	 *            The raw KeyEvent created by java AWT
	 *            http://docs.oracle.com/javase
	 *            /1.4.2/docs/api/java/awt/package-summary.html
	 */
	void keyPressed(KeyEvent e);
}
