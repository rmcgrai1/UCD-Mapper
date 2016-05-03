package gfx;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import com.jogamp.opengl.util.texture.Texture;
import cont.TextureController;

public class TextureExt {
	public final static byte E_NONE = -1, E_GRAYSCALE = 0, E_INVERT = 1, E_TWIRL = 2, E_CHROME = 3, E_CRYSTALLIZE = 4, E_POINTILLIZE = 5, E_BLUR = 6, E_OIL = 7, E_PIXELIZE = 8, E_RAINBOW = 9, E_UNDERWATER = 10, E_DIFFUSE = 11, E_SHIVER = 12, E_EDGE = 13, E_DISSOLVE = 14;
	
	private List<Texture> frameList = new ArrayList<Texture>();
	private int imageNumber = 0;
	
	public TextureExt(BufferedImage img) {
		addFrame(img);
		
	    TextureController.add(this);
	}	
	public TextureExt(List<BufferedImage> imgs) {
		
		for(BufferedImage i : imgs)
			addFrame(i);
		imageNumber = frameList.size();
		
	    TextureController.add(this);
	}
	
	public void destroy() {
		TextureController.remove(this);
		for(Texture t : frameList)
			TextureController.destroy(t);
		
		frameList.clear();
	}
	
	public int getImageNumber() {
		return imageNumber;
	}
	
	public void addFrame(BufferedImage img) {
		frameList.add(GOGL.createTexture(img, false));
		imageNumber++;
	}
	
	public Texture getFrame(int imageIndex) {
		return frameList.get(imageIndex % imageNumber);
	}
	
	public Texture getFrame(double imageIndex) {
		return getFrame((int) Math.floor(imageIndex));
	}


	public int getWidth() {
		return getWidth(0);
	}
	public int getWidth(int index) {
		return frameList.get(index).getWidth();
	}
	
	public int getHeight() {
		return getHeight(0);
	}
	public int getHeight(int index) {
		return frameList.get(index).getHeight();
	}
}
