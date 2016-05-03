package learning;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import time.Stopwatch;

public abstract class Algorithm {
	protected NodeSystem system;
	private String algorithmName;
	
	private int maxTrialNum, trialNum, curTrialNum, bestTrialNum;

	// Current Learning Variables
	private List<Edge> solution;
	private double travelDistance;
	protected long learningTime;
	
	// Best Solution Variables
	private List<Edge> bestSolution;
	private double bestTravelDistance;
	protected long bestLearningTime;
	private Deque<Double> bestTravelDistances;
	
	private boolean isDone;
	private Stopwatch watch;
	
	protected Node startNode, endNode;
	
	public Algorithm(String algorithmName, NodeSystem system, Node startNode, Node endNode, int maxTrialNum) {
		this.algorithmName = algorithmName;
		
		this.system = system;
		watch = new Stopwatch();
		this.startNode = startNode;
		this.endNode = endNode;
		
		this.maxTrialNum = maxTrialNum;
		
		solution = new ArrayList<Edge>();
		bestSolution = new ArrayList<Edge>();
		bestTravelDistances = new LinkedList<Double>();
		
		clear();
	}

	
	// Erase Data Stored in Algorithm
	public void destroy() {
		clear();
		
		system = null;
		watch = null;
		
		bestTravelDistances = null;
		bestSolution = null;
		solution = null;
	}
	
	
	public abstract double learn(List<Edge> path);

	
	public void runTrial() {
		// If Done Learning, Quit
		if(isDone)
			return;
		
		// If No More Trials Yet, Done Learning
		if(trialNum <= 0) {
			setDone();
			return;
		}
		
		
		// Reset Solution
		solution.clear();
		
		// Time Learning Process & Get Solution and distance
		watch.start();
			travelDistance = learn(solution);
		watch.stop();
		
		
		// Increment Total Learning Time
		learningTime += watch.getNano();
		
		// If Current Solution Beats Best Solution...
		if(travelDistance < bestTravelDistance) {
			// Update Best Distance & Time
			bestTravelDistances.addFirst(bestTravelDistance = travelDistance);
			bestLearningTime = learningTime;
			bestTrialNum = curTrialNum;
			
			// Update Solution
			bestSolution.clear();
			for(Edge e : solution)
				bestSolution.add(e);
		}
		
		trialNum--;		// Decrease # of Remaining Trials
		curTrialNum++;	// Increase # of Total Trials Necessary
	}
	public void runTrials(int times) {
		for(int i = 0; i < times; i++) {
			if(getDone())
				break;
			runTrial();
		}
	}

	
	public void draw() {
		for(Edge e : bestSolution)
			e.draw();
	}
		
	public double 	getTravelDistance()		{return bestTravelDistance;}
	public double 	getTravelTime()			{return bestTravelDistance/1.4;}
	public String 	getTravelTimeString()	{
		long time = Math.round(getTravelTime());
		
		int mins, secs;
		secs = (int) (time % 60);
		mins = (int) ((time-secs)/60);
		
		return mins + " min, " + secs + " secs";
	}
	public long		getLearningTimeNanos()	{return bestLearningTime;}
	public double	getLearningTimeSecs()	{return getLearningTimeNanos()/Math.pow(10, 9);}
	
	public String getName() {return algorithmName;}
	
	public void 	setDone()				{isDone = true;}
	public boolean	getDone()				{return isDone;}

	public List<Edge> getSolution() 		{return bestSolution;}

	public int getTrialNum() {return bestTrialNum;}
	
	protected void clear() {
		solution.clear();
		bestSolution.clear();
		bestTravelDistances.clear();
		
		isDone = false;
		
		learningTime = bestLearningTime = 0;
		travelDistance = bestTravelDistance = Double.MAX_VALUE;
		
		trialNum = maxTrialNum;
		curTrialNum = 1;
		bestTrialNum = -1;
	}
	
	public void setStart(Node start) 	{this.startNode = start;}
	public void setEnd(Node end) 		{this.endNode = end;}
}
