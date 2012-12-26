package engine.assets.factories;

import java.awt.image.BufferedImage;
import java.util.Map;

import engine.misc.ImageSlicer;
import engine.misc.managers.GameAssetManager;

public class SlicedBufferedImageFactory extends AssetFactory<BufferedImage[]> {
	
	@Override
	public BufferedImage[] getObject(String name, Map<String, String> data) {
		if(cache.containsKey(name)){
			return cache.get(name);
		}

		BufferedImage rawImage = GameAssetManager.getInstance().getObject(BufferedImage.class, name);
		BufferedImage[] slicedImages = ImageSlicer.getAsSlicedImageArray(rawImage);
		cache.put(name, slicedImages);
		return slicedImages;
	}

	@Override
	public BufferedImage[] getClonedObject(String name, Map<String, String> data) {
		BufferedImage[] oldImages = getObject(name, data);
		BufferedImage[] newImages = new BufferedImage[oldImages.length];
		for(int i = 0; i < oldImages.length; i++){
			newImages[i] = oldImages[i].getSubimage(0, 0, oldImages[i].getWidth(), oldImages[i].getHeight());
		}
		return newImages;
	}
}
