package hud;

import java.awt.Graphics2D;

import src.Configuration;
import engine.gui.GameFont;
import engine.main.GameTime;
import engine.misc.Timer;
import engine.misc.managers.GameAssetManager;

/**
 * <p>
 * This class provides a generic way of creating a HUDTimer. By this I mean a
 * counter which will show and update on the screen. Remember that as this
 * extends HUDItem, if you wish to change the 'drawing location', then you
 * should set the field of type DrawRelativeToViewport, with the field name of
 * drawLocation to be what you want it. This item should be explicitly added to
 * the HUDSystem still, as with all of the HUDItems.
 * <p>
 * However this class is instantiated, either through anonymous classes or
 * through extending this class, please note that the method timeOver is called
 * when the timer has reached zero. For further details see this method's
 * header.
 * <p>
 * This class contains its own font object to draw to the screen, it will also
 * take care of 'caching' the text, so that on every update it is not recreating
 * the same text, as that would be computationally expensive. At the heart of
 * this 'caching' it relies on the encapsulated CountdowmTimer field, which is
 * offered by the GameEngine misc folder.
 * <p>
 * I believe a cool way of instantiating this class would be to do something
 * like : <blockquote>
 * 
 * <pre>
 * <code>
 *  hudSystem.add(new HUDTimer(){
 *     // Initialisation block to override the default draw position
 *     {
 *         drawLocation = DrawRelativeToViewport.CENTER;
 *         countDownTimer = new CountdownTimer(1337);
 *         // ... etc
 *     }
 * 	
 * 		// This gets called when the timer hits 0		
 * 	  &#64;Override
 *     public void timeOver() {
 *          System.out.println("time up!");
 *          // Do some logic
 *      }
 * });
 * </code>
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public abstract class HUDTimer extends HudItem {
	/**
	 * used as a simple count down timer. It will receive an initial time in
	 * milliseconds to count down from, and this time can be reduced by calling
	 * the method decreaseTimer(), which will be called during each logic update
	 */
	protected Timer countDownTimer;

	/**
	 * This boolean is used to decide whether or not the timer should be drawn
	 * to the screen or not. When this is true the current game text will be set
	 * the the timer's value. When it is set to false the game text given to the
	 * object will be drawn instead of the timer.
	 */
	protected boolean showTimer = true;

	/**
	 * The GameFont which is used to draw onto the screen the current time. By
	 * default it uses the Configuration's default HUD font
	 */
	protected GameFont drawnText = GameAssetManager.getInstance().getClonedObject(GameFont.class, Configuration.GUI.Fonts.HUD);
	/**
	 * The default X,Y width height values of a HUD timer object, these will be
	 * used within the constructor in which no x,y, width height is specified.
	 */
	private final static int defaultX = 0, defaultY = 0, defaultWidth = 200, defaultHeight = 50;

	// ----------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------

	/**
	 * Creates a new hud timer with a the count down timer shown as the text.
	 * And with a specified x,y, width and height
	 * 
	 * @param x
	 *            The X location from the top left
	 * @param y
	 *            The Y location from the top left
	 * @param width
	 *            The width
	 * @param height
	 *            The hegiht
	 */
	public HUDTimer(int x, int y, int width, int height) {
		super(x, y, width, height);
		countDownTimer = new Timer(-1);
		drawnText.setText(countDownTimer.getAsString());
	}

	/**
	 * Creates a new hud timer with a the count down timer shown as the text.
	 * And with the default X,Y width and height. This constructor will display
	 * the count down message as normal.
	 */
	public HUDTimer() {
		this(defaultX, defaultY, defaultWidth, defaultHeight);
	}

	/**
	 * Creates a timed HUD message with the specified string. This string will
	 * overwrite the shown timer so that it no longer shows the count down time,
	 * but instead the message that we require
	 * <p>
	 * This method uses the default x,y width and height values
	 * 
	 * @param text
	 *            The test which will be shown by this HUD Message
	 */
	public HUDTimer(String text) {
		this(defaultX, defaultY, defaultWidth, defaultHeight);
		showTimer = false;
		drawnText.setText(text);
	}

	/**
	 * Creates a timed HUD message with the specified string. This string will
	 * overwrite the shown timer so that it no longer shows the count down time,
	 * but instead the message that we require
	 * <p>
	 * This method uses the default x,y width and height values
	 * <p>
	 * This method
	 * 
	 * @param text
	 *            The test which will be shown by this HUD Message
	 * @param drawLocation
	 *            The relative draw location that the message will be drawn at.
	 *            For instance it can be specified as the center if required.
	 *            {@link DrawRelativeToViewport}
	 * @param duration
	 *            The time in milliseconds that the displayed message will be
	 *            shown for
	 * 
	 */
	public HUDTimer(String message, DrawRelativeToViewport drawLocation, int duration) {
		this(defaultX, defaultY, defaultWidth, defaultHeight);
		countDownTimer = new Timer(duration);
		this.drawLocation = drawLocation;

		showTimer = false;
		drawnText.setText(message);
	}

	/**
	 * The offsetX and offsetY for this argument will be given by the HUDSystem.
	 * This offsetX and offsetY will be determined by the RelativeDraw. For
	 * instance if the RelativeDraw enum is set to TOP_LEFT then the offsetX and
	 * offsetY will both be 0. However if it is BOTTOM_RIGHT then the HUDSystem
	 * will calculate the location of the bottom right hand side of the viewport
	 * and take into consideration this HudItem's width and height so that the
	 * HudItem will draw in the correct place, relative to the bottom right.
	 * 
	 * This means that there should be a level of consideration for keeping the
	 * Spatial field up to date
	 */
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		drawnText.draw(drawScreen, offsetX, offsetY);
	}

	/**
	 * This method will be called when an object is first coming into
	 * realization.
	 * <p>
	 * In this method we set the spatial width/height to the drawn text
	 * width/height
	 */
	@Override
	public void startUp(GameTime gameTime) {
		// Remember to update our spatial, so that the hudsystem can
		// calculate where to put us in terms of relativity
		spatial.width = drawnText.getWidth();
		spatial.height = drawnText.getHeight();
	}

	/**
	 * 
	 * @return The current gamefont that is associated with this game timer.
	 *         This is the font that is used for the drawing delegation for the
	 *         hud
	 */
	public GameFont getTextFont() {
		return drawnText;
	}

	/**
	 * This method will be called during the update cycle of the game loop.
	 * <p>
	 * During this logic update we will decrease teh game timer and decide
	 * whether or not to call the timeOver method when the time has run out.
	 */
	@Override
	public void logicUpdate(GameTime gameTime) {
		// Decrease the timer by the current game time in milliseconds
		countDownTimer.decreaseTimer(gameTime.getElapsedTimeMilli());

		// If our timer is now over, time to call our timeOver method. The class
		// that instantiates this will have to provide that method for
		// themselves.
		if (countDownTimer.isFinished()) {
			timeOver();
		}

		if (countDownTimer.hasChanged && showTimer) {
			// Update the text as required
			drawnText.setText(countDownTimer.getAsString());
			// Note, at this point we DO NOT update the spatial.width when the
			// text as changed. The reason for this is, if it is aligned
			// relatively right to the viewport.. Then it will move slightly to
			// the left and right as the size of digits change, which would look
			// odd
		}
	}

	/**
	 * This method will be called when the time has reached zero. Possible
	 * suggestions for what to put in this method include things like Level
	 * timers, so that the game ends when the time is over. This could also be
	 * used to show time till next wave etc. It's pretty flexible. This could
	 * also have been rewritten using the observer class, but it's just another
	 * way of doing the same thing really.
	 */
	public abstract void timeOver();

	/**
	 * This clean up method will be called when it is removed from the logic
	 * system. Potentailly this will be called when timeOver() is called (IE,
	 * when the timer has ran out. But this depends on the logic implemented by
	 * the extending classes of course)
	 */
	@Override
	public void cleanUp() {
		countDownTimer = null;
		drawnText = null;
	}
}