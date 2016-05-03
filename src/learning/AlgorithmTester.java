package learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cont.Controller;
import functions.Math2D;
import gfx.GOGL;
import gfx.RGBA;

public class AlgorithmTester {
	private NodeSystem nodeSystem;
	private Node start, end, hoverNode;
	private boolean isRandom, isTestDone;

	private boolean doQ = true, doSARSA = true, doDijkstra = true, doAStar = true;
	private List<Algorithm> algorithms = new ArrayList<Algorithm>();
	private double weightRandomness = .95, weightPrediction = .4;
	private int numTests = -1, totNumTests;
	private int trialNum;
			
	// Keeping Track of Edge Usage
	private Map<EdgePair,Long> trafficMap = new HashMap<EdgePair, Long>();
	private long maxTraffic = 0;
	
	private static List<RGBA> colors = new ArrayList<RGBA>();
	static { 
		colors.add(RGBA.YELLOW);
		colors.add(RGBA.BLUE);
		colors.add(RGBA.MAGENTA);
		colors.add(RGBA.CYAN);
	}
	
	
	public AlgorithmTester(NodeSystem n) {
		this.nodeSystem = n;

		for(EdgePair e : n.getEdgePairs())
			trafficMap.put(e, 0L);
	}
	
	public void start(int numTests, int trialNum) {
		this.totNumTests = this.numTests = numTests;
		this.trialNum = trialNum;	
		
		isTestDone = false;
		
		// Destroy Algorithms to Free Memory
		for(Algorithm a : algorithms)
			if(a != null)
				a.destroy();
		
		// Clear Algorithms, Fill List
		algorithms.clear();		
		algorithms.add(doQ ? new QLearner(nodeSystem, start, end, trialNum, weightPrediction, weightRandomness) : null);
		algorithms.add(doSARSA ? new SARSALearner(nodeSystem, start, end, trialNum, weightPrediction, weightRandomness) : null);	
		algorithms.add(doDijkstra ? new DijkstraLearner(nodeSystem, start, end, trialNum) : null);
		algorithms.add(doAStar ? new AStarLearner(nodeSystem, start, end, trialNum) : null);
	}
	
	public boolean checkDone() {
		boolean allEqual = true;

		int numAlgorithms = 0;
		double len = -1;

		for(Algorithm a : algorithms) {
			numAlgorithms++;
			if(a.getTrialNum() == 0)
				allEqual = false;
			if(len == -1)
				len = a.getTravelTime();
			else if(len != a.getTravelTime())
				allEqual = false;
		}
			
		if(numAlgorithms <= 1)
			allEqual = false;
		
		for(Algorithm a : algorithms)
			if(a == null)
				continue;
			else if(allEqual)
				a.setDone();
			else if(!a.getDone())
				return false;
		return true;
	}
	
	public String limitString(String str, int places) {
		int len = str.length();
		
		if(len == places)
			return str;
		else if(len < places) {
			str.replace(" ", "");
			while((str = "_"+str).length() < places);
			return str;
		}
		else
			return str.substring(0, places);
	}
	
	public void clear() {
		if(isTestDone)
			return;
		
		int minHops = 100, curHops;
		
		// Determine Minimum # of Hops for Path, Print at Start of Line
		for(Algorithm a : algorithms)
			if(a != null) {
				curHops = a.getSolution().size();
				if(curHops != 0)
					minHops = Math.min(minHops, curHops);
			}
		Controller.print(limitString(""+minHops,2)+"|");

		Algorithm aa;
		String curStr;
		
		// Print Running Time of All Algorithms
		for(int i = 0; i < 4; i++) {
			aa = algorithms.get(i);
			curStr = limitString((aa != null) ? ""+aa.getLearningTimeSecs() : "", 10);
			Controller.print(curStr + "|");
		}
		// Print Trial Num of All Algorithms
		for(int i = 0; i < 2; i++) {
			aa = algorithms.get(i);
			curStr = limitString((aa != null) ? ""+aa.getTrialNum() : "", 4);			
			Controller.print(curStr + "|");
		}
		Controller.endln();
		
		EdgePair ep;
		long traffic;
		for(Algorithm a : algorithms)
			if(a != null)
				for(Edge e : a.getSolution()) {
					ep = nodeSystem.getEdgePair(e);
					
					traffic = trafficMap.get(ep)+1;
					trafficMap.put(ep, traffic);
					
					maxTraffic = Math.max(maxTraffic, traffic);
				}
		
		isTestDone = true;
	}
	
	public void run() {
		for(int i = 0; i < 1; i++)
			runTest();
	}
	
	public void runTest() {
		if(numTests <= 0)
			return;
		
		if(checkDone())
			clear();
			
		if(isTestDone) {
			// Pick New Start & End Nodes
			if(isRandom)
				randomNodes();
			
			// Clear Current Test Information
			for(Algorithm a : algorithms)
				if(a != null)
					a.clear();
			
			isTestDone = false;	
			numTests--;
		}	

		// Run All Trials for Each
		for(Algorithm a : algorithms)
			if(a != null)
				a.runTrials(trialNum);
	}
	
	public void draw() {
		
		// Draw Traffic Map
		float perc, minWidth = 1, maxWidth = 5;
		List<EdgePair> edgePairs = nodeSystem.getEdgePairs();
		for(EdgePair e : edgePairs) {
			perc = 1.f*trafficMap.get(e)/maxTraffic;

			// Set Color and Line Width Based on Traffic
			GOGL.setColor(new RGBA(perc,0,0,1));
			GOGL.setLineWidth((1-perc)*minWidth + perc*maxWidth);
			
			e.draw();
		}
		

		GOGL.setColor(RGBA.WHITE);
		if(hoverNode != null)
			GOGL.drawPolygon((float) hoverNode.getScreenX(), (float) hoverNode.getScreenY(), 10+Math2D.calcLenX(2,GOGL.getTime()*8), 10);
		
		GOGL.setColor(RGBA.GREEN);
		if(start != null)
			start.draw(1.5f);
		if(end != null)
			end.draw(1.5f);

		
		// Draw Each Algorithm, Slightly Offset (To See all Lines)
		Algorithm a;
		float offsetDir, offsetX, offsetY;
		for(int i = 0; i < algorithms.size(); i++) {
			a = algorithms.get(i);
			
			if(a == null)
				continue;
			
			offsetDir = 360 * i/algorithms.size();
			offsetX = Math2D.calcLenX(2, offsetDir);
			offsetY = Math2D.calcLenY(2, offsetDir);
			
			GOGL.setColor(colors.get(i));
			GOGL.transformTranslation(offsetX,offsetY,0);
				a.draw();
			GOGL.transformTranslation(-offsetX,-offsetY,0);
		}		
	}
	
	public float getProgressFraction() {
		return 1-1f*numTests/totNumTests;
	}

	public Node getStartNode() 	{return start;}
	public Node getEndNode() 	{return end;}

	public void setStart(Node node) {start = node;}
	public void setEnd(Node node) {end = node;}

	public void setRandom(boolean isRandom) {this.isRandom = isRandom;}
	public void randomNodes() {
		start = nodeSystem.getRandomNode();			
		do	end = nodeSystem.getRandomNode();
		while(start == end);
		
		for(Algorithm a : algorithms)
			if(a != null) {
				a.setStart(start);
				a.setEnd(end);
			}
	}
	
	public void setHoverNode(Node n) {hoverNode = n;}
	
	public void setRandomnessWeight(double w) {weightRandomness = w;}
	public void setPredictionWeight(double w) {weightPrediction = w;}
	
	public void enableQLearning(boolean st) {doQ = st;}
	public void enableSARSA(boolean st) {doSARSA = st;}
	public void enableDijkstra(boolean st) {doDijkstra = st;}
	public void enableAStar(boolean st) {doAStar = st;}
}