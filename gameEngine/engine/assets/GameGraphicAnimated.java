package engine.assets;

import java.awt.Graphics2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import engine.components.GameGraphicComponent;
import engine.interfaces.IDrawable;
import engine.interfaces.IDrawableLogical;
import engine.main.GameEngine;
import engine.main.GameTime;
import engine.misc.GameLogging;
import entitysystem.systems.AnimateSystem;
import entitysystems.components.Animate;

//---------------------------------------------------------------------------------
// NOTE !! :: READ ME
//---------------------------------------------------------------------------------
/**
 * The code here is 'old', and is really part of the entity System now. See the
 * below list of 'see' classes instead. The below code code works still, but
 * i've 'rewritten' it within the entity system, but I decided not to delete it
 * from here. But the new updated code and comments are essentially within the
 * below systems instead, so ignore this class in terms of comments etc.
 * 
 * @see AnimateSystem
 * @see Animate
 * @see Draw
 */
// ---------------------------------------------------------------------------------
// NOTE !! :: READ ME
// ---------------------------------------------------------------------------------
public class GameGraphicAnimated extends GameGraphicComponent implements IDrawableLogical {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameGraphicAnimated.class);

	BufferedImage[] children;

	private int index;

	private long totalTimeShown;
	// In milliseconds
	private long timePerImage;

	// if -1 then infinite loop! 0 for no loop
	private int desiredLoopAmount;
	private int currentLoopCount;
	private boolean removeWhenDone;
	private boolean reverseWhenDone;
	private boolean isReversing;

	public GameGraphicAnimated(BufferedImage[] sprites, String name, int posX, int posY) {
		super(name, sprites[0], posX, posY);

		children = sprites;
		setCurrentGraphic(children[0]);
	}

	@Override
	public void startUp(GameTime gameTime) {
		// startingTime = time;
		index = 0;
		currentLoopCount = -1;
		desiredLoopAmount = -1;
		reverseWhenDone = true;
		removeWhenDone = false;
		timePerImage = 50;

		setCurrentGraphic(children[index]);
	}

	public void nextGraphic() {
		if (index < children.length && !isReversing) {
			setCurrentGraphic(children[index++]);
		} else if (index >= 0 && isReversing) {
			setCurrentGraphic(children[index--]);
		} else {
			logger.error(new ArrayIndexOutOfBoundsException(), "Animation index ", index,
					" out of bounds for element with ", children.length, " children.");
		}

		// If we've hit the bounds required, test for removal
		// Remember index++ will it make equal to children size if at the end..
		if ((index == 0 && isReversing) || (index == children.length && !isReversing)) {
			if (++currentLoopCount == desiredLoopAmount && removeWhenDone) {
				// needsRemoved();
			} else {
				index = reverseWhenDone ? children.length - 1 : 0;
				if (reverseWhenDone)
					isReversing = !isReversing;
			}
		}
	}

	@Override
	public void logicUpdate(GameTime gameTime) {
		totalTimeShown += gameTime.getElapsedTimeMilli();
		while (totalTimeShown > timePerImage) {
			totalTimeShown -= timePerImage;
			nextGraphic();
		}
	}

	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		getCurrentGraphic().draw(drawScreen, (int) (getX() + offsetX - (getCurrentGraphic().getWidth() / 2)), (int) (getY() + offsetY - (getCurrentGraphic().getHeight() / 2)));
	}
}
