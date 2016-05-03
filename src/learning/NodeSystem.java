package learning;

import functions.Math2D;
import gfx.GOGL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jogamp.opengl.util.texture.Texture;
import datatypes.lists.RandomList;

public class NodeSystem {
	private RandomList<Node> nodeList;
	private List<Edge> edgeList;
	
	private List<EdgePair> edgePairList;
	private Map<Edge, EdgePair> edgePairMap;
	
	private Map<String, Node> nodeMap;
	
	private Texture mapTex;
	
	private double[] boundingBox;
	private double maxDistance;
	
	public NodeSystem() {
		nodeList = new RandomList<Node>("Node System");
		edgeList = new ArrayList<Edge>();
		nodeMap = new HashMap<String, Node>();
		boundingBox = new double[4];
		
		edgePairList = new ArrayList<EdgePair>();
		edgePairMap = new HashMap<Edge, EdgePair>();
		
		mapTex = GOGL.loadTexture("Resources/map.png");
	}
	
	public void draw() {
		// Draw Map
		GOGL.drawTexture(0,0, GOGL.getScreenWidth(),GOGL.getScreenHeight(), mapTex);

		// Draw All Nodes
		for(Node n : nodeList)
			n.draw();
	}
	
	public Node getNearestNode(double screenX, double screenY) {
		Node nearest = null;
		double minDis = Double.MAX_VALUE, curDis;
		
		for(Node n : nodeList) {
			curDis = Math2D.calcPtDis(screenX,screenY, n.getScreenX(),n.getScreenY());
			if(curDis < minDis) {
				nearest = n;
				minDis = curDis;
			}
		}
		
		return nearest;
	}

	public void doneAdding() {
		Node node = nodeList.get(0);
		boundingBox[0] = node.getLatitude();
		boundingBox[1] = node.getLongitude();
		boundingBox[2] = node.getLatitude();
		boundingBox[3] = node.getLongitude();

		for(Node n : nodeList) {
			boundingBox[0] = Math.min(n.getLatitude(), 	boundingBox[0]);
			boundingBox[1] = Math.min(n.getLongitude(), boundingBox[1]);
			boundingBox[2] = Math.max(n.getLatitude(), 	boundingBox[2]);
			boundingBox[3] = Math.max(n.getLongitude(), boundingBox[3]);
		}
		
		for(Node n : nodeList)
			n.calculateScreenCoords(boundingBox);
		
		maxDistance = Node.calcDistance(boundingBox[0],boundingBox[1],boundingBox[2],boundingBox[3]);
	}
	
	public void add(String name, double latitude, double longitude) {
		Node node = new Node(name, latitude, longitude);
		
		nodeList.add(node);
		nodeMap.put(name, node);
	}	
	public void add(String name, String latitude, String longitude) {
		Node node = new Node(name, latitude, longitude);
		
		nodeList.add(node);
		nodeMap.put(name, node);
	}
	
	public boolean link(String nodeName1, String nodeName2) {
		Node 	node1 = nodeMap.get(nodeName1),
				node2 = nodeMap.get(nodeName2);
		
		return link(node1, node2);
	}
	public boolean link(Node node1, Node node2) {
		if(node1 == null || node2 == null)
			throw new NullPointerException();
		
		if(node1 == node2 || node1.checkNeighbor(node2))
			return false;

		Edge a, b;
		a = node1.addEdge(node2);
		b = node2.addEdge(node1);
		edgeList.add(a);
		edgeList.add(b);
		
		EdgePair ab;
		ab = new EdgePair(a, b);
		edgePairList.add( ab );
		edgePairMap.put(a, ab);
		edgePairMap.put(b, ab);
		
		return true;
	}

	public int size() {return nodeList.size();}

	public double getMaxDistance() {return maxDistance;}
	public Node getRandomNode() {
		Node out;
		boolean isInvalid;
		
		do {
			out = nodeList.get();
			
			isInvalid = (out.getName().equals("Top-Left Corner")) || (out.getName().equals("Bottom-Right Corner"));
		} while(isInvalid);
		
		return out;
	}

	public List<Node> 		getNodes() 			{return nodeList;}
	public List<Edge> 		getEdges() 			{return edgeList;}
	public List<EdgePair> 	getEdgePairs() 		{return edgePairList;}	
	public EdgePair 		getEdgePair(Edge e) {return edgePairMap.get(e);}
}
