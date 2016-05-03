package object.primitive;

import io.Mouse;
import datatypes.lists.CleanList;

public abstract class Updatable {
	private static CleanList<Updatable> instanceList = new CleanList<Updatable>("Inst");
	private static CleanList<Updatable> updateList = new CleanList<Updatable>("Upd");
	protected boolean doUpdates;
	protected String name = "";


	public Updatable() {
		doUpdates = true;
			
		instanceList.add(this);
		updateList.add(this);
	}
	
	public abstract void update();
	
	public void destroy() {
		instanceList.remove(this);
		updateList.remove(this);
	}

		
	//Global Functions
		public static void updateAll() {			
			Mouse.update();
			
			for(Updatable u : updateList)
				u.update();
		}

		public static int getNumber() {
			return updateList.size();
		}
		
		protected void disableUpdates() {
			updateList.remove(this);
		}
		protected void setDoUpdates(boolean should) {
			doUpdates = should;
		}

		public static void unload() {
			for(Updatable u : instanceList)
				u.destroy();
			instanceList.clear();
		}

		public static CleanList<Updatable> getList() {
			return instanceList;
		}

		public String getName() {
			return name;
		}
}
