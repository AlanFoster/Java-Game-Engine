package entitysystems.components;

import entitysystem.core.IComponent;
import entitysystem.systems.ParticleEmitterSystem;

/**
 * A generic particle which is instantiated by the {@link ParticleEmitterSystem}
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class Particle implements IComponent {
	/**
	 * The amount of time this particle is going to stay alive. During each
	 * logic update this is decreased, and once it hits zero it will be
	 * considered dead. This is in milliseconds.
	 */
	public int timeAlive;

	/**
	 * Creates a new particle with a life time of 0
	 */
	public Particle() {
	}
	
	/**
	 * Creates a new particle with the required particle life time.
	 * 
	 * @param particleLife
	 *            The life time that this particle will be alive for in
	 *            milliseconds.
	 */
	public Particle(int particleLife) {
		this.timeAlive = particleLife;
	}

	@Override
	public String toString() {
		return "[particle]";
	}
}