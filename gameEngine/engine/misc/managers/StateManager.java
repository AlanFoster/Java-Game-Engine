package engine.misc.managers;

import java.util.HashMap;
import java.util.Map;

import engine.interfaces.ILogical;
import engine.interfaces.INameable;
import engine.main.GameTime;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import engine.misc.State;

/**
 * This class offers a 'Finite State Machine', which stores all possible states
 * within it. A starting state is given to the StateManager which it will update
 * within each game loop. This State that it continually updates will offer a
 * new transition name, which can either be itself or another state if required.
 * This is technically not a 'true' finite state machine, as it allows for many
 * states to be added as time, IE it is not a fixed size.
 * <p>
 * This state machine ensures that no two states have the same state name.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class StateManager implements ILogical, INameable {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(StateManager.class);

	/**
	 * The name of this state machine, used with the {@link INameable} interface
	 * implementation
	 */
	private String name;

	/**
	 * Stores the list of all possible states that this finite state machine can
	 * access. The String is the name which identifies the state, which will be
	 * used when finding the transition state that a state offers.
	 */
	private Map<String, State> possibleStates;

	/**
	 * Stores the 'current' state during the logic update. This should not be
	 * set directly, and should only be used within logicUpdate. I have created
	 * this as a field simply for allocation.
	 */
	private State currentState;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------
	public StateManager(String name) {
		this.name = name;
		possibleStates = new HashMap<String, State>();
	}

	public StateManager(String name, Map<String, State> possibleStates) {
		this.name = name;
		this.possibleStates = possibleStates;
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * 
	 * @param state
	 *            The new state which can be added to the state machine
	 */
	public void addPossibleState(State state) {
		if (!possibleStates.containsKey(state.getName())) {
			possibleStates.put(state.getName(), state);
		} else {
			logger.error("State name ", state.getName(), " already existed within the state machine :: ", getName());
		}
	}

	/**
	 * Returns the current hash map of possible states
	 * 
	 * @return The possible states that this state machine maintains
	 */
	public Map<String, State> getPossibleStates() {
		return possibleStates;
	}

	/**
	 * 
	 * @param name
	 * @throws NullPointerException
	 *             If the required state name does not exist a null pointer
	 *             exception is thrown
	 */
	private void setState(String name) throws NullPointerException {
		if (possibleStates.containsKey(name)) {
			currentState = possibleStates.get(name);
		} else {
			throw new NullPointerException(Helpers.concat("Name ", name, " not found within states for the state manager ", getName()));
		}
	}

	/**
	 * Sets the current state within the state machine, and on the next logic
	 * update it will recieve the logic udpates required.
	 * 
	 * @param name
	 *            The name of the new state
	 */
	public void setStartingState(String name) {
		setState(name);
	}

	/**
	 * Return the name of this state manager
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * As mentioned within the header we will update the currentState, and we
	 * will transition to whatever state that it offers to us, even if it's the
	 * same state.
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		// Update the current state
		currentState.logicUpdate(gameTime);
		// Attempt to change the current state to the offered transition state
		// if it is not null, and it exists within the system
		if (currentState.getTransitionStateName() == null) {
			// This shouldn't happen as there is logic in place within State to
			// stop this, but just incase.
			logger.error("Current State ", currentState, " offered no transition state");
		} else {
			setState(currentState.getTransitionStateName());
		}
	}

	@Override
	public void startUp(GameTime gameTime) {
	}

	@Override
	public void cleanUp() {
		currentState = null;
	}
}
