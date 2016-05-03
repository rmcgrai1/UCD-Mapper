package learning;

import java.util.List;

import gfx.GOGL;

public class Edge {
	private double distance;
	private Node startNode, endNode;
	
	public Edge(Node startNode, Node endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
		distance = Node.calcDistance(startNode, endNode);		
	}

	public Node getStartNode() 		{return startNode;}
	public Node getEndNode() 		{return endNode;}
	public double getDistance() 	{return distance;}
	
	
	public void draw() {
		GOGL.drawLine((float)startNode.getScreenX(),(float)startNode.getScreenY(), (float)endNode.getScreenX(), (float)endNode.getScreenY());
	}
	
	public void destroy() {
		startNode = endNode = null;
	}
	
	public boolean equals(Edge other) {
		boolean direct, indirect;
		direct = this == other;
		indirect = (endNode == other.endNode || endNode == other.startNode || startNode == other.startNode);
		
		return direct || indirect;
	}
	
	public boolean checkInList(List<Edge> edgeList) {
		for(Edge e : edgeList)
			if(equals(e))
				return true;
		return false;
	}
}