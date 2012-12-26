package engine.interfaces;

import java.util.Observer;

import engine.misc.GameObservervable;

/**
 * Implement this interface if a class contains a {@link GameObservervable}
 * object. See the notes on {@link GameObservervable} for more information
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public interface IObservable {
	void addObserver(Observer o);
	void deleteObserver(Observer o);
}
