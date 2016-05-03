package learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DijkstraLearner extends Algorithm {
		
	public DijkstraLearner(NodeSystem system, Node startNode, Node endNode, int trialNum) {
		super("Dijkstra's Algorithm", system, startNode, endNode, trialNum);
	}
		
	@Override
	public double learn(List<Edge> path) {
		Map<Node, Node> cameFrom = new HashMap<Node,Node>();
		Map<Node, Double> dist = new HashMap<Node, Double>();
		List<Edge> neighbors;
		List<Node> allNodes = new ArrayList<Node>(),
			systemList = system.getNodes();
		boolean wasCompleted = false;
		double alt, outDistance = 0;
		Node current = null, neighbor;

		
		// Copy Nodes into New List so Original is Protected
		for(Node n : systemList) {
			allNodes.add(n);
			dist.put(n, Double.MAX_VALUE);
		}
		
		// Assign 0 to Start Node so it is Picked First
		dist.put(startNode, 0.);
			     

	    while(!allNodes.isEmpty()) {
	    	
	    	// Get New Current Node, Node w/ Minimum Distance
	    	double minDist = Double.MAX_VALUE, curDist;
	    	current = allNodes.get(0);
	    	for(Node n : allNodes) {
	    		curDist = dist.get(n);
	    		if(curDist < minDist) {
	    			current = n;
	    			minDist = curDist;
	    		}
	    	}
	    	allNodes.remove(current);
	        
	    	
	    	// If Current Node is End, Stop Searching! 
	        if(current == endNode) {
	        	wasCompleted = true;
	        	break;
	        }

	        // Get Neighbors at Current Node, Calculate Each's Distance
	        neighbors = current.getNeighbors();
	        for(Edge e : neighbors) {
	        	neighbor = e.getEndNode();
	            
	        	if(allNodes.contains(neighbor)) {
	        		// Neighbor's Distance = Current Distance + Edge Length
		        	alt = dist.get(current) + e.getDistance();
		        	
		        	// If Calculated Distance is Less than Neighbor's Current Distance,
		        	// Update Info!
		            if(alt < dist.get(neighbor)) {
		            	dist.put(neighbor, alt);
		            	cameFrom.put(neighbor, current);
		            }
	        	}
	        }
	    }

	    // Dijkstra Only Needs to Run Once to Find Best Path
	    setDone();
	    
	    // Work from End Node to Start w/ CameFrom Map (Where the Magic Happens!!)
	    if(wasCompleted) {
		    Node next = current;
		    Edge e;
		    
		    // Loop Through, Build Path
		    while(cameFrom.containsKey(current)) {
		        current = cameFrom.get(current);
		        
		        // Find Edge from Current to Next
		        e = current.getEdge(next);
		        	outDistance += e.getDistance();
		        path.add(e);
		        
		        next = current;
		    }
	    }
	    else
	    	outDistance = Double.MAX_VALUE;
	    
	    // Clean Up
	    dist.clear();
	    cameFrom.clear();
	    allNodes.clear();
	    //systemList.clear(); // Do NOT erase, System List is Original List!
	    
	    return outDistance;
	}	
}
