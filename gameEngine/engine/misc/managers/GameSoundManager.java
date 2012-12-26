package engine.misc.managers;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

import engine.assets.GameSound;
import engine.assets.factories.GameSoundFactory;
import engine.misc.GameLogging;

/**
 * This GameSoundManager should be used when attemping to play any type of
 * sound. All Sounds that are managed by this {@link GameSoundManager} will
 * automatically have the masterVolume applied to it. For instance if a sound
 * plays, it will be take into consideration the overall system's volume.
 * <p>
 * Note :: This is a sound manager, and does not perform any loading of
 * GameSounds. Instead this should be done through the {@link GameAssetManager}
 * and {@link GameSoundFactory} instead.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see GameSound
 */
public class GameSoundManager implements LineListener {
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
	private final static GameLogging logger = new GameLogging(GameSoundManager.class);

	/**
	 * Stores all of the sounds that this sound manager is currently playing
	 */
	private List<Clip> managingSounds;
	private float masterVolume = 60;

	private FloatControl volumeControl;

	private static GameSoundManager instance;

	private GameSoundManager() {
		managingSounds = new ArrayList<Clip>();
		volumeControl = getVolumeControl();
	}

	// stores the instance, only when accessed will it be be instantiated
	private static class GameSoundManagerInstance {
		private static GameSoundManager instance = new GameSoundManager();
	}

	// singleton instance
	public static GameSoundManager getInstance() {
		return GameSoundManagerInstance.instance;
	}

	public void setMasterVolume(float volume) {
		// I know I that this may be 'overkill', but, i'd rather make the point
		// in the logs that the volume control wasn't found for volume many
		// times, to make it easier to find if it's gone wrong
		if (volumeControl == null)
			logger.error(new NullPointerException(), "Volume mixer null");
		if (volume < 0 || volume > 1)
			logger.error(new IllegalArgumentException(), "Percentage must be between 0 and 100");

		masterVolume = volume;

		// Open the port
		volumeControl.setValue(volume);

		logger.info("changed volume to ", volume);
	}

	private final FloatControl getVolumeControl() {
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		Port.Info speakerInfo = Port.Info.SPEAKER;

		mixerInfoLoop: for (Mixer.Info info : mixerInfo) {
			Mixer mixer = AudioSystem.getMixer(info);
			if (AudioSystem.getMixer(info).isLineSupported(speakerInfo)) {
				Port volumePort = null;
				try {
					volumePort = (Port) mixer.getLine(speakerInfo);
					volumePort.open();
				} catch (LineUnavailableException e) {
					logger.error(e, "Failed getting volume mixer");
				}
				if (volumePort != null && volumePort.isOpen() && volumePort.isControlSupported(FloatControl.Type.VOLUME)) {
					return (FloatControl) volumePort.getControl(FloatControl.Type.VOLUME);
				}
			}
		}

		logger.error("No volume mixer found for volume control");
		return null;
	}

	public void playSound(GameSound sound) {
		managingSounds.add(sound.clip);
		sound.addLineListener(this);
		sound.start();
	}

	public void stopAllSounds() {
		for (Clip sound : managingSounds) {
			sound.stop();
		}
	}

	@Override
	public void update(LineEvent e) {
		logger.info(managingSounds.size());
		logger.info(e.getSource());
		if (e.getType().equals(LineEvent.Type.STOP)) {
			managingSounds.remove(e.getSource());
			logger.info(managingSounds.size());
		}
	}
}
