package learning;

import java.util.List;

public class SARSALearner extends ReinforcementLearner {
	
	public SARSALearner(NodeSystem system, Node startNode, Node endNode, int trialNum) {
		super("SARSA", system, startNode, endNode, trialNum);
	}
	public SARSALearner(NodeSystem system, Node startNode, Node endNode, int trialNum, double predictionWeight, double randomnessWeight) {
		super("SARSA", system, startNode, endNode, trialNum, predictionWeight, randomnessWeight);
	}
	
	@Override
	public double learn(List<Edge> path) {
		int maxSteps = 10*system.size();
		double travelDistance = 0, q;		
		boolean wasCompleted = false;
		Node curNode = startNode, toNode;
		Edge transition, nextTransition;
		
		nextTransition = pickTransition(curNode, path);
		
		while(!wasCompleted && maxSteps > 0) {
					
			transition = nextTransition;

			// If Reached Dead End, Return
			if(transition == null)
				break;

				// Get To Node and Add to Path
				path.add(transition);
				toNode = transition.getEndNode();

			nextTransition = pickTransition(toNode, path);			
			
			if(nextTransition == null)
				break;
			
			// Calculate Q
			q = calcReward(transition) + predictionWeight*calcReward(nextTransition);
			Q.put(transition, q);
			
			
			travelDistance += transition.getDistance();			
			
			// Stop Trial if Reach End
			if((curNode = toNode) == endNode)
				wasCompleted = true;
			
			maxSteps--;
		}
				
		if(wasCompleted)
			return travelDistance;
		else {
			//failed();
			return Double.MAX_VALUE;
		}
	}
}
