package entitysystems.components;

import entitysystem.core.IComponent;


public class TimedDelete implements IComponent {
	// the time in milliseconds that this component will survive for before
	// being deleted by the entity system
	public int lifeDuration;

	public TimedDelete(int duration) {
		this.lifeDuration = duration;
	}
}