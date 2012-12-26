package engine.interfaces;

/**
 * This interface should be implemented if an object will require to be
 * explicitly removed from a system. Currently only the GameLayer makes use of
 * this interface. The current 
 */
public interface IRemovable extends INameable {
	boolean needsRemoved();
	void removeMe();
}
