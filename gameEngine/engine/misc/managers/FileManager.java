package engine.misc.managers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import engine.misc.GameLogging;

/**
 * This class provides basic reading/writing of files. This class currently does
 * not take advantage of the ARM (Automatic Resource Management) offered by Java
 * 1.7. I'd like to change this when I have the time to do so, but it's not
 * exactly important, since i've written its now.
 * <p>
 * Edit :: On the above, sadly I can't use java 1.7 anymore :sadpanda: When this
 * GameEngine is re-released as open source it will conform to java 1.7 however
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class FileManager {
	private final static GameLogging logger = new GameLogging(FileManager.class);

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	/**
	 * Reads the contents of a file and returns the contents of the file.
	 * 
	 * @param path
	 *            The path to the required file. This should relational to the
	 *            project, ie /files/foo.txt
	 * @return Returns the required file contents as a single string
	 */
	public final static String getFileContents(String path) {
		StringBuffer stringBuffer = new StringBuffer();
		InputStreamReader inputStream = null;
		
		try {
			String nextLine;
			inputStream = new InputStreamReader(FileManager.class.getResourceAsStream(path));
			BufferedReader bufferReader = new BufferedReader(inputStream);

			while ((nextLine = bufferReader.readLine()) != null)
				stringBuffer.append(nextLine);

		} catch (IOException e) {
			logger.error(e, "failed loading file contents for :: ", path);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error(e, "Failed closing the inputStream within getFileContents");
				}
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * Saves the required string to the file. It will create this file if it
	 * does not exist, and <b>overwrites the file if it did previously
	 * exist.</b>
	 * 
	 * @param filePath
	 *            the relational path of the file
	 * @param data
	 *            the data as a string which needs to be saved to the file
	 */
	public final static void saveFileContents(String filePath, String data) {
		File file = new File(filePath);
		// If the file exists, delete it
		if (file.exists())
			file.delete();
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			file.createNewFile();
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(data);
		} catch (IOException e) {
			logger.error(e, "Error saving a file :: ", filePath, " with data :: ", data);
		} finally {
			// Try to close everything
			if (bufferedWriter != null)
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					logger.error(e, "Failed closing fileWriter/BufferedWriter");
				}
		}
	}

	/**
	 * Loads an image with the file path. Note, if you're wanting to actually
	 * load a BufferedImage into the game for use, I suggest you use the
	 * GameAssetManager to do so isntead. This method makes no attempt what so
	 * ever to cache any objects loaded.
	 * 
	 * @param fileName
	 * @return The loaded BufferedImage
	 */
	public final static BufferedImage loadImage(String filePath) {
		BufferedImage loadedImage = null;
		try {
			loadedImage = ImageIO.read(FileManager.class.getResource(filePath));
		} catch (IOException e) {
			logger.error("Failed loading an image :: ", filePath);
		}
		return loadedImage;
	}
}
