package object.primitive;
import datatypes.lists.CleanList;

public abstract class Drawable extends Updatable {
	private static CleanList<Drawable> drawList = new CleanList<Drawable>("Draw");
	
	
	public Drawable() {	
		super();
		drawList.add(this);
	}
	
	//PARENT FUNCTIONS
	public abstract void draw();

	public void destroy() {
		super.destroy();
		drawList.remove(this);
	}
			
	//GLOBAL FUNCTIONS
	
	public static int getNumber() {return drawList.size();}
	
	public static void display() {		
		for(Drawable d : drawList)
			d.draw();
	}
}
