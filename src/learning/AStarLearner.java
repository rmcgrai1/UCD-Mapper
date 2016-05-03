package learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import datatypes.lists.CleanList;

public class AStarLearner extends Algorithm {
	public AStarLearner(NodeSystem system, Node startNode, Node endNode, int trialNum) {
		super("A*", system, startNode, endNode, trialNum);
	}
	
	public double calcH(Node n)	{
		return Node.calcDistance(n, endNode);
	}
	
	@Override
	public double learn(List<Edge> path) {
		List<Node> closedSet = new ArrayList<Node>(),
					openSet = new ArrayList<Node>();
		Map<Node, Node> cameFrom = new HashMap<Node,Node>();
		Map<Node, Double> gScore = new HashMap<Node, Double>(),
			fScore = new HashMap<Node, Double>();
		gScore.put(startNode, 0.);
		fScore.put(startNode, gScore.get(startNode) + calcH(startNode));
			     
		CleanList<Edge> neighbors;
		
		boolean wasCompleted = false;
		
		double tentativeGScore;
		Node current = null, neighbor;

		openSet.add(startNode);
	    while(!openSet.isEmpty()) {
	    	
	    	// Get New Current Node, Node w/ Minimum F Score
	    	double minFScore = Double.MAX_VALUE, curFScore;
	    	current = openSet.get(0);
	    	for(Node n : openSet) {
	    		curFScore = fScore.get(n);
	    		if(curFScore < minFScore) {
	    			current = n;
	    			minFScore = curFScore;
	    		}
	    	}
	        
	    	// If Current Node is End, Stop Searching! 
	        if(current == endNode) {
	        	wasCompleted = true;
	        	break;
	        }

	        openSet.remove(current);
	        closedSet.add(current);
	        
	        // Get Neighbors at Current Node, Calculate Each's Distance
	        neighbors = current.getNeighbors();
	        for(Edge e : neighbors) {
	        	neighbor = e.getEndNode();

	        	// Ignore the neighbor which is already evaluated.
	        	if(closedSet.contains(neighbor))
	                continue;
	        	// Calculate G Score for Current Neighbor
	            tentativeGScore = gScore.get(current) + e.getDistance();
	        	// Discover a new node
	            if(!openSet.contains(neighbor))
	                openSet.add(neighbor);
	            // If G Score Greater than Neighbor's Current G Score, Then this is a Worse Path!
	            else if(tentativeGScore >= gScore.get(neighbor))
	                continue;

	            // This path is the best until now. Record it!
	            cameFrom.put(neighbor, current);
	            gScore.put(neighbor, tentativeGScore);
	            fScore.put(neighbor, gScore.get(neighbor) + calcH(neighbor));
	        }
	        neighbors.broke();
	    }
	    
	    // Work from End Node to Start w/ CameFrom Map (Where the Magic Happens!!)
	    double outDistance = Double.MAX_VALUE;
	    if(wasCompleted) {
		    Node prev = current;
		    Edge e;
		    double totalDistance = 0;
		    
		    // Loop Through, Build Path
		    while(cameFrom.containsKey(current)) {
		        current = cameFrom.get(current);
		        
		        // Find Edge from Current to Next
		        e = current.getEdge(prev);
		        	totalDistance += e.getDistance();
		        path.add(e);
		        
		        prev = current;
		    }
		    		    
		    outDistance = totalDistance;
	    }
	    
	    
	    // Clean Up
	    openSet.clear();
	    closedSet.clear();
	    gScore.clear();
	    fScore.clear();
	    cameFrom.clear();
	    
	    // A* Only Needs to Run Once to Find Best Path
	    setDone();
	    
	    return outDistance;
	}
}
