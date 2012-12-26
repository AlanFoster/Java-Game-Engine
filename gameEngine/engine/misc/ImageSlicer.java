package engine.misc;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * Takes an image which is split into sections with the color of
 * 'imageDividingColour' and returns an array of BufferedImage. This class is
 * considered more of a helper method, so should not be directly instantiated
 * and should only be accessed through the static method calls
 * <p>
 * In order for an image to be sliced by this ImageSlicer a raw image must be
 * passed to it, in which each individual sprite that you wish to be sliced are
 * seperated by a solid 1px line equal to the color of the private field
 * 'imageDividingColour'. This slicing will occur on a top down row basis. If
 * there is a need for a 'dead' cell (where 'cell' is defined as a rectangle
 * area outlined by our diving color) we can use the color of that cell to be
 * equal to the private field 'emptyCellColor'. If the image does not have any
 * dividing borders it will simply return the original buffered image
 * <p>
 * This class will be useful for slicing fonts into an array of buffered images,
 * animations, sprites etc.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class ImageSlicer {
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(ImageSlicer.class);
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * The colour that seperates each 'game graphic' within the buffered image.
	 * This is where the slicing will happen within the method
	 * getImageAsGraphicArray.
	 */
	private static final int imageDividingColour = 0xED1C24;
	/**
	 * This colour should be present when a cell is considered 'dead'. If you do
	 * not wish for an image to be added to the list of graphics, then use this
	 * colour within the cell and when splicing it will skip that area of the
	 * image and progress onto the next.
	 */
	private static final int emptyCellColor = 0xCCCCCC;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	/**
	 * This method is private as this class should not be instantiated
	 */
	private ImageSlicer() {
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * <p>
	 * Division of images is done by the colour 0xED1C24. If a cell contains
	 * #CCCCCC, then it isn't generated a graphic, as it assumed that cell is
	 * dead.
	 * </p>
	 * <p>
	 * <b>It is also assumed that the first cell of each row will be the height
	 * of the remaining cells within that row.</b> There is no attempt to
	 * recalculate the heights of each cell dynamically, once a height is found
	 * for the first image, that is the height for the rest of the row. It is
	 * /very/ easy to change this if it is problematic however. This height
	 * value will be recalculated on each new row however, so as a 'quick fix'
	 * if you wished to be clever and use new rows in conjunction with the
	 * 'dead' cells, this won't be a problem if there are singular unique
	 * heights.
	 * </p>
	 * <p>
	 * Images will be inserted into the array in a column/row basis, ie <br>
	 * |0|1|2|<br>
	 * |3|4 _|<br>
	 * |5____|
	 * </p>
	 * 
	 * @param rawImage
	 *            The original bufferedImage which is needed to be sliced
	 * @return An array of the individual images which can be of any width and
	 *         height
	 */
	public static BufferedImage[] getAsSlicedImageArray(BufferedImage rawImage) {
		LinkedList<BufferedImage> bufferedImageList = new LinkedList<BufferedImage>();

		// Loop through the image and find an x pixel that matches the
		// colour
		for (int width = 0, height = 0, startX = 0, startY = 0, currentX = 0, color = 0; startY < rawImage.getHeight(); currentX++) {
			try {
				color = rawImage.getRGB(currentX, startY);
				// Begin to slice the image if the dividing colour is found
				// Or its the end of the image
				if (((color ^ 0xFF << 24) == imageDividingColour) || currentX + 1 >= rawImage.getWidth()) {
					// If we have no sequence height for this row , work it
					// out
					if (height == 0) {
						for (int y = startY; y < rawImage.getHeight(); y++) {
							// If we find pink crossbar colour, set the
							// height variable
							if ((rawImage.getRGB(currentX - 1, y) ^ 0xFF << 24) == imageDividingColour) {
								height = y - startY;
								break;
							}
						}
						// If no crossing line was found, assume that the
						// rest of the image is the height
						if (height == 0) {
							height = rawImage.getHeight() - startY;
						}
					}
					// Only cut this image into a sequence if it's not a
					// dead cell
					if ((rawImage.getRGB(startX + 1, startY) ^ 255 << 24) != emptyCellColor) {
						width = currentX - startX;
						bufferedImageList.add(rawImage.getSubimage(startX, startY, width, height));
					}
					// We increment start X by one, so that the dividing
					// line doesn't show when cropped
					startX = currentX + 1;
				}

				// If we've reached the end of the image in terms of width,
				// restart the values back to the 'start' of the image,
				// but move down the image to cut the next line
				if (currentX + 1 >= rawImage.getWidth()) {
					currentX = startX = 0;
					startY += height + 1;
					height = 0;
				}
			} catch (Exception e) {
				logger.error(Helpers.concat("died gettingImageAs strip\n",
						" x : ", currentX, " y : ", startY,
						" width : ", width, " height : ", height));
			}
		}

		// Return the array of buffered images
		return bufferedImageList.toArray(new BufferedImage[bufferedImageList.size()]);
	}
}
