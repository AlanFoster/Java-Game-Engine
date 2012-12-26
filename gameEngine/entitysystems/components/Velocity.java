package entitysystems.components;

import entitysystem.core.IComponent;
import entitysystem.systems.VelocitySystem;

/**
 * This Velocity component is handed by the Velocity system. All entities that
 * have this will possess an acceleration, a current velocity and a velocity
 * cap.
 * 
 * @see VelocitySystem
 * 
 */
public class Velocity implements IComponent {
	// ----------------------------------------------------------------
	// Velocity States
	// ----------------------------------------------------------------
	/**
	 * Stores the current VelocityStates that this component is under. It is
	 * used by the {@link VelocitySystem}
	 * 
	 * @see VelocitySystem
	 */
	public enum VelocityState {
		/**
		 * accelerate : Attempt to reach our maximum velocity on each update by
		 * the VelocitySystem
		 */
		Accelerate,
		/**
		 * Apply negitive acceleration and attempt to reach our maximum negitive
		 * velocity on each update by the velocity system
		 */
		Reverse,
		/**
		 * Apply the breaks
		 */
		Break,
		/**
		 * if our velocity is either positive or negitive, let friction bring us
		 * back to a halt. If our velocity is not positive or negitive, do
		 * nothing.
		 */
		Idle
	}

	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The maximum velocity that this component can reach
	 */
	public float velocityCap;
	/**
	 * The acceleration, this is added to the current velocity on each update
	 * providing that velocityCap isn't exceeded, in which case the
	 * currentVelocity will equal velocityCap
	 */
	public float acceleration;
	/**
	 * The current velocity which is used to calculate the new position
	 */
	public float currentVelocity;
	/**
	 * The force which acts against our acceleration, so that eventually we will
	 * halt when there is no acceleration anymore
	 */
	public float friction = 0.9f;

	/**
	 * The current state. By changing this state the {@link VelocitySystem} will
	 * apply the required logic to the entity that posses this component
	 */
	public VelocityState currentState = Velocity.VelocityState.Idle;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * 
	 * @param velocityCap
	 *            The maximum velocity that this component can be
	 * @param acceleration
	 *            The acceleration that this component can achieve
	 */
	public Velocity(float velocityCap, float acceleration) {
		this.velocityCap = velocityCap;
		this.acceleration = acceleration;
	}
	/**
	 * 
	 * @param velocityCap
	 *            The maximum velocity that this component can be
	 * @param acceleration
	 *            The acceleration that this component can achieve
	 * @param friction
	 *            The amount of 'resistance' given to the object each game loop.
	 *            This will decrease from the current velocity
	 */
	public Velocity(float velocityCap, float acceleration, float friction) {
		this(velocityCap, acceleration);
		this.friction = friction;
	}

	/**
	 * 
	 * @param velocityCap
	 *            The maximum velocity that this component can be
	 * @param acceleration
	 *            The acceleration that this component can achieve
	 * @param friction
	 *            The amount of 'resistance' given to the object each game loop.
	 *            This will decrease from the current velocity
	 * @param startingVelocity
	 *            The initial velocity that this component will start off with
	 */
	public Velocity(float velocityCap, float acceleration, float friction, float startingVelocity) {
		this(velocityCap, acceleration);
		currentVelocity = startingVelocity;
		this.friction = friction;
		currentState = Velocity.VelocityState.Accelerate;
	}
}