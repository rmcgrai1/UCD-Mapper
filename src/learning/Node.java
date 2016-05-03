package learning;

import datatypes.StringExt;
import datatypes.lists.RandomList;
import gfx.GOGL;

public class Node {
	private RandomList<Edge> neighbors;
	private String name;
	private double latitude, longitude, screenX, screenY;

	public Node(String name, double latitude, double longitude) {
		neighbors = new RandomList<Edge>(name + "'s Edges");
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public Node(String name, String latitude, String longitude) {
		neighbors = new RandomList<Edge>(name + "'s Edges");
		this.name = name;
		this.latitude = convertDegMinSecToDeg(latitude);
		this.longitude = convertDegMinSecToDeg(longitude);
	}

	public void calculateScreenCoords(double[] boundingBox) {
		double xFrac, yFrac;
		
		xFrac = (longitude - boundingBox[1])/(boundingBox[3]-boundingBox[1]);
		yFrac = (latitude - boundingBox[0])/(boundingBox[2]-boundingBox[0]);
				
		screenX = GOGL.getScreenWidth() * xFrac;
		screenY = GOGL.getScreenHeight() * yFrac;
	}
	
	public void draw() 				{draw(1);}
	public void draw(float scale) 	{
		GOGL.fillPolygon((float) screenX, (float) screenY,  scale*3,  5);
	}
		
	public Edge addEdge(Node other) {
		Edge e = getEdge(other);
		
		if(e != null)
			return e;
		else {
			neighbors.add(e = new Edge(this, other));
			return e;
		}
	}
	public boolean checkNeighbor(Node other) {
		return getEdge(other) != null;
	}
	
	
	public Edge getEdge(Node toNode) {
		Edge outEdge = null;
		
		for(Edge e : neighbors)
			if(e.getEndNode() == toNode) {
				outEdge = e;
				break;
			}
		neighbors.broke();	// Necessary for CleanList Iterator
		
		return outEdge;
	}
	public Edge getEdgeRandom() 	{return neighbors.get();}
	public Node getNeighborRandom()	{return getEdgeRandom().getEndNode();}

	public double convertDegMinSecToDeg(String degMinSec) {
		StringExt dms = new StringExt(degMinSec.replace('\'',' ').replace('\"',' ').replace('o',' '));
		
		int sign = 1;
		dms.chopOut("S", "E");
		if(dms.chopOut("N", "W"))
			sign *= -1;
				
		double ang = dms.chompNumber() +
			dms.chompNumber()/60. +
			dms.chompNumber()/3600.;
		
		return ang*sign;
	}
	
	public String getName()	 		{return name;}
	public double getLatitude() 	{return latitude;}
	public double getLongitude() 	{return longitude;}
	public double getScreenX()		{return screenX;}
	public double getScreenY()		{return screenY;}
	
	public RandomList<Edge> getNeighbors() {return neighbors;}

	
	// Convert Distance (m) to Time (s) Based on Average Walking Speed
	public static double calcTime(float distance) {
		return distance/1.4;
	}
	
	// HAVERSINE FORMULA
	
	public static double calcDistance(Node node1, Node node2) {
		return calcDistance(node1.getLatitude(),node1.getLongitude(), node2.getLatitude(),node2.getLongitude());
	}
	public static double calcDistance(double lat1, double long1, double lat2, double long2) {
		double earthRadius = 6371000, deg2Rad = Math.PI/180.;

		double la1 = lat1*deg2Rad,
			la2 = lat2*deg2Rad,
			dLat = (lat2-lat1)*deg2Rad,
			dLon = (long2-long1)*deg2Rad;

		double v1 = Math.sin(dLat/2),
			v2 = Math.cos(la1) * Math.cos(la2),
			v3 = Math.sin(dLon/2);
		double a =  v1*v1 + v2 * v3*v3;
				
		double dis = earthRadius * ( 2 * Math.asin(Math.sqrt(a)) );
		
		return dis;
	}
	
	
	public void destroy() {
		for(Edge e : neighbors)
			e.destroy();
		neighbors.clear();
	}
}