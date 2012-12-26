package entitysystems.components;

import java.util.ArrayList;
import java.util.List;

import engine.misc.Helpers;
import entitysystem.core.Entity;
import entitysystem.core.IComponent;
import entitysystem.systems.AISystem;

/**
 * An AI component that is given to an entity which will be automatically
 * controlled by the computer. As with all components this component contains no
 * logic, and is simply a pure way of storing information about a component. The
 * actual logical processing is done by the system {@link AISystem}
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class AI implements IComponent {
	/**
	 * The states that an AI component can be under
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	public enum AIState {
		ROAM, SEEK, FLEE, ARRIVE;
	}

	/**
	 * The possible states that this AI component is allowed to be under. For
	 * instance ROAM and SEEK.
	 */
	public List<AIState> possibleStates;
	/**
	 * Stores the current AI state that this entity is under
	 */
	public AIState currentState;
	/**
	 * The tag name that this AI component will seek. IE if this is set to
	 * 'player', and any entity with the tag entity that matches will cause us
	 * to change to seeking mode and seek that entity.
	 */
	public String seekTagName;

	/**
	 * The minimum distance that the entity with the seekTagName added to them
	 * that this entity will change states. This is public and can be set
	 * directly.
	 */
	public int seekingDistance = 500;
	/**
	 * Near distance is the point at which we are considered to be 'near' to the
	 * entity that we are seeking. And if we are seeking, and we are within this
	 * distance between the entity, we move to the ARRIVE state. This is public
	 * and can be set directly.
	 */
	public int nearDistance = 300;

	/**
	 * The target that we are either roaming or seeking towards
	 */
	public Spatial targetSpatial;

	/**
	 * The target Entity that we are either are seeking towards
	 */
	public Entity targetEntity;

	/**
	 * The maximum rotation that this AI component can turn. This is in radians.
	 * The default is 2 degrees.
	 */
	public double maxTurningDistance = Math.toRadians(2);
	/**
	 * A counter to denote when a new roaming target should be generated. This
	 * is incremented each game loop update.
	 **/
	public int roamCounter;

	/**
	 * @param seekTagName
	 *            The tagname is the tag attached to an entity that it will
	 *            possibly follow. In this scenario the tagname will most likely
	 *            be 'player', and when an entity with the tag of 'player' is
	 *            close, it will move towards it.
	 * @param startingState
	 *            The initial state that this AI component will start on
	 * @param possibleStates
	 *            The possible states that this AI entity may possibly go into.
	 *            For instance ROAM and SEEK.
	 */
	public AI(String seekTagName, AIState startingState, AIState... possibleStates) {
		this.possibleStates = new ArrayList<AIState>(possibleStates.length);
		for (AIState possibleState : possibleStates) {
			this.possibleStates.add(possibleState);
		}

		this.seekTagName = seekTagName;
		this.currentState = startingState;
	}

	@Override
	public String toString() {
		return Helpers.concat("[AI Component : {currentState : ",
				currentState, ", possibleStates : ", possibleStates, "]");
	}
}