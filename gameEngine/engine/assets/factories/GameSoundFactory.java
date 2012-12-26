package engine.assets.factories;

import java.util.Map;

import engine.assets.GameSound;

/**
 * Factory for sound assets. This class does not offer references sadly.
 * <P>
 * Note :: This class was not finished to the same standard as the rest, due to
 * the advice given within week 9. However, it should be noted sound does indeed
 * work.
 * 
 * @author Alan Foster
 * @version 1.0
 */
public class GameSoundFactory extends AssetFactory<GameSound> {

	@Override
	public GameSound getObject(String name, Map<String, String> data) {
		// no cache or reference copy yet
		return new GameSound(name, data.get("fileLocation"));
	}

	@Override
	public GameSound getClonedObject(String name, Map<String, String> data) {
		return getObject(name, data);
	}
}
