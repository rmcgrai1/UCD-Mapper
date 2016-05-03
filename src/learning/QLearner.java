package learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QLearner extends ReinforcementLearner {
	protected Map<Node, Double> Qmax = new HashMap<Node, Double>();

	
	public QLearner(NodeSystem system, Node startNode, Node endNode, int trialNum) {
		super("Q Learning", system, startNode, endNode, trialNum);
	}
	public QLearner(NodeSystem system, Node startNode, Node endNode, int trialNum, double predictionWeight, double randomnessWeight) {
		super("Q Learning", system, startNode, endNode, trialNum, predictionWeight, randomnessWeight);
	}
	
	
	protected void updateQMax(Node n, double q) {
		Double qMax = Qmax.get(n);
		if(qMax == null)	Qmax.put(n,q);
		else if(qMax < q)	Qmax.put(n,q);
	}
	protected double getQMax(Node n) {
		Double q = Qmax.get(n);
		return (q == null) ? DEFAULT_REWARD : q;
	}

	
	@Override
	public double learn(List<Edge> path) {
		double travelDistance = 0, q;		
		boolean wasCompleted = false;
		Node curNode = startNode, toNode;
		Edge transition;
				
		while(!wasCompleted) {
			transition = pickTransition(curNode, path);
			
			// If Reached Dead End, Return
			if(transition == null)
				break;
			

			// Get To Node and Add to Path
			toNode = transition.getEndNode();
			path.add(transition);

			
			// Calculate Q
			q = calcReward(transition) + predictionWeight*getQMax(toNode);
			Q.put(transition, q);
			
			
			// If New Q Greater than Previous Max, Reset QMax
			updateQMax(curNode, q);
			
			
			// Increase Travel Distance
			travelDistance += transition.getDistance();			
			
			// Stop Trial if Reach End
			if((curNode = toNode) == endNode)
				wasCompleted = true;
		}
				
		if(wasCompleted)
			return travelDistance;
		else
			return Double.MAX_VALUE;
	}
	
	public void clear() {
		super.clear();
		
		if(Qmax != null)
			Qmax.clear();
	}
}
