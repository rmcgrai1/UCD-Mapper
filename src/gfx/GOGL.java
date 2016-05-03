package gfx;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import learning.AlgorithmTester;
import learning.NodeSystem;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import time.Timer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import cont.Controller;
import cont.ImageLoader;
import cont.TextureController;
import datatypes.mat4;
import datatypes.vec;
import datatypes.vec3;
import functions.Math2D;

public final class GOGL {
	private static final float TOP_LAYER = 999;
	private static GLCanvas canv;
	private static GLEventListener listener;
	private static GLProfile gp = GLProfile.get(GLProfile.GL2);
	public static GL2 gl;
	public static GLCapabilities gcap;
	private static float[] viewPos;	
	private static NodeSystem nodeSystem;		
	public static final byte F_NEAREST = 0, F_BILINEAR = 1, F_TRILINEAR = 2;
	private static AlgorithmTester algorithmTester;	
	private static Timer time = new Timer(360);


    public static final int VIEW_FAR = 10000;
	public static final int 
		P_TRIANGLES = GL2.GL_TRIANGLES,
		P_TRIANGLE_STRIP = GL2.GL_TRIANGLE_STRIP,
		P_LINE_LOOP = GL2.GL_LINE_LOOP,
		P_LINES = GL2.GL_LINES;
			
	
	private static float screenScale = .6f;
	private static int 
		BORDER_LEFT = 3, // 3 is PERFECT but doesn't work, 2 works but isn't perfect
		BORDER_TOP = 25, // 25 is PERFECT but doesn't work, 24 works but isn't perfect
		SCREEN_WIDTH = (int) (632*screenScale), 
		SCREEN_HEIGHT = (int) (705*screenScale),
		WINDOW_WIDTH = SCREEN_WIDTH,
		WINDOW_HEIGHT = SCREEN_HEIGHT;
	private static RGBA drawingColor = new RGBA(1,1,1,1);
	private static float orthoLayer = 0;	
	private static mat4 modelMatrix = new mat4();
	
	
	public static void start3D(Controller Controller) {
		
		GLProfile.initSingleton();
	
	    canv = new GLCanvas();
	    	canv.setBackground(Color.WHITE);
	    	
	    listener =  new GLEventListener() {
            
            public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {}
            public void init( GLAutoDrawable glautodrawable ) {  	
            	
            	gl = glautodrawable.getGL().getGL2();
            	           		
	        	TextureController.ini();                
                viewPos = new float[4];
                float border = 150 + Math2D.calcLenY(getTime())*20;
                setViewPos(0,border,SCREEN_WIDTH-border,SCREEN_HEIGHT-border);                

                iniNode();
                
            	gl.glEnable(GL2.GL_DEPTH_TEST);
                gl.glEnable(GL2.GL_ALPHA_TEST);
                gl.glAlphaFunc(GL.GL_GREATER, 0);
            	
                gl.glEnable(GL2.GL_NORMALIZE);
            	            	            	
            	gcap = new GLCapabilities(gp);
            	gcap.setDepthBits(16);
            }
            public void dispose( GLAutoDrawable glautodrawable ) {}
            public void display( GLAutoDrawable glautodrawable ) { 
            	                			
            	time.check();
            	Timer.updateAll();

            	//Updatable.updateAll();
            	
            	Controller.updateInstance();
            	
            	gl = glautodrawable.getGL().getGL2();
            	
            	setViewport(0,0,WINDOW_WIDTH,WINDOW_HEIGHT);	
            	setOrtho();


            	GOGL.clear();
            	
        		GOGL.setColor(RGBA.WHITE);
            	nodeSystem.draw();
            	
            	// Run Trials
            	algorithmTester.run();
            	
            	// Draw Start & End Nodes
            	algorithmTester.draw();
        	}
        };
                
        canv.addGLEventListener(listener);
	}
	

	protected static void setWindowSize(int w, int h) {
		WINDOW_WIDTH = w;
		WINDOW_HEIGHT = h;
		Controller.getInstance().setSize(w,h+28);
		canv.setSize(w,h);
	}

	public static void setViewport(float x, float y, float w, float h) {
		y = WINDOW_HEIGHT-h-y;
		gl.glViewport((int)x, (int)y, (int)w, (int)h);
		
		viewPos[0] = x;
		viewPos[1] = y;
		viewPos[2] = w;
		viewPos[3] = h;
	}
		
	public static void disableBlending() {gl.glDisable(GL.GL_BLEND);}
	public static void enableBlending() {
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); 
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
	}

	public static void disableFog() {gl.glDisable(GL2.GL_FOG);}
	public static void enableFog(float start, float end, RGBA col) {
		
		gl.glEnable(GL2.GL_FOG);
		gl.glFogi(GL2.GL_FOG_COORD_SRC, GL2.GL_FRAGMENT_DEPTH);
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);

		gl.glFogfv(GL2.GL_FOG_COLOR, col.getRGBArray(), 0);
		
		gl.glFogf(GL2.GL_FOG_START, start);
		gl.glFogf(GL2.GL_FOG_END, end);
	}
			
	public static void repaint() {canv.display();}

	
	public static Texture createTexture(int width, int height) {
		return createTexture(new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB), true);
	}
	public static Texture createTexture(BufferedImage img, boolean mipmap) {
		if (img == null) 
			return null;
		
		//HORRIBLE BUG HERE
		Texture tex = (AWTTextureIO.newTexture(gp, img, mipmap));
		tex.setMustFlipVertically(false);
		
		TextureController.add(tex);
		
		return tex;
	}
	
	public static void unbind() {
		gl.glBindTexture(GL2.GL_TEXTURE_2D,0);
		gl.glDisable(GL2.GL_TEXTURE_2D);
	}
	
	public static void bind(Texture tex) {bind(tex.getTextureObject());}
	public static void bind(Texture tex, int target) {bind(tex.getTextureObject(),target);}
	public static void bind(int tex) {bind(tex, 0);}
	public static void bind(int tex, int target) {
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE0+target);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
	}
	public static void unbind(int target) {
		gl.glActiveTexture(GL2.GL_TEXTURE0+target);
		gl.glBindTexture(GL2.GL_TEXTURE_2D,0);
		gl.glDisable(GL2.GL_TEXTURE_2D);
	}
	
	public static void setViewPos(float x, float y, float w, float h) {
		viewPos[0] = x;
		viewPos[1] = y;
		viewPos[2] = w;
		viewPos[3] = h;
	}
	
	
	public static void setTextureFiltering(Texture tex, byte type) {
		bind(tex);
		switch(type) {
			case F_NEAREST:/* point sampling of nearest neighbor */
				gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
				gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
				break;
			case F_BILINEAR:/* bilinear interpolation */
				gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
				gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
				break;
			case F_TRILINEAR:/* trilinear interpolation on pyramid */
				gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
				gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
				break;
		}
		unbind();
	}
	
	public static void setOrthoLayer(float layer) {orthoLayer = layer;}
	public static float getOrthoLayer() {return orthoLayer;}
	
	public static void setOrtho() {setOrtho(TOP_LAYER);}
	public static void setOrtho(float w, float h) {setOrtho(w,h,TOP_LAYER);}
	public static void setOrtho(float useLayer) {setOrtho(viewPos[0],viewPos[1],viewPos[2],viewPos[3],useLayer);}
	public static void setOrtho(float w, float h, float useLayer) {
		setOrtho(viewPos[0],viewPos[1],w,h,useLayer);
	}
	public static void setOrtho(float x, float y, float w, float h, float useLayer) {
				
		orthoLayer = useLayer;
	
		gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrtho(x,w,h,y, -1000, 1000);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
		disableDepth();
	}
	
	
	
	// DRAWING FUNCTIONS

	public static void resetColor() {setColor(RGBA.WHITE);}
	
	public static RGBA getColor() {return new RGBA(drawingColor);}

	public static void setColor(float r, float g, float b) {setColor(r,g,b,1);}
	public static void setColor(RGBA col) {setColor(col.getR(),col.getG(),col.getB(),col.getA());}
	public static void setColor(float r, float g, float b, float a) {
		drawingColor.set(r,g,b,a);
		
		gl.glDisable(GL2.GL_COLOR_MATERIAL);
		gl.glColor4f(drawingColor.getR(),drawingColor.getG(),drawingColor.getB(),drawingColor.getA());
	}
	
	public static void drawTexture(float x, float y, TextureExt texExt) {drawTextureScaled(x,y,1,1,texExt);}
	public static void drawTextureScaled(float x, float y, float xS, float yS, TextureExt texExt) {drawTextureScaled(x,y,xS,yS,texExt.getFrame(0));}
	public static void drawTexture(float x, float y, Texture tex) {drawTexture(x,y,tex.getWidth(),tex.getHeight(),tex);}
	public static void drawTextureScaled(float x, float y, float xS, float yS, Texture tex) {drawTexture(x,y,tex.getWidth()*xS,tex.getHeight()*yS,tex);}
	public static void drawTexture(float x, float y, float w, float h, Texture tex) {
		drawTexture(x,y,w,h,tex,new float[] {0,0,1,1});
	}
	public static void drawTexture(float x, float y, float w, float h, Texture tex, float[] bounds) {
		if(tex == null)
			return;
		
		enableTextures();
		enableBlending();
		bind(tex);
		
		if(tex.getMustFlipVertically())
			fillRectangle(x,y+h,w,-h, bounds);
		else
			fillRectangle(x,y,w,h, bounds);
      	
      	bind(0);
      	disableTextures();
	}

	
	public static void drawTexture(float x, float y, float w, float h, MultiTexture tex, int frame) {
		drawTexture(x,y,w,h, tex.getTexture(), tex.getBounds(frame));
	}
	

	public static void drawPixel(float x, float y) {
		gl.glBegin(GL.GL_POINTS);
			gl.glVertex3f(x,y, orthoLayer);
	    gl.glEnd();
	}
	
	public static void setLineWidth(float w) {gl.glLineWidth(w);}
	public static float getLineWidth() {
		float[] w = new float[1];
		gl.glGetFloatv(GL.GL_LINE_WIDTH, w, 0);
		return w[0];
	}
	
	public static void drawLine(float x1, float y1, float x2, float y2) {drawLine(x1,y1,x2,y2, getLineWidth());};
	public static void drawLine(float x1, float y1, float x2, float y2, float w) {
		setLineWidth(w);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(x1,y1, orthoLayer);
			gl.glVertex3f(x2,y2, orthoLayer);
	    gl.glEnd();
	    setLineWidth(1);
	}

	public static void drawRectangle(float x, float y, float w, float h) {rectangle(x,y,w,h,false);}
	public static void drawRectangle(float x, float y, float w, float h, float[] bounds) {rectangle(x,y,w,h,bounds,false);}
	public static void fillRectangle(float x, float y, float w, float h) {rectangle(x,y,w,h,true);}
	public static void fillRectangle(float x, float y, float w, float h, float[] bounds) {rectangle(x,y,w,h,bounds,true);}
	public static void rectangle(float x, float y, float w, float h, boolean fill) {rectangle(x,y,w,h,new float[] {0,0,1,1},fill);}
	public static void rectangle(float x, float y, float w, float h, float[] bounds, boolean fill) {
		gl.glBegin((fill ? GL2.GL_QUADS : GL.GL_LINE_LOOP));
			gl.glTexCoord2d(bounds[0], bounds[1]);	gl.glVertex3f(x, y, orthoLayer);
			gl.glTexCoord2d(bounds[2], bounds[1]);	gl.glVertex3f(x+w, y, orthoLayer);
			gl.glTexCoord2d(bounds[2], bounds[3]);	gl.glVertex3f(x+w, y+h, orthoLayer);
			gl.glTexCoord2d(bounds[0], bounds[3]);	gl.glVertex3f(x,y+h, orthoLayer);		
        gl.glEnd();
	}

	public static void drawPolygon(vec vec, float r, int numPts) {drawPolygon(vec.get(0), vec.get(1), r, numPts);}
	public static void drawPolygon(float x, float y, float r, int numPts) {polygon(x,y, r, numPts, false);}
	public static void drawPolygon(float x, float y, float r, int numPts, float rotation) {polygon(x,y, r, numPts, rotation, false);}
	public static void fillPolygon(vec vec, float r, int numPts) {fillPolygon(vec.get(0), vec.get(1), r, numPts);}
	public static void fillPolygon(float x, float y, float r, int numPts) {polygon(x,y, r, numPts, true);}
	public static void fillPolygon(float x, float y, float r, int numPts, float rotation) {polygon(x,y, r, numPts, rotation, true);}
	public static void polygon(float x, float y, float r, int numPts, boolean fill) {polygon(x,y,r,numPts,0,fill);}
	public static void polygon(float x, float y, float r, int numPts, float rotation, boolean fill) {
		if(fill) {
			gl.glBegin(GL2.GL_TRIANGLE_FAN);
			gl.glTexCoord2d(.5, .5);
				gl.glVertex3f(x, y, orthoLayer);
		}
		else
			gl.glBegin(GL2.GL_LINE_LOOP);
				
			float dir, xN, yN;			
			for(int i = 0; i <= numPts; i++) {
				dir = rotation + 360.f*i/numPts;
				xN = Math2D.calcLenX(dir);
				yN = Math2D.calcLenY(dir);
				
				gl.glTexCoord2d(.5+.5*xN, .5+.5*yN);
					gl.glVertex3f(x+xN*r, y+yN*r, orthoLayer);
			}
        gl.glEnd();
	}

	
	// 3D Functions

	public static void transformClear() {
		gl.glLoadIdentity();
		modelMatrix.setIdentity();
	}
	
	public static void transformTranslation(vec3 pos) {transformTranslation(pos.x(),pos.y(),pos.z());}
	public static void transformTranslation(float[] pos) {
		transformTranslation(pos[0],pos[1],pos[2]);
	}
	public static void transformTranslation(float x, float y, float z) {
		gl.glTranslatef(x,y,z);
		
		mat4 tr = mat4.createTranslationMatrix(x,y,z);
		modelMatrix.multe(tr);
		tr.destroy();
	}
			
	public static float getTime() {return time.get();}
	
    public static void enableTextures() 	{gl.glEnable(GL.GL_TEXTURE_2D);}
    public static void disableTextures() 	{gl.glDisable(GL.GL_TEXTURE_2D);}

	public static void clear() {clear(RGBA.BLACK);}
	public static void clear(RGBA color) {
		gl.glClearColor(color.getR(),color.getG(),color.getB(),color.getA());
    	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	}
		
	
	public static void begin(int type) 	{gl.glBegin(type);}
	public static void end() 			{gl.glEnd();}
	
	
	public static void vertex(float x, float y, float z, float tX, float tY, float nX, float nY, float nZ) {
		gl.glNormal3f(nX,nY,nZ);
		gl.glTexCoord2f(tX,tY);
			gl.glVertex3f(x,y,z);
	}
	public static void vertex(float x, float y, float z, float nX, float nY, float nZ) {
		gl.glNormal3f(nX,nY,nZ);
			gl.glVertex3f(x,y,z);
	}
	public static void vertex(float x, float y, float z) {
		gl.glVertex3f(x,y,z);
	}

	
	public static void forceColor(RGBA color) 	{enableFog(-1000,-1000,color);}
	public static void unforceColor() 			{disableFog();}
	
	public static GL2 getGL() {return gl;}
	
	public static void enableDepth() {
        gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
	}
	public static void disableDepth() {
        gl.glDisable(GL.GL_DEPTH_TEST);
	}

	public static Texture loadTexture(String fileName) {
		try {
			return createTexture(ImageLoader.load(fileName),false);
		} catch (IOException e) {
			return null;
		}
	}
	
	

	
	public static float getViewX() {
		return viewPos[0];
	}
	public static float getViewY() {
		return viewPos[1];
	}
	
	
	public static int getScreenWidth() 	{return SCREEN_WIDTH;}
	public static int getScreenHeight() {return SCREEN_HEIGHT;}
	public static int getSideBorderSize() 	{return BORDER_LEFT;}
	public static int getTopBorderSize()	{return BORDER_TOP;}

	
	private static void iniNode() {
		NodeSystem n = new NodeSystem();
				
		n.add("Top-Left Corner", "53o18'40.30N", "6o13'43.62W");
		n.add("Bottom-Right Corner", "53o18'02.09N", "6o12'46.83W");
		
		n.add("Outside Roebuck Hall House 1", "53o18'06.57N", "6o13'11.14W");
		n.add("Roebuck Hall House 1", "53o18'05.81N", "6o13'10.82W");
		n.add("Outside Roebuck Hall House 2", "53o18'07.02N", "6o13'09.75W");
		n.add("Roebuck Hall House 2", "53o18'06.33N", "6o13'09.44W");
		n.add("Outside Roebuck Hall House 3", "53o18'07.46N", "6o13'08.35W");
		n.add("Roebuck Hall House 3", "53o18'06.87N", "6o13'08.13W");
		n.add("Roebuck Hall - Front Gate", "53o18'08.05N", "6o13'06.39W");

		n.add("Soccer Field Shortcut", "53o18'12.12N", "6o13'09.39W");

		n.add("Owenstown Park and Mt Merrion", "53o18'14.07N", "6o13'05.01W");
		n.add("Molloy's Centra", "53o18'14.10N", "6o13'01.86W");

		n.add("Between Parking Lot and Law", "53o18'13.80N", "6o13'09.19W");
		
		n.add("Front Gate Fork", "53o18'27.39N", "6o13'09.92W");
		
		n.add("Law Building - North Entrance", "53o18'16.21N", "6o13'07.91W");
		n.add("Law Building - South Entrance", "53o18'15.35N", "6o13'07.70W");
		n.add("Outside Law Building - North Side", "53o18'17.05N", "6o13'08.39W");
		n.add("Outside Law Building - South Side", "53o18'14.46N", "6o13'07.55W");
		n.add("Outside Law Building - North Corner", "53o18'17.32N", "6o13'06.75W");
		n.add("Outside Law Building - South Corner", "53o18'14.57N", "6o13'09.58W");
		n.add("Between Business and Law", "53o18'16.50N", "6o13'09.86W");
		n.add("Outside Business Building", "53o18'17.96N", "6o13'09.46W");
		n.add("Business Building", "53o18'17.47N", "6o13'10.58W");
		n.add("Behind Business Building", "53o18'14.57N", "6o13'14.07W");

		n.add("Outside Old Student Pub", "53o18'18.63N", "6o13'11.00W");

		n.add("Behind Old Student Pub", "53o18'16.23N", "6o13'16.33W");
		n.add("Behind Newman Hall", "53o18'17.89N", "6o13'19.52W");
		n.add("Behind Newman and Library", "53o18'20.21N", "6o13'25.14W");
		n.add("Behind Library", "53o18'21.23N", "6o13'27.56W");

		n.add("Cafeteria", "53o18'19.68N", "6o13'13.59W");
		n.add("Outside Cafeteria", "53o18'20.20N", "6o13'12.33W");
		
		n.add("Outside Daedalus Building", "53o18'22.07N", "6o13'14.56W");
		n.add("Daedalus Building", "53o18'22.41N", "6o13'13.76W");

		n.add("Bank of Ireland", "53o18'24.79N", "6o13'12.32W");

		n.add("Between Daedalus, Newman and Astra", "53o18'22.69N", "6o13'15.61W");
		n.add("Between Newman and Astra", "53o18'23.61N", "6o13'17.59W");
		n.add("Newman Hall", "53o18'21.14N", "6o13'18.28W");
		n.add("Newman Hall - Front Entrance", "53o18'22.65N", "6o13'18.02W");
		n.add("Newman Hall - Side Entrance", "53o18'20.97N", "6o13'17.09W");
		n.add("Newman Hall - Back Entrance", "53o18'19.43N", "6o13'19.17W");

		n.add("Between Cafeteria and Newman", "53o18'20.80N", "6o13'15.76W");
		
		n.add("Between Newman, Astra and Library", "53o18'24.65N", "6o13'19.33W");
		n.add("Library - Front Entrance", "53o18'25.17N", "6o13'21.95W");
		n.add("Library Walkway", "53o18'23.32N", "6o13'21.48W");
		n.add("Between Student Union, O'Brien and Chapel", "53o18'27.71N", "6o13'33.59W");
		n.add("Between Health Science and O'Brien", "53o18'33.40N", "6o13'31.53W");
		n.add("Between Library and O'Brien", "53o18'26.54N", "6o13'23.85W");
		
		n.add("School of Computer Science", "53o18'33.00N", "6o13'26.43W");

		n.add("O'Brien - East Entrance", "53o18'29.68N", "6o13'24.72W");

		n.add("Veterinary Building", "53o18'32.68N", "6o13'22.58W");

		n.add("Between Astra and Lake", "53o18'26.85N", "6o13'17.08W");
		
		n.add("Glenomena Courtyard", "53o18'12.28N", "6o13'01.45W");
		n.add("Glenomena Gate", "53o18'11.56N", "6o13'04.83W");
		
		
		n.add("Bus Lot - North Corner", "53o18'19.46N", "6o12'59.19W");		
		n.add("Bus Lot - West Corner", "53o18'18.25N", "6o13'02.35W");
		n.add("Bus Lot - South Corner", "53o18'17.02N", "6o12'58.48W");

		n.add("Between ERC and Lake", "53o18'20.80N", "6o13'06.20W");

		n.add("Stillorgan Bus Stop", "53o18'33.75N", "6o13'08.09W");
		n.add("North Parking Lot", "53o18'32.82N", "6o13'19.07W");

		n.add("Between Lake and Stillorgan", "53o18'29.00N", "6o13'16.19W");
		
		n.add("Between Bus Lot, Building and Lake", "53o18'21.28N", "6o13'02.56W");
		n.add("Between Bus Lot and Lake", "53o18'18.66N", "6o13'04.37W");
		n.add("Old Track Lot", "53o18'25.51N", "6o13'00.85W");
		n.add("UCD Front Gate", "53o18'31.57N", "6o13'06.60W");

		

		// Make Links
		n.link("School of Computer Science", "Between Health Science and O'Brien");
		n.link("Between Student Union, O'Brien and Chapel", "Between Health Science and O'Brien");
		n.link("Between Student Union, O'Brien and Chapel", "Between Library and O'Brien");
		
		n.link("Outside Roebuck Hall House 1", "Outside Roebuck Hall House 2");
			n.link("Outside Roebuck Hall House 1", "Roebuck Hall House 1");
		n.link("Outside Roebuck Hall House 2", "Outside Roebuck Hall House 3");
			n.link("Outside Roebuck Hall House 2", "Roebuck Hall House 2");
		n.link("Roebuck Hall - Front Gate", "Outside Roebuck Hall House 3");
			n.link("Outside Roebuck Hall House 3", "Roebuck Hall House 3");
		n.link("Glenomena Gate", "Owenstown Park and Mt Merrion");
		n.link("Glenomena Gate", "Glenomena Courtyard");
		n.link("Molloy's Centra", "Glenomena Courtyard");
		n.link("Glenomena Gate", "Roebuck Hall - Front Gate");
		n.link("Roebuck Hall - Front Gate", "Soccer Field Shortcut");
		n.link("Owenstown Park and Mt Merrion", "Molloy's Centra");

		n.link("Bank of Ireland", "Between ERC and Lake");
		n.link("Between Bus Lot and Lake", "Between ERC and Lake");
		n.link("Between Bus Lot, Building and Lake", "Between ERC and Lake");

		n.link("Library - Front Entrance", "Library Walkway");
		n.link("Newman Hall", "Library Walkway");
		n.link("Front Gate Fork", "UCD Front Gate");
		n.link("Front Gate Fork", "Between Bus Lot, Building and Lake");
		
		n.link("Bank of Ireland", "Front Gate Fork");
		n.link("Old Track Lot", "UCD Front Gate");
		n.link("Old Track Lot", "Between Bus Lot, Building and Lake");
		n.link("Outside Business Building", "Between Bus Lot and Lake");
		n.link("Bus Lot - West Corner", "Between Bus Lot and Lake");
		n.link("Bus Lot - North Corner", "Between Bus Lot, Building and Lake");
		n.link("Between Bus Lot and Lake", "Between Bus Lot, Building and Lake");
		n.link("Outside Law Building - North Corner", "Between Bus Lot and Lake");
		n.link("Outside Law Building - North Corner", "Outside Business Building");
		
		n.link("Law Building - South Entrance", "Law Building - North Entrance");
			n.link("Law Building - South Entrance", "Outside Law Building - South Side");
		n.link("Law Building - North Entrance", "Outside Law Building - North Side");

		n.link("Outside Law Building - South Corner", "Behind Business Building");		

		n.link("Bus Lot - North Corner", "Bus Lot - South Corner");		

		n.link("UCD Front Gate", "Stillorgan Bus Stop");		
		n.link("Veterinary Building", "Between Lake and Stillorgan");		

		n.link("North Parking Lot", "Veterinary Building");		

		n.link("Between Lake and Stillorgan", "Stillorgan Bus Stop");		
		n.link("North Parking Lot", "Stillorgan Bus Stop");		
		n.link("Between Lake and Stillorgan", "Between Astra and Lake");		

		n.link("Owenstown Park and Mt Merrion", "Bus Lot - South Corner");		
		n.link("Bus Lot - West Corner", "Bus Lot - South Corner");		
		n.link("Bus Lot - West Corner", "Bus Lot - North Corner");
		n.link("Bus Lot - West Corner", "Outside Law Building - North Corner");

		n.link("Bus Lot - West Corner", "Owenstown Park and Mt Merrion");
		n.link("Bus Lot - South Corner", "Molloy's Centra");
		
		n.link("Owenstown Park and Mt Merrion", "Outside Law Building - North Corner");		
		n.link("Outside Law Building - North Side", "Outside Law Building - North Corner");
		n.link("Outside Law Building - North Side", "Outside Business Building");
			n.link("Outside Law Building - North Side", "Between Business and Law");
			n.link("Outside Law Building - South Corner", "Between Business and Law");
			n.link("Outside Law Building - South Corner", "Outside Law Building - South Side");
			n.link("Outside Law Building - South Corner", "Between Parking Lot and Law");
			n.link("Soccer Field Shortcut", "Between Parking Lot and Law");
			n.link("Owenstown Park and Mt Merrion", "Between Parking Lot and Law");
			n.link("Owenstown Park and Mt Merrion", "Outside Law Building - South Side");
		n.link("Outside Business Building", "Outside Cafeteria");
			n.link("Outside Business Building", "Business Building");
			n.link("Behind Business Building", "Between Business and Law");
			n.link("Behind Business Building", "Between Parking Lot and Law");
			n.link("Behind Business Building", "Soccer Field Shortcut");
			n.link("Business Building", "Between Business and Law");
			n.link("Business Building", "Outside Old Student Pub");
			n.link("Outside Old Student Pub", "Cafeteria");
			n.link("Outside Old Student Pub", "Behind Old Student Pub");
			n.link("Outside Old Student Pub", "Outside Cafeteria");
			n.link("Outside Old Student Pub", "Outside Business Building");



		n.link("Outside Cafeteria", "Cafeteria");		
		n.link("Outside Cafeteria", "Outside Daedalus Building");
			n.link("Outside Cafeteria", "Between Cafeteria and Newman");
			n.link("Outside Daedalus Building", "Between Cafeteria and Newman");
			n.link("Behind Newman Hall", "Between Cafeteria and Newman");
			n.link("Between Cafeteria and Newman", "Newman Hall - Side Entrance");
		n.link("Outside Daedalus Building", "Between Daedalus, Newman and Astra");
		n.link("Between Daedalus, Newman and Astra", "Between Newman and Astra");
			n.link("Between Daedalus, Newman and Astra", "Bank of Ireland");

		n.link("Between Newman, Astra and Library", "Between Astra and Lake");
		n.link("Bank of Ireland", "Between Astra and Lake");

		n.link("Between Newman and Astra", "Newman Hall - Front Entrance");
		n.link("Between Newman, Astra and Library", "Newman Hall - Front Entrance");
		n.link("Between Daedalus, Newman and Astra", "Newman Hall - Front Entrance");
			n.link("Between Daedalus, Newman and Astra", "Newman Hall - Side Entrance");

		n.link("Between Newman and Astra", "Between Newman, Astra and Library");

		n.link("Between Newman, Astra and Library", "Library - Front Entrance");

		n.link("Daedalus Building", "Outside Daedalus Building");
		n.link("Glenomena Gate", "Soccer Field Shortcut");

		n.link("Newman Hall", "Newman Hall - Front Entrance");
		n.link("Newman Hall", "Newman Hall - Side Entrance");
		n.link("Newman Hall", "Newman Hall - Back Entrance");

		n.link("Outside Law Building - North Corner", "Bus Lot - South Corner");
		
		n.link("Behind Business Building", "Behind Old Student Pub");
		n.link("Behind Newman Hall", "Behind Old Student Pub");
		n.link("Behind Newman Hall", "Behind Newman and Library");
			n.link("Behind Newman Hall", "Newman Hall - Back Entrance");
		n.link("Between Newman, Astra and Library", "Behind Newman and Library");

		n.link("Behind Library", "Behind Newman and Library");
		n.link("Behind Library", "Between Library and O'Brien");

		n.link("Library - Front Entrance", "Between Library and O'Brien");
		n.link("Between Library and O'Brien", "O'Brien - East Entrance");
		n.link("O'Brien - East Entrance", "Veterinary Building");
		n.link("School of Computer Science", "Veterinary Building");
		
		
		n.doneAdding();
		
		
		nodeSystem = n;
		
		algorithmTester = new AlgorithmTester(n);
	}

	public static GLCanvas getCanvas() {return canv;}

	public static NodeSystem getNodeSystem() {return nodeSystem;}
	public static AlgorithmTester getAT() {return algorithmTester;}
}