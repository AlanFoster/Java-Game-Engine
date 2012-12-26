package entitysystems.components;

import java.awt.image.BufferedImage;

import engine.misc.Helpers;
import engine.misc.managers.GameAssetManager;
import entitysystem.core.IComponent;

public class Shield implements IComponent, IHUDable {
	public int reduceDamageAmount = 200;
	public int currentReduceDamageAmount = 100;

	public Shield(int damageReducingAmount) {
		this.currentReduceDamageAmount = this.reduceDamageAmount = damageReducingAmount;
	}

	// ----------------------------------------------------------------
	// IHUDable implementations
	// ----------------------------------------------------------------
	private static final BufferedImage hudGraphic = GameAssetManager.getInstance().getObject(BufferedImage.class, "shieldHud");

	@Override
	public BufferedImage getHUDIcon() {
		return hudGraphic;
	}

	@Override
	public String getHUDDetails() {
		return Helpers.concat("reduce damage :: \n", currentReduceDamageAmount, " / ", reduceDamageAmount);
	}
}