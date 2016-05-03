package cont;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import fl.FileExt;

public final class ImageLoader {
		
	private ImageLoader() {}	
	public static BufferedImage load(String fileName) throws IOException {	
		return ImageIO.read(FileExt.get(fileName));
	}
}
