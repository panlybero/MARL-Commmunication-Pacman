package pacman.entries.ghosts;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Random;
import java.util.Scanner;

import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QFunction;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class SarsaSingleGhost extends RLSingleGhost
{	
	
	private Random rng = new Random();
	private FeatureSet prototype; // Class to use
	public QFunction Qfunction; // Learned policy
	public QFunction m_Qfunction; // Learned messaging policy 
	
	private MOVE[] actions; // Actions possible in the current state
	private Integer[] possible_messages;
	private double[] qvalues; // Q-values for actions in the current state
	private double[] m_qvalues;
	private GhostDepthFeatureSet[] features; // Features for actions in the current state
	private GhostDepthFeatureSet[] m_features;
	private int lastScore; // Last known game score
	private int m_lastScore;
	private int bestActionIndex; // Index of current best action
	private int lastActionIndex; // Index of action actually being taken
	private int bestMessageIndex; // Index of current best message
	private int lastMessageIndex; // Index of message actually being taken
	
	private boolean testMode; // Don't explore or learn or take advice?
	private boolean doUpdate; // Perform a delayed gradient-descent update?
	private double delta1; // First part of delayed update: r-Q(s,a)
	private double delta2; // Second part of delayed update: yQ(s',a')
	
	private double m_delta1; // First part of delayed update: r-Q(s,a) for message
	private double m_delta2; // Second part of delayed update: yQ(s',a') for message
	
	private int scoreSum = 0;
	public double m_scoreSum = 0;
	private double EPSILON = 0.1; // Exploration rate
	private double ALPHA = 0.001;
	private double m_ALPHA = 0.01;
	private double GAMMA = 0.999; // Discount rate
	private double LAMBDA = 0.0; // Backup weighting
	private double m_LAMBDA = 0.0;
	//private double LAMBDA = 0.1;
	public boolean coms = true;
	public boolean debug = false;
	public GHOST name;
	
	public int my_message_index = 0;
	

	/** Initialize the policy. */
	public SarsaSingleGhost(GhostDepthFeatureSet proto, GHOST _name) {
		prototype = proto;
		Qfunction = new QFunction(prototype);
		m_Qfunction = new QFunction(prototype);
		name = _name;

		my_message_index = name.ordinal();
		//System.out.println("Featureset size:" + prototype.size());
	}

	/** Prepare for the first move. */
	public void startEpisode(Game game, boolean testMode) {


		this.testMode = testMode;
		lastScore = 0;
		Qfunction.clearTraces();
		m_Qfunction.clearTraces();
		doUpdate = false;
		delta1 = 0;
		delta2 = 0;
		m_delta1 = 0;
		m_delta2 = 0;
		scoreSum = 0;
		Integer[] initial_messages = new Integer[4];
		for(int k=0;k<4;k++)
		{
			initial_messages[k] = 0;
		}
		evaluateMoves(game, initial_messages);
	}
	
	public void dump()
	{
		
		String s = "";
		for(MOVE m:actions)
		{
			s+=m+" ";
		}
		String st = "";
		for(double m:qvalues)
		{
			st+=m+" ";
		}
		
		String stt = "";
		for(FeatureSet f:features)
		{
			for(double d: ((GhostDepthFeatureSet)f).values)
				stt+=d+" ";
		}
		System.out.println(
				"lastScore = "+lastScore+"\n"+
				"actions = "+s+"\n"+
				"qvalues = "+st+"\n"+
				"lastActionIndex = "+lastActionIndex+"\n"+
				"features = "+stt+"\n"
				
				
				);
		
	}
	
	/** Choose a move. */
	public MOVE getMove(Game game, long timeDue) 
	{
		if (game.doesGhostRequireAction(name))
		{
			//System.out.println("hey");
			return actions[lastActionIndex];
		}
		else
		{

			return MOVE.NEUTRAL;
		}
		
	}
	public Integer getMessage(Game game, long timeDue) 
	{
		if (game.doesGhostRequireAction(name))
		{
			/*
			boolean edible = game.isGhostEdible(name);
			if(edible)
			{
				lastMessageIndex = 1;
				return 1;
			}else
			{
				lastMessageIndex = 0;
				return 0;
			}
			*/
			
			//System.out.println("hey");
			/*
			int curr = game.getGhostCurrentNodeIndex(name);
			int pac = game.getPacmanCurrentNodeIndex();
			int my_dist = game.getShortestPathDistance(curr, pac);
			double mess = 1;
			
			for(GHOST g: GHOST.values())
			{
				
				int oth = game.getGhostCurrentNodeIndex(g);
				/*
				if(g!=name && game.getShortestPathDistance(curr, game.getGhostCurrentNodeIndex(g)) < 20)
				{
					mess=1;
				}
				
				int oth_dist = game.getShortestPathDistance(oth, pac);
				if(oth_dist<my_dist && g!=name)
				{
					mess = 0;
					break;
				}
				
				
				
			}
			lastMessageIndex = (int) mess;
			return (int) mess;
			*/
			//return (int) mess;
			/*
			double dist = game.getDistance(curr, pac, DM.PATH);
			double max_d = Math.sqrt(game.currentMaze.graph.length);
			
			
			if(dist<max_d)
				return 1;
			else
				return 0;
			*/
			
			return possible_messages[lastMessageIndex];
		}
		else
		{

			return possible_messages[lastMessageIndex];
		}
		
		
	}
	/** Override the move choice. */
	public void setMove(MOVE move) {
		lastActionIndex = -1;
		for (int i=0; i<actions.length; i++)
			if (actions[i] == move)
				lastActionIndex = i;
	}
	
	public void processStep(Game game) {
		return; //processStep(game, null);
		
	}
	/** Learn if appropriate, and prepare for the next move. */
	public void processStep(Game game, Integer[] last_messages) {
		
		//if(!game.doesGhostRequireAction(name)) return;
		// Do a delayed gradient-descent update
		if (doUpdate) {
			delta2 = (GAMMA * qvalues[lastActionIndex]);
			m_delta2 = (GAMMA * m_qvalues[lastMessageIndex]);
			//System.out.println("\t"+ qvalues[lastActionIndex]+"\t"+delta1+"\t"+delta2);
			
			Qfunction.updateWeights(ALPHA*(delta1+delta2));
			m_Qfunction.updateWeights(m_ALPHA*(m_delta1+m_delta2));
			//System.out.println(ALPHA*(m_delta1+m_delta2));
			
		}
		
		// Eligibility traces

		Qfunction.decayTraces(GAMMA*LAMBDA);
		Qfunction.addTraces(features[lastActionIndex]);
		
		m_Qfunction.decayTraces(GAMMA*m_LAMBDA);
		m_Qfunction.addTraces(m_features[lastMessageIndex]);
		
		// Q-value correction
		double reward = game.rewards.get(name);
		double coms_reward = game.coms_rewards.get(name);
		scoreSum+=reward;
		m_scoreSum = coms_reward;
		if(reward == 10)
		{
			//System.out.println("sum "+scoreSum);
		}
		lastScore = game.rewards.get(name);
		m_lastScore = game.coms_rewards.get(name);
		
		delta1 = reward - qvalues[lastActionIndex];
		m_delta1 = coms_reward - m_qvalues[lastMessageIndex];

		
		
		
		
		if (!game.gameOver())
			evaluateMoves(game,last_messages);
		
		// Gradient descent update
		if (!testMode) {
			
			// Right away if game is over
			if (game.gameOver()){
				Qfunction.updateWeights(ALPHA*delta1);
				m_Qfunction.updateWeights(m_ALPHA*m_delta1);
			}
			
			// Otherwise delayed (for potential advice)
			else
				doUpdate = true;
		}
	}
	
	public Integer[] getPossibleMessages()
	{
		Integer[] messages = new Integer[2];
		//messages[0] = -1;
		messages[0] = 0;
		messages[1] = 1;
		return messages;
	}

	/** Compute predictions for moves in this state. */ //AND messages
	private void evaluateMoves(Game game)
	{
		evaluateMoves(game,null);
	}
	
	private void evaluateMoves(Game game, Integer[] last_messages) {
		//if(!game.doesGhostRequireAction(name)) return;
		actions = game.getPossibleMoves(game.getGhostCurrentNodeIndex(name));
		
		possible_messages = getPossibleMessages();
		
		
		features = new GhostDepthFeatureSet[actions.length];
		m_features = new GhostDepthFeatureSet[possible_messages.length];

		for (int i=0; i<actions.length; i++){
			
			
			
			features[i] = new GhostDepthFeatureSet(name); // give ghost name
			
			features[i] = features[i].extract_with_messages(game, actions[i], last_messages);
			
			


			if (debug){
				System.out.print("Features for action "+actions[i]+"\t");
				for (int t = 0; t < features[i].size();t++)
					System.out.print("\t"+features[i].get(t));
				System.out.println();
			}
		}
		
		qvalues = new double[actions.length];
		for (int i=0; i<actions.length; i++){
			qvalues[i] = Qfunction.evaluate(features[i]);
//			System.out.print(actions[i] + " ");
//			System.out.print(qvalues[i] + " ");
			if (debug){
				System.out.println("Q value for action "+actions[i]+":\t"+qvalues[i]);
			}
		}
		
		bestActionIndex = 0;
		for (int i=0; i<actions.length; i++)
			if (qvalues[i] > qvalues[bestActionIndex])
				bestActionIndex = i;
		
		boolean edible = game.isGhostEdible(name);
		int curr_node = game.getGhostCurrentNodeIndex(name);
		MOVE right_move = null;
		if(!edible) game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(name), DM.PATH);
		if(edible) game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(name), DM.PATH);
		//if(actions[bestActionIndex] == right_move)
		// Explore or exploit
		if (!testMode && rng.nextDouble() < EPSILON)
			lastActionIndex = rng.nextInt(actions.length);
		else
			lastActionIndex = bestActionIndex;
		
		
		if(coms)
		for(int i=0;i<possible_messages.length;i++)
		{
			m_features[i] = new GhostDepthFeatureSet(name); // give ghost name
			if(coms) 
			{
				Integer[] messages = last_messages;
				messages[my_message_index] = possible_messages[i];


				m_features[i] = m_features[i].extract_with_messages(game, actions[lastActionIndex], messages); //features for each message given the last action i took 

			}
		}
		
		m_qvalues = new double[possible_messages.length];
		if(coms)
		for (int i=0; i<possible_messages.length; i++){
			m_qvalues[i] = m_Qfunction.evaluate(m_features[i]);
//			System.out.print(actions[i] + " ");
//			System.out.print(qvalues[i] + " ");
			if (debug){
				System.out.println("Q value for action "+possible_messages[i]+":\t"+m_qvalues[i]);
			}
		}
		
		bestMessageIndex = 0;
		for (int i=0; i<possible_messages.length; i++)
			if (m_qvalues[i] > m_qvalues[bestMessageIndex])
				bestMessageIndex = i;
		
		//switch messages
		boolean counterfacts = true;
		if(counterfacts)
		{


			for (GHOST ghost: GHOST.values())
			{
				if(ghost!=name)
				{
					Integer[] counterfactuals = last_messages;
					counterfactuals[ghost.ordinal()] = 1- counterfactuals[ghost.ordinal()];
					
					GhostDepthFeatureSet feats = new GhostDepthFeatureSet(name).extract_with_messages(game, actions[bestActionIndex],counterfactuals);
					
					double q = Qfunction.evaluate(feats);
					if(q!=qvalues[bestActionIndex])
					{
						game.coms_rewards.put(ghost, 10);
					}
				}
			}
			
			


		}
		
		
		if (!testMode && rng.nextDouble() < EPSILON)
			lastMessageIndex = rng.nextInt(possible_messages.length);
		else
			lastMessageIndex = bestMessageIndex;
		
		
		if (debug){
			Scanner scanner = new Scanner(new InputStreamReader(System.in));
			String input = scanner.nextLine();
		}
	}
	
	/** Get the current possible moves. */
	public MOVE[] getMoves() 
	{
		return actions;
	}
	
	/** Get the current Q-value array. */
	public double[] getQValues() {
		return qvalues;
	}
	
	/** Get the current features for an action. */
	public FeatureSet getFeatures(MOVE move) {
		int actionIndex = -1;
		for (int i=0; i<actions.length; i++)
			if (actions[i] == move)
				actionIndex = i;
		return features[actionIndex];
	}
	
	/** Save the current policy to a file. */
	public void savePolicy(String filename) {
		Qfunction.save(filename);
	}

	/** Return to a policy from a file. */
	public void loadPolicy(String filename) {
		Qfunction = new QFunction(prototype, filename);
	}
	
	public QFunction getQFunction(){
		return Qfunction;
	}

	@Override
	public int getScore() {
		// TODO Auto-generated method stub
		return scoreSum;
	}

	@Override
	public void processStep(Game game, EnumMap<GHOST, Integer> messages) {
		
		Integer[] mess = new Integer[4];
		
		for(GHOST ghost:GHOST.values())
		{
			mess[ghost.ordinal()] = messages.get(ghost);
			//System.out.println(mess[ghost.ordinal()]);
		}
		processStep(game,mess);
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getComsScore() {
		// TODO Auto-generated method stub
		return m_scoreSum;
	}
	

}
