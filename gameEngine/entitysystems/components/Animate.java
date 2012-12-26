package entitysystems.components;

import java.awt.image.BufferedImage;

import engine.assets.GameGraphic;
import engine.misc.GameLogging;
import entitysystem.core.IComponent;
import entitysystem.systems.AnimateSystem;

/**
 * Contains the data used for the AnimateSystem. Please see the AnimateSystem
 * for furthed details on how this is expected to work.
 * 
 * <p>
 * Please remember that if an Animate component is given to an entity it MUST
 * also contain a draw component.
 * 
 * @see AnimateSystem
 */
public class Animate implements IComponent {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	final static GameLogging logger = new GameLogging(Animate.class);

	/**
	 * The 2d array of possible images that this animation will contain. On each
	 * logical update if the required time has passed for the next image, this
	 * is where it will get it from.
	 */
	public BufferedImage[] children;

	/**
	 * THe current index of the image that is be shown within the 2d array of
	 * GameGraphic
	 */
	public int index;

	/**
	 * The total amount of elapsed time that the image has been shown for. On
	 * each logical update this will be reduced by the timePerImage if it has
	 * been surpassed so that it can keep track of when to show the next
	 * animation
	 */
	public long totalTimeShown;
	/**
	 * The amount of time in milliseconds that the image should show for before
	 * moving to the next image
	 */
	public long timePerImage;

	/**
	 * The desired amount of times to loop for. If this is -1 then it will loop
	 * infinitively, and 0 will not loop
	 */
	public int desiredLoopAmount;
	/**
	 * Keeps track of the current amount of times the animation has looped for,
	 * when this equals the desiredLoopCount the animation will stop.
	 */
	public int currentLoopCount = -1;
	/**
	 * If the currentLoopCount equals to that of the desired loop count and this
	 * is set to true then the animation will remove itself from the entity
	 * manager. However if thisi s false it will still exist within the screen.
	 */
	public boolean removeWhenDone;
	/**
	 * If true the and the animation reaches its list child image it will begin
	 * to count down indices back to zero. For instance when true the index
	 * values will be 0,1,2,3,4,3,2,1. Whilst if this is set to false it will be
	 * 0,1,2,3,4,0,1,2,3,4
	 */
	public boolean reverseWhenDone;
	/**
	 * A boolean flag to test whether or not we are current reversing, used in
	 * conjunction with reverseWhenDone
	 */
	public boolean isReversing;

	/**
	 * Create a new Animation component that will be implicitly registered with
	 * a system once added to an entity. Remember the Entity must also contain a
	 * draw component for this to work.
	 * 
	 * @param images
	 *            The list of images that this animation will transition to
	 * @param startingIndex
	 *            The first image that will be shown. This should be a number
	 *            between 0 and the number of total number of images within the
	 *            2d array passed in.
	 * @param loopCount
	 *            The total amount of times you want the animation to loop. If
	 *            you want a animation to loop for a finite number of times make
	 *            it positive. -1 is used to represent an infinite loop
	 * @param reverseWhenDone
	 *            When a loop has reached the end, IE starting index from 0 to
	 *            its nth iteration, it will increment the total loop count.
	 *            When this is set to true the next index will be n-1. When this
	 *            is set to false the next image index will be 0
	 * @param removeWhenDone
	 *            After the animation is completed we can either request for the
	 *            EntityManager to remove the entity that this component is
	 *            attached to or not.
	 * @param timePerImage
	 *            The total amount of time, <b>in milliseconds</b> that each
	 *            image will be displayed for before progressing to the next.
	 */
	public Animate(BufferedImage[] images,
			int startingIndex, int loopCount, boolean reverseWhenDone, boolean removeWhenDone, long timePerImage) {
		this.children = images;

		if (images == null) {
			logger.error(new NullPointerException(), "images given to Animation component were null");
		}

		if (startingIndex < 0 || startingIndex > children.length) {
			logger.error(new IllegalArgumentException(), "invalid index ", startingIndex, " in animate component, valid range is 0-", images.length);
		}

		this.index = startingIndex;

		// As when we either reach an index of 0 when reversing or the end we
		// consider that to be one 'loop' iteration, we must multiply it by two
		// if it expects to loop twice.
		desiredLoopAmount = reverseWhenDone ? loopCount * 2 : loopCount;
		this.reverseWhenDone = reverseWhenDone;
		this.removeWhenDone = removeWhenDone;
		this.timePerImage = timePerImage;
	}
}