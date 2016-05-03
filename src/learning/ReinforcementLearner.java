package learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import functions.MathExt;

public abstract class ReinforcementLearner extends Algorithm {
	protected Map<Edge, Double> Q = new HashMap<Edge, Double>();			
	protected double predictionWeight = .95, randomnessWeight = .4;
	protected final double DEFAULT_REWARD = -1;
	

	public ReinforcementLearner(String name, NodeSystem system, Node startNode, Node endNode, int trialNum) {
		super(name, system, startNode, endNode, trialNum);
	}
	public ReinforcementLearner(String name, NodeSystem system, Node startNode, Node endNode, int trialNum, double predictionWeight, double randomnessWeight) {
		super(name, system, startNode, endNode, trialNum);
		this.predictionWeight = predictionWeight;
		this.randomnessWeight = randomnessWeight;
	}
	
	// Reward is Difference between Initial Distance to End Node and Final Distance to End Node
	public double calcReward(Edge edge)	{
		return Node.calcDistance(edge.getStartNode(), endNode)
				- Node.calcDistance(edge.getEndNode(), endNode);	
	}
	
	protected double getQ(Node n) {
		Double q = Q.get(n);
		return (q == null) ? DEFAULT_REWARD : q;
	}
	
			
	protected Edge pickTransition(Node curNode, List<Edge> path) {
		Edge transition = null;
		
		// Pick Random Path
		if(MathExt.rnd() <= randomnessWeight)
			transition = curNode.getEdgeRandom();

		// Pick Best Path
		if(transition == null || transition.checkInList(path)) {
			List<Edge> neighbors = curNode.getNeighbors();

			transition = null;
			
			double curReward, maxReward = DEFAULT_REWARD;
			for(Edge e : neighbors)
				// If Edge Not Picked Yet...
				if(!e.checkInList(path))
					// If Current Edge Gives Best Reward, Pick
					if((curReward = getQ(e.getEndNode())) >= maxReward) {
						transition = e;
						maxReward = curReward;
					}
		}

		return transition;
	}
	
	protected void clear() {
		super.clear();
		if(Q != null)
			Q.clear();
	}
}
