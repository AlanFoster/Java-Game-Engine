package engine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.assets.GameGraphic;
import engine.assets.factories.GameFontFactory;
import engine.components.GameGraphicComponent;
import engine.interfaces.ICloneable;
import engine.main.GameEngine;
import engine.misc.GameLogging;
import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;

/**
 * Creates a new 'GameFont' object. A GameFont stores internally an array of
 * BufferedImages which will represent each character and the list of
 * availableCharacters that link up with the BufferedImages.
 * <p>
 * When the setText method is called it will create the new image that this
 * GameFont object represents, and it is only here that this buffered image is
 * changed, for performance reasons it is not best to continually perform
 * computationally expensive operations.
 * <p>
 * This class provides support for font alignment, {@link Alignment}, and
 * provides new line character support ie "foo\nbar" will be shown as
 * 
 * <pre>
 * foo
 * bar
 * </pre>
 * 
 * It is best to acquire a new GameFont object from the GameAssetManager, which
 * will call the GameFontFactory, for caching and performance reasons.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameFontFactory
 * @see GameAssetManager
 * 
 */
public class GameFont extends GameGraphicComponent implements ICloneable<GameFont> {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameGraphicComponent.class);

	/**
	 * The current alignment values that the GameFont can be under. This will be
	 * calculated automatically within the setText method. This also takes into
	 * consideration new lines, IE if MIDDLE_ALIGN is selected and there are
	 * many new lines in the text, they will all be centered.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 * 
	 */
	public static enum Alignment {
		LEFT_ALIGN, MIDDLE_ALIGN, RIGHT_ALIGN
	};

	Alignment currentAlignment = Alignment.LEFT_ALIGN;

	/**
	 * The current text being represented by this font
	 */
	private String text;

	/**
	 * Set to false when the game font is not required to be drawn. For instance
	 * this may be set when setText is set to an empty string, or when it is
	 * explicitly told - if a need arises for an ignored draw, then accessor
	 * methods should be created, or set the access type to protected/public
	 */
	private boolean drawRequired = true;

	/**
	 * The list of available characters for the given font For instance
	 * "ABCDEFabcdef1234"
	 */
	private String availableChar;

	/**
	 * Font Kerning. This is the font distance between each character within a
	 * string. This number can be negitive if required, a negitive number is
	 * particularly useful for fonts which have a lot of 'distortion' around
	 * them.
	 * 
	 * http://en.wikipedia.org/wiki/Kerning
	 */
	private int kerning;

	/**
	 * The distance of gap when the character is a space, ' '. Basically this is
	 * the size of gap between each word
	 * 
	 * http://en.wikipedia.org/wiki/Letter-spacing
	 */
	private int letterSpacing = 10;

	/**
	 * When a string contains \n is will go onto a new line. This value is the
	 * distance between the bottom of the graphics of the first line, to the top
	 * of the graphics on the new line.
	 */
	int verticalSpacing = 5;

	/**
	 * Each individual character image that this GameFont has access to.
	 */
	private BufferedImage[] splitCharGraphicList;

	/**
	 * 
	 * @param name
	 *            The asset name of the GameFont
	 * @param charGraphics
	 *            The character graphics, IE different images representing each
	 *            of the characters
	 * @param availableChar
	 *            The String of available characters
	 */
	public GameFont(String name, BufferedImage[] charGraphics, String availableChar) {
		super(name);

		splitCharGraphicList = charGraphics;
		this.availableChar = availableChar;
	}

	/**
	 * Returns a shallow clone of the GameFont object which will share the same
	 * graphical character set
	 * 
	 * @param gameFont
	 *            The game font to clone
	 */
	public GameFont(GameFont gameFont) {
		this(gameFont.getName(), gameFont.splitCharGraphicList, gameFont.availableChar);
	}

	/**
	 * This is the 'heart' of the GameFont object. It is here that the GameFont
	 * object can change its text. This is the only time that the image of the
	 * font will be created. We do this for caching and performance reasons.
	 * <p>
	 * This method will iterate through the string, taking into consideration
	 * any new lines ie \n, and will automatically set the width and height of
	 * this object so that external systems may perform any further position
	 * calculations on the object.
	 * <p>
	 * It is possible to split this method into a few different methods, but at
	 * the time I didn't really see any situation in which this method might be
	 * overridden as it is generic enough, thus the reason for it being final.
	 * 
	 * @param text
	 *            The new text that this GameFont object should represent
	 */
	public final void setText(String text) {
		this.text = text;

		if (text.length() == 0) {
			setWidth(0);
			setHeight(0);
			drawRequired = false;
			return;
		}

		drawRequired = true;

		// to find the max width/height we're going to have to iterate twice..
		String[] rows = text.split("\\n");
		RowData[] rowData = new RowData[rows.length];
		int i = 0;
		for (String s : rows) {
			rowData[i++] = calculateRowData(s);
		}

		// Get maximum width row, and total height for all rows
		int maximumWidthRow = rowData[0].totalWidth;
		int totalHeightRow = 0;

		for (RowData row : rowData) {
			if (row.totalWidth >= maximumWidthRow) {
				maximumWidthRow = row.totalWidth;
			}
			totalHeightRow += row.maxHeight + verticalSpacing;
		}

		// Factor in the row spacing into the totalHeight
		// totalHeightRow += verticalSpacing * (rows.length - 1);

		// Create the image
		BufferedImage fontDrawingImage = new BufferedImage(maximumWidthRow, totalHeightRow, BufferedImage.TYPE_INT_ARGB);
		Graphics2D fontGraphic = fontDrawingImage.createGraphics();

		// Draw the font

		// The Y drawing position. This is used when a piece of text contains
		// multiple rows, when \n is present. On each iteration of a row, this
		// is incremented by the previous row's height and vertical spacing so
		// that it goes underneath the previous row when drawing
		int drawingPosY = 0;

		for (RowData row : rowData) {
			int alignmentOffset = 0;
			// Decide where to place this row
			switch (currentAlignment) {
				case LEFT_ALIGN:
					alignmentOffset = 0;
					break;
				case MIDDLE_ALIGN:
					alignmentOffset = maximumWidthRow / 2 - row.totalWidth / 2;
					break;
				case RIGHT_ALIGN:
					alignmentOffset = maximumWidthRow - row.totalWidth;
					break;
			}

			int charOffsetPosX = 0;
			for (int pos : row.charIndicePositions) {
				// If the char was found within the avalable chars, draw it
				if (pos > -1) {
					fontGraphic.drawImage(splitCharGraphicList[pos], alignmentOffset + charOffsetPosX, drawingPosY, null);
					charOffsetPosX += (int) (splitCharGraphicList[pos].getWidth() + kerning);
				} else {
					// Otherwise leave a space
					charOffsetPosX += letterSpacing;
				}
			}

			// Increase the row drawing position to the current row's height,
			// plus the vertical spacing
			drawingPosY += row.maxHeight + verticalSpacing;
		}

		// Set these values so that other objects that may encapsulate GameFont
		// know exactly what the total width/height is. Possibly for alignment.
		setWidth(maximumWidthRow);
		setHeight(totalHeightRow);
		setCurrentGraphic(new GameGraphic(Helpers.concat(getName(), "-", text), fontDrawingImage));
	}

	/**
	 * Changes the alignment of the game font object, ie center alignment. This
	 * change will happen automatically.
	 * 
	 * @param alignment
	 *            The new alignment that this font should be under
	 */
	public void setAlignment(Alignment alignment) {
		currentAlignment = alignment;
		refreshTextRendering();
	}

	/**
	 * Sets the kerning of the font. This is the font distance between each
	 * character within a string. This number can be negitive if required, a
	 * negitive number is particularly useful for fonts which have a lot of
	 * 'distortion' around them.
	 * 
	 * @param charSpacing
	 *            The new charSpacing that will be given to each character
	 */
	public void setCharSpacing(int charSpacing) {
		this.kerning = charSpacing;
	}

	/**
	 * Sets the distanec between each veritcal row. IE if there are many \n
	 * within the string it will add this distance between the top and bottom of
	 * the rows.
	 * 
	 * @param verticalSpacing
	 *            The new vertical spaciong between each row
	 */
	public void setVerticalSpacing(int verticalSpacing) {
		this.verticalSpacing = verticalSpacing;
	}

	/**
	 * A method for seeing if this GameFont object can represent the required
	 * character
	 * 
	 * @param ch
	 *            Thew character which we are requesting if it representable by
	 *            this GameFont object
	 * @return True if the this GameFont can possibly represent this character,
	 *         false if this GameFont object can not represent the required
	 *         character
	 */
	public boolean containsChar(char ch) {
		return availableChar.indexOf(ch) > -1;
	}

	/**
	 * Should be called when a setting changes how the font will look. For
	 * instance if there is a change made to the alignment, char spacing,
	 * vertical spacing etc, we will need to redraw everything again alignment,
	 * spacing etc
	 */
	private void refreshTextRendering() {
		setText(getText());
	}

	/**
	 * @return Returns the current text that this GameFont is representing
	 */
	public String getText() {
		return text;
	}

	/**
	 * Return the size of gap between each word
	 * 
	 * @return The distance of gap when the character is a space, ' '.
	 * 
	 */
	public int getLetterSpacing() {
		return letterSpacing;
	}

	/**
	 * Draws the font object if there is a drawRequired. drawRequired will be
	 * set to false when the game font is not required to be drawn. For instance
	 * this may be set when setText is set to an empty string, or when it is
	 * explicitly told - if a need arises for an ignored draw
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
	@Override
	public void draw(Graphics2D drawScreen, int offsetX, int offsetY) {
		if (drawRequired) {
			getCurrentGraphic().draw(drawScreen, (int) (offsetX + getX()), (int) (offsetY + getY()));
		}
	}

	/**
	 * Calculates the information required about a row of text. A row of text is
	 * a new line within the text object..
	 * 
	 * @param s
	 *            The string of the row
	 * @return the rowData object which will contain the row's width, maximum
	 *         height and indice positions. See the classRowData for further
	 *         details
	 */
	private final RowData calculateRowData(String s) {
		int i = -1;
		int indicePos = 0;
		int[] indicePositions = new int[s.length()];
		int totalWidth = 0;
		int maxHeight = 0;
		for (char c : s.toCharArray()) {
			i++;
			indicePos = availableChar.indexOf(c);
			// if it wasn't found within the char array
			if (indicePos == -1) {
				// There won't be a graphical representation for a space
				// So if it isn't a space, flag it up as an error
				if (c != ' ') {
					logger.error("invalid char :: '", c, "' within string ", s);
				}
				// If space or key not found, just leave a space for it
				// increment total width, and continue
				indicePositions[i] = -1;
				totalWidth += letterSpacing;
				continue;
			}
			indicePositions[i] = indicePos;
			totalWidth += splitCharGraphicList[indicePos].getWidth() + kerning;

			if (splitCharGraphicList[indicePos].getHeight() > maxHeight) {
				maxHeight = (int) splitCharGraphicList[indicePos].getHeight();
			}
		}
		return new RowData(totalWidth, maxHeight, indicePositions);
	}

	/**
	 * A private static innner class which is used to store information about a
	 * 'row' within a new text string. This class stores the width and height of
	 * the row, and the 'indicePositions' which is the index of each character
	 * within the available characters so that we can recreate the string that
	 * this row represents based on charAt.
	 * 
	 * @author Alan Foster
	 * @version 1.0
	 */
	private static class RowData {
		/**
		 * The total width of the row object
		 */
		public int totalWidth;
		/**
		 * The single largest character within the row.
		 */
		public int maxHeight;
		/**
		 * The integer positions of this string that we set. This stores the
		 * index of the character within our available character string so that
		 * we can iterate over it and use charAt to get the actual character
		 * represented.
		 */
		public int[] charIndicePositions;

		RowData(int totalWidth, int maxHeight, int[] indicePositions) {
			this.totalWidth = totalWidth;
			this.maxHeight = maxHeight;
			this.charIndicePositions = indicePositions;
		}
	}

	/**
	 * Return a deep clone of the object that implements this class. This object
	 * will be of type T instead of being an object, so there will not be a need
	 * for an explicit type cast to the same object type.
	 * 
	 * This game font will share the same graphic image
	 */
	@Override
	public GameFont getShallowClone() {
		return new GameFont(this);
	}
}
