package entitysystems.components;

import java.awt.image.BufferedImage;

import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.IComponent;

public class Score implements IComponent, IHUDable {
	public int totalScore;

	// ----------------------------------------------------------------
	// IHUDable implementations
	// ----------------------------------------------------------------
	private static final BufferedImage hudGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "scoreHud");

	@Override
	public BufferedImage getHUDIcon() {
		return hudGraphic;
	}

	@Override
	public String getHUDDetails() {
		return Helpers.concat("score :: \n", totalScore);
	}
}