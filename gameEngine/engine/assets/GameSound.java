package engine.assets;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import engine.misc.GameLogging;
import engine.misc.managers.GameAssetManager;

/**
 * This class offers the ability to store a GameSound which can be played. It is
 * suggested this class should not be instantiated directly, but instead assets
 * should be loaded through the {@link GameAssetManager}.
 * <p>
 * * Note :: This class was not finished to the same standard as the rest, due
 * to the advice given within week 9. However, it should be noted sound does
 * indeed work.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 */
public class GameSound extends Asset<GameSound> implements LineListener {
	// ----------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------
	/**
	 * For any logging actions required they should be outputted through this
	 * logging object rather than through something crude like
	 * System.out.println(). This logger object offers basic logging levels such
	 * as info, debug, error.
	 * 
	 * @see GameLogging
	 */
	private final static GameLogging logger = new GameLogging(GameSound.class);

	/**
	 * The url location that this game clip has been played from
	 */
	private URL location;

	/**
	 * The loaded clip that this GameSound represents
	 */
	public Clip clip;

	// ----------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------
	public GameSound(String name, String path) {
		super(name);

		InputStream clipStream = GameSound.class.getResourceAsStream(path);
		logger.info(clipStream);
		clip = loadAudioClip(clipStream);

		// FloatControl master_gain = (FloatControl)
		// clip.getControl(FloatControl.Type.MASTER_GAIN);
		// master_gain.setValue(0f);

		// updateClipVolume(clip);
		// logger.info(clip.getControls()[0]);

		// Initialise all of the fields
		masterGainSupported = clip.isControlSupported(FloatControl.Type.MASTER_GAIN);
		panSupported = clip.isControlSupported(FloatControl.Type.PAN);

		if (masterGainSupported) {
			masterGainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			minimumVolume = masterGainControl.getMinimum();
			volumeRange = masterGainControl.getMaximum() - minimumVolume;
		}

		if (panSupported) {
			panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
		}

		// Start the volume at 0.6f for some reason
		setVolume(1f);
	}

	// ----------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------
	public void setVolume(float vol) {
		if (vol < 0 || vol > 1)
			logger.error(new IllegalArgumentException(),
					"Can't set to invalid volume, ", vol, " for ", this);

		masterGain = vol;
		if (masterGainSupported) {
			float newGainValue = minimumVolume + (volumeRange * masterGain);
			masterGainControl.setValue(newGainValue);
		}
	}

	public float getVolume() {
		return masterGain;
	}

	/**
	 * Type of sound file supported is WAV only
	 * 
	 * @param file
	 * @return
	 */
	// TODO should this be in soundfactory/manager ?
	public Clip loadAudioClip(InputStream file) {
		Clip playableClip = null;

		AudioInputStream inputStream;

		try {
			inputStream = AudioSystem.getAudioInputStream(file);

			DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());

			playableClip = (Clip) AudioSystem.getLine(info);
			playableClip.addLineListener(this);
			playableClip.open(inputStream);
			playableClip.setFramePosition(0);
			inputStream.close();

			// This catch used to be a lovely multi catch statement.
			// Pretend it's still there.
		} catch (UnsupportedAudioFileException e) {
			logger.info(e, "died within loadAudioClip UnsupportedAudioFileException");
		} catch (IOException e) {
			logger.info(e, "died within loadAudioClip IOException");
		} catch (LineUnavailableException e) {
			logger.info(e, "died within loadAudioClip LineUnavailableException");
		}

		return playableClip;
	}

	/**
	 * 
	 * @param pan
	 */
	public void setPan(int pan) {
		if (clip.isControlSupported(FloatControl.Type.PAN)) {
			FloatControl panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
			panControl.setValue(pan);
		}
	}

	public void addLineListener(LineListener listener) {
		clip.addLineListener(listener);
	}

	public void update() {
		// FloatControl pan = (FloatControl)
		// clip.getControl(FloatControl.Type.PAN);
		// pan.setValue(increment);

		// updateClipVolume(clip);
	}

	public void start() {
		clip.start();
	}

	public void stop() {
		clip.stop();
	}

	@Override
	public void update(LineEvent e) {

	}

	@Override
	public GameSound getShallowClone() {
		return null;
	}

	// ----------------------------------------------------------------
	// Cached private fields
	// ----------------------------------------------------------------
	/**
	 * The minimum volume that the masterGainControl can be for this sound
	 */
	private float minimumVolume;

	/**
	 * 
	 */
	private float volumeRange;

	private float masterGain;

	private boolean masterGainSupported;
	private boolean panSupported;

	private FloatControl masterGainControl;
	private FloatControl panControl;
}
