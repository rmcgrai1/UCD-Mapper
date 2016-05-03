package learning;

public class EdgePair {
	private Edge edgeA, edgeB;
	
	public EdgePair(Edge a, Edge b) {
		edgeA = a;
		edgeB = b;
	}
	
	public void draw() {
		edgeA.draw();
	}
}
