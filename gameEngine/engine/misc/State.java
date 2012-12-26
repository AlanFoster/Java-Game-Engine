package engine.misc;

import engine.interfaces.ILogical;
import engine.interfaces.INameable;
import engine.main.GameTime;
import engine.misc.managers.StateManager;

/**
 * A state is a logical object which will recieve logical updates from the
 * {@link StateManager} that is is registered with. At the end of each logical
 * update it will offer a transition state based on any logic that it has
 * performed. This may or may not be the same state name, it does not matter.
 * <p>
 * This class is abstract as the logicUpdate implementation within
 * {@link ILogical}'s interface MUST be implemented.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public abstract class State implements ILogical, INameable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The state's name, used for debugging and when registering to the
	 * {@link StateManager} this will be the name that other states use to offer
	 * their transition state as
	 */
	private String name;
	/**
	 * Stores the transition state name, which will be set during the logic
	 * update. This may or may not be set to itself if required.
	 */
	private String nextStateName;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * Creates a new state object
	 * 
	 * @param name
	 *            This states name. This will be used within the
	 *            {@link StateManager} to identify states when transitioning to
	 *            the next state at the end of logical updates
	 */
	public State(String name) {
		this.name = name;
	}

	/**
	 * Sets the transition state name
	 * 
	 * @param name
	 *            The name of the state that the {@link StateManager} will
	 *            transition to next
	 */
	public void setTransitionStateName(String name) {
		nextStateName = name;
	}

	/**
	 * Get the name of the state that we wish to transition to
	 * 
	 * @return The name of the state that the {@link StateManager} will
	 *         transition to next
	 */
	public String getTransitionStateName() {
		return nextStateName;
	}

	/**
	 * Get the current state's name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * The {@link StateManager} will call this during each update, IF it is the
	 * currentState within the {@link StateManager}. This logic must be
	 * implemented and it is thus abstract. It is within this logic update that
	 * the state must offer the transition state required using
	 * setTransitionStateName();
	 */
	@Override
	public abstract void logicUpdate(GameTime gameTime);

	@Override
	public void startUp(GameTime gameTime) {
	}

	@Override
	public void cleanUp() {
	}
}
