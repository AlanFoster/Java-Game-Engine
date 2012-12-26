package engine.interfaces;

import java.awt.Graphics2D;

/**
 * All objects that wish to recieve draw requests on each logic update should
 * implement this interface. When registered with a game layer on each game
 * update all objects will recieve the graphics object and can draw directly to
 * it as required.
 * <p>
 * All drawing should take into consideration the parents game layer offset. For
 * instance this is the parent game layer offset. All objects that wish to draw
 * to the screen should take this x,y into consideration when attempting to draw
 * to the graphics2d object.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public interface IDrawable {
	/**
	 * Depending on the situation this method will either be called by the main
	 * {@link IRenderUpdateDelegator} if it sits at a higher level. This method
	 * however can go 'deeper' than that, for instance a gamelayer pass on the
	 * arguments and call any child {@link IDrawable} objects to allow for it to
	 * draw. Likewise, the child object itself may be represented by many other
	 * drawable objects so it too can pass the arguments on and call their child
	 * draw methods. Potentially it will look like a typical tree structure.
	 * 
	 * @param drawScreen
	 *            Direct access to the graphics2d object where all drawing
	 *            should appear
	 * @param offsetX
	 *            The x offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 * @param offsetY
	 *            The y offset that should be given to the object. For instance
	 *            this is the parent game layer offset. All objects that wish to
	 *            draw to the screen should take this x,y into consideration
	 *            when attempting to draw to the graphics2d object.
	 */
	void draw(Graphics2D drawScreen, int offsetX, int offsetY);
}
