package entitysystems.components;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import entitysystem.core.Entity;
import entitysystem.core.IComponent;
import entitysystem.systems.ParticleEmitterSystem;

/**
 * 
 * @author alan
 * @see ParticleEmitterSystem
 */
public class ParticleEmitter implements IComponent {
	public BufferedImage[] particleGraphics;
	public double startAngle, endAngle;
	public int minimumLife, maximumLife;
	public int[] possibleColors;

	public Queue<Entity> currentParticles;
	public Queue<Entity> deadParticles;

	public BufferedImage[] cachedParticleImages;

	public int minimumParticles;
	public int maximumParticles;
	public int maximumTotalParticles;

	public int minimumAcceleration;
	public int maximumAcceleration;

	/**
	 * 
	 * @param startAngle
	 * @param endAngle
	 * @param minmumLife
	 * @param maximumLife
	 * @param possibleColors
	 * @param minimumParticles
	 *            The minimum number of particles that can be generated at once
	 * @param maximumParticles
	 *            THe maximum amount of particles that can be generated at once
	 * @param maximumTotalParticles
	 *            the TOTAL maximum number of particles. This is the maximum
	 *            number that will ever be created
	 * @param minimumAcceleration
	 * @param maximumAcceleration
	 * @param particleGraphics
	 */
	public ParticleEmitter(double startAngle, double endAngle,
			int minmumLife, int maximumLife,
			int[] possibleColors,
			int minimumParticles, int maximumParticles,
			int maximumTotalParticles,
			int minimumAcceleration, int maximumAcceleration,
			BufferedImage... particleGraphics) {
		this.particleGraphics = particleGraphics;
		this.startAngle = startAngle;
		this.endAngle = endAngle;
		this.minimumLife = minmumLife;
		this.maximumLife = maximumLife;
		this.possibleColors = possibleColors;

		this.minimumParticles = minimumParticles;
		this.maximumParticles = maximumParticles;
		this.maximumTotalParticles = maximumTotalParticles;

		currentParticles = new LinkedList<Entity>();
		deadParticles = new LinkedList<Entity>();

		cachedParticleImages = new BufferedImage[possibleColors.length * particleGraphics.length];
	}

	public String toString() {
		return "[particle emitter]";
	}
}