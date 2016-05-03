package time;

public final class Delta {

	private static float fps, targetFPS;
	
	private Delta() {
		fps = targetFPS = 60;
	}
	
	// STATIC
		public static float getFPS() {return fps;}
		public static void setTargetFPS(float newTargetFPS) {
			targetFPS = newTargetFPS;
		}
	
	// NONSTATIC
		
		// Delta Time Methods
		public static float calcDeltaTime() {
			if(fps <= 0 || targetFPS <= 0)
				return 1;
			else
				return 60/fps;
		}
		public static float convert(float timeVal) {
			return timeVal*calcDeltaTime();
		}
	
	
		// RUNNING METHODS
		public static void setDelta(float delta) {
			
			if(delta == 0)
				return;
						
			fps = targetFPS/delta;
		}

		public static float getTargetFPS() {
			return targetFPS;
		}
}
