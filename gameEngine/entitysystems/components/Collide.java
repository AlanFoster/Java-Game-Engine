package entitysystems.components;

import java.util.ArrayList;
import java.util.Observable;

import engine.misc.Location;
import entitysystem.core.Entity;
import entitysystem.core.IComponent;

public class Collide extends Observable implements IComponent {
	/** A list entities that this entity entity collided with */
	public ArrayList<Entity> collisionList;

	/**
	 * Stores what bucket locations this entity is in, the collision system
	 * current populates this. The collsion detection system also uses this for
	 * debug drawing, HOWEVER, I could make a system dedicated to sorting things
	 * into buckets, so that I can cull things easily.
	 */
	public ArrayList<Location> bucketLocations;

	public Collide() {
		collisionList = new ArrayList<Entity>();
		bucketLocations = new ArrayList<Location>();
	}

	public void addCollidedWith(Entity collidedEntity) {
		collisionList.add(collidedEntity);

		// Tell the listening systems that want details from THIS
		// entity/component
		setChanged();
		notifyObservers();
	}

	public void clearDetails() {
		collisionList.clear();
		bucketLocations.clear();
		// tested = false;
	}

	public void addBucketLocation(Location location) {
		bucketLocations.add(location);
	}

}