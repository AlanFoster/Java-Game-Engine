package engine.interfaces;

import java.awt.Graphics2D;

import engine.main.GameTime;

/**
 * <p>
 * This interface should be implemented if a class wishes to interact with the
 * game engine in such a way that it will pass down the messages of logical
 * updates and rendering to registered children for example. I have created this
 * interface so that the game engine have such a burden removed from it, which
 * will allow for great flexibility.
 * </p>
 * <p>
 * The main advantage of this interface is the ability to swap which class is
 * going to handle the delegation of rendering and updates, for instance it is
 * most likely that the GameScreenManager will handle such tasks, but it also
 * allows for there to be a single GameScreen which will handle this, or even a
 * single GameLayer.
 * </p>
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see ILogical
 * @see IDrawable
 */
public interface IRenderUpdateDelegator {
	/**
	 * Called during the startGame() method of the game engine. It is expected
	 * that during this method any validation checks will be performed. For
	 * instance in the case of the GameScreenManager it ensures that there is a
	 * waiting GameScreen before progressing.
	 */
	void startUp();

	/**
	 * This method will be called during the update/render cycle. During this
	 * cycle the GameTime object will be passed around which offers methods to
	 * get the elapsed update time for instance
	 * 
	 * @param time
	 * @see GameTime
	 */
	void logicUpdate(GameTime time);

	/**
	 * Called during the render section of the render/update cycle
	 * 
	 * @param drawScreen
	 */
	void draw(Graphics2D drawScreen);
}
