package engine.misc;

import java.util.Observable;

import engine.interfaces.IObservable;

/**
 * The reason for creating this class is to allow for other classes to
 * encapsulate an Observable object. Due to Observable's "setChanged()" method
 * being protected we have to override and make it public... This is perfectly
 * acceptable to do this, and even the java docs even suggested this as a
 * possible method of encapsulating observable objects too.
 * <p>
 * I also would eventually like to roll my own Observable pattern, so that
 * instead of 1 argument of type Object. I could potentially have variadic
 * arguments of either type <T> or Object. Either or.
 * <P>
 * Encapsulation of this GameObservable object removes the need to extend
 * classes, or involve levels of unneeded OOP hierarchies. It is most likely
 * that the class that encapsulates this object will want to offer methods to
 * interact with it, which is why it is suggested that {@link IObservable} be
 * implemented
 * 
 * @see IObservable
 */
public class GameObservervable extends Observable {
	/**
	 * Override the setChanged of Observable (which is of protected type), with
	 * an access type of public, and call the super method within the class.
	 * This widens the scope to the class that encapsulates it, which is what we
	 * want. I don't see too many problems with having other classes being able
	 * to call this.
	 * 
	 * @see java.util.Observable#setChanged()
	 */
	@Override
	public void setChanged() {
		super.setChanged();
	}
}
