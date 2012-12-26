package entitysystems.components;

import engine.misc.Helpers;
import entitysystem.core.IComponent;
import entitysystem.systems.RenderSystem;

public class Spatial implements IComponent {
	/**
	 * Stores the previous spatial details. This will most likely be used a lot
	 * for caching purposes, and perhaps collision detection. For instance if
	 * the previous spatial is equal to this 'current' spacial, then we can save
	 * a lot of time by not doing the same functions.
	 * 
	 * Current this is used within the render system, as setting affine
	 * transformations on each frame is costly we can check if the
	 * previousSpatial's rotation is equal to this 'current' rotation, and if it
	 * is the same we don't need to change the cached image
	 * 
	 * @see RenderSystem
	 * @see Draw
	 */
	public Spatial previousSpatial;

	public boolean spatialChanged;
	public boolean previousSpatialWasNull;
	
	//public boolean previouslyNullSpatial;
	
	public float x, y;
	public float width, height;

	/**
	 * rotation is stored in radians. This is a private field so that it can
	 * only be accessed by the method setRotation which will make sure it's both
	 * a positive radian and <2pi
	 */
	private double rotation;



	public Spatial(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Spatial(float x, float y, float width, float height, double radians) {
		this(x, y, width, height);
		setRotation(radians);
	}

	/**
	 * Set the rotation of this component. This rotation will be measured in
	 * radians. This method will also make the rotation a positive number
	 * between 0 and 2PI
	 * 
	 * @param r
	 */
	public void setRotation(double r) {
		rotation = Helpers.correctRadian(r);
	}

	public double getRotation() {
		return rotation;
	}

	/**
	 * Increases the current rotation by degrees instead of the radians method
	 * that 'setRotation' provides
	 * 
	 * @param degree
	 */
	public void incrementDegrees(int degree) {
		setRotation(rotation + Math.toRadians(degree));
	}

	public float getRight(){
		return x + width;
	}
	
	public float getBottom(){
		return y + height;
	}
	
	public boolean intersects(Spatial s){
		if(!(s == null || s.width <= 0 || s.height <= 0)) {
			return s.getRight() > this.x
			&& s.getBottom() > this.y
			&& s.x < this.getRight()
			&& s.y < this.getBottom();
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o != null && o instanceof Spatial))
			return false;

		Spatial s = (Spatial) o;
		return s.x == x && s.y == y && s.height == height && s.width == width && s.rotation == rotation;
	}

	@Override
	public String toString() {
		return Helpers.concat("[Spatial component, x : ", x, " y : ", y,
				" width :: ", width, " height :: ", height,
				" rotation :: ", rotation, "]");
	}
}