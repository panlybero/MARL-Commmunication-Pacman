package pacman.entries.ghosts;


import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Random;
import java.util.Scanner;

import pacman.controllers.Controller;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QFunction;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

import static pacman.game.Constants.*;

/**
 * Ghost team controller as part of the starter package - simply upload this file as a zip called
 * MyGhosts.zip and you will be entered into the rankings - as simple as that! Feel free to modify 
 * it or to start from scratch, using the classes supplied with the original software. Best of luck!
 * 
 * This ghost controller does the following:
 * 1. If edible or Ms Pac-Man is close to power pill, run away from Ms Pac-Man
 * 2. If non-edible, attack Ms Pac-Man with certain probability, else choose random direction
 */
public final class RLGhosts extends Controller<EnumMap<GHOST,MOVE>>
{	
	private final static float CONSISTENCY=1.0f;	//attack Ms Pac-Man with this probability
	private final static int PILL_PROXIMITY=15;		//if Ms Pac-Man is this close to a power pill, back away
	
	public EnumMap<GHOST,MOVE[]> actions = new EnumMap<GHOST,MOVE[]>(GHOST.class); //actions possible at the current state
	public EnumMap<GHOST,double[]> qvalues = new EnumMap<GHOST,double[]>(GHOST.class); // Q-values for actions in the current state
	private EnumMap<GHOST,FeatureSet[]> features= new EnumMap<GHOST,FeatureSet[]>(GHOST.class); // Features for actions in the current state
	private FeatureSet prototype; // Class to use
	private QFunction Qfunction; // Learned policy. Centralized learning
	
	public EnumMap<GHOST,QFunction> Qfunctions = new EnumMap<GHOST,QFunction>(GHOST.class);
	
	private EnumMap<GHOST,Integer> lastScore =new EnumMap<GHOST,Integer>(GHOST.class);; // Last known game score
	private EnumMap<GHOST,Integer> bestActionIndex = new EnumMap<GHOST,Integer>(GHOST.class);; // Index of current best action
	private EnumMap<GHOST,Integer> lastActionIndex = new EnumMap<GHOST,Integer>(GHOST.class);; // Index of action actually being taken
	
	public EnumMap<GHOST,Boolean> can_act = new EnumMap<GHOST,Boolean>(GHOST.class);
	
	private boolean testMode; // Don't explore or learn or take advice?
	private EnumMap<GHOST,Boolean> doUpdate = new EnumMap<GHOST,Boolean>(GHOST.class);; // Perform a delayed gradient-descent update?
	private EnumMap<GHOST,Double> delta1 = new EnumMap<GHOST,Double>(GHOST.class); // First part of delayed update: r-Q(s,a)
	private EnumMap<GHOST,Double> delta2 = new EnumMap<GHOST,Double>(GHOST.class); // Second part of delayed update: yQ(s',a')
	private boolean debug = false;
	
	private double EPSILON = 0.1; // Exploration rate
	private double ALPHA = 0.01; // Learning rate
	private double GAMMA = 0.999; // Discount rate
	private double LAMBDA = 0.9; // Backup weighting
	
	public boolean pacman_caught = false;
	
	Random rnd=new Random();
	
	EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	
	public double[] episodeData() { // Override to add data to learning curves
		double[] data = new double[0];
		return data;
	}
	public RLGhosts(FeatureSet proto) 
	{
		prototype = proto;
		//Qfunction = new QFunction(prototype);
		for (GHOST ghost: GHOST.values())
		{
			lastScore.put(ghost,0);
			bestActionIndex.put(ghost,0);
			lastActionIndex.put(ghost,0);
			delta1.put(ghost,0.0);
			delta2.put(ghost,0.0);
			qvalues.put(ghost, new double[4]);
			can_act.put(ghost,false);
			doUpdate.put(ghost,false);
			
			Qfunctions.put(ghost, new QFunction(prototype));
			//EnumMap<GHOST,
			//actions.compute(ghost, new MOVE[]);
		}


	}
	public void startEpisode(Game game, boolean testMode) {
		this.testMode = testMode;
		for (GHOST ghost : GHOST.values())
		{
			lastScore.put(ghost, 0);
			delta1.put(ghost,0.0);
			delta2.put(ghost,0.0);
			//qvalues.put(ghost, new double[4]);
			doUpdate.put(ghost,false);
		}

		//Qfunction.clearTraces();
		
		
		evaluateMoves(game);

	}
public void processStep(Game game) 
{
	//System.out.println(doUpdate);
	for (GHOST ghost: GHOST.values())
	{
		if (can_act.get(ghost))
		{
			
		
		// Do a delayed gradient-descent update
			if (doUpdate.get(ghost) && qvalues.get(ghost).length!=0) 
			{
				//System.out.println("test");
				delta2.put(ghost,  (GAMMA * qvalues.get(ghost)[lastActionIndex.get(ghost)]));
				//Qfunction.updateWeights(ALPHA*(delta1.get(ghost)+delta2.get(ghost)));
				for(double w: Qfunctions.get(ghost).weights)
				{
					//System.out.print(w+" ");
					
				}
				//System.out.println();
				//System.out.println(ALPHA*(delta1.get(ghost)+delta2.get(ghost)));
				double update = ALPHA*(delta1.get(ghost)+delta2.get(ghost));//Math.min(1, Math.max(-1,ALPHA*(delta1.get(ghost)+delta2.get(ghost))));//
				//System.out.println(update);
				Qfunctions.get(ghost).updateWeights(update);
				//Qfunctions.get(ghost).updateWeights(ALPHA*(delta1.get(ghost)+delta2.get(ghost)));
				for(double w: Qfunctions.get(ghost).weights)
				{
					//System.out.print(w+" ");
					
				}
				//System.out.println();
				// Eligibility traces
				//Qfunction.decayTraces(GAMMA*LAMBDA);
				Qfunctions.get(ghost).decayTraces(GAMMA*LAMBDA);
				//Qfunction.addTraces(features.get(ghost)[lastActionIndex.get(ghost)]);
				Qfunctions.get(ghost).addTraces(features.get(ghost)[lastActionIndex.get(ghost)]);
				// Q-value correction
				
				int reward = game.rewards.get(ghost);
				if(reward == 10)
				{
					pacman_caught = true;
				}
				
				lastScore.put(ghost, reward);
				//System.out.println(lastActionIndex.get(ghost));
				delta1.put(ghost, reward - qvalues.get(ghost)[lastActionIndex.get(ghost)]);
				//System.out.println(delta1.get(ghost)+delta2.get(ghost));
			}
		}
	}
		if (!game.gameOver())
		{
			evaluateMoves(game);
		
		}
			
		for (GHOST ghost: GHOST.values())
		{
		// Gradient descent update
			if (!testMode) {
				
				// Right away if game is over
				if (game.gameOver()){
				
					if(can_act.get(ghost))
					{
						//Qfunction.updateWeights(ALPHA*delta1.get(ghost)); // make delta enum and update for each ghost
						Qfunctions.get(ghost).updateWeights(ALPHA*delta1.get(ghost));
						
					}
					
				}
				
				// Otherwise delayed (for potential advice)
				else
					if(can_act.get(ghost))
					{
						doUpdate.put(ghost, true);
					}
			}
		}
	}

	public void evaluateMoves(Game game) {
		//debug = true;
		for (GHOST ghost : GHOST.values())
		{
			if(can_act.get(ghost))
			{
				actions.put(ghost, game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost)));
				//System.out.println(actions.get(ghost).length);
				GhostFeatures[] feats = new GhostFeatures[actions.get(ghost).length];
				
				for (int i=0; i<actions.get(ghost).length; i++)
				{
					feats[i] = new GhostFeatures(ghost);
	
				}
				
				for (int i=0; i<actions.get(ghost).length; i++){
					//System.out.println(((GhostFeatures)feats[i]).ghost);
					feats[i] = feats[i].extract(game, actions.get(ghost)[i]); //= prototype.extract(game, actions.get(ghost)[i]);
					//System.out.println(feats[i].get(1));
					if (debug){
						System.out.print("Features for action "+actions.get(ghost)[i]+"\t");
						for (int t = 0; t < feats[i].size();t++)
							System.out.print("\t"+feats[i].get(t));
						System.out.println();
					}
				}
				features.put(ghost,feats);
				
				double[] qvals = new double[actions.get(ghost).length];
				//qvalues 
				for (int i=0; i<actions.get(ghost).length; i++){
					qvals[i] = Qfunctions.get(ghost).evaluate(feats[i]);
					//qvals[i] = Qfunction.evaluate(feats[i]);
	//				System.out.print(actions[i] + " ");
	//				System.out.print(qvalues[i] + " ");
					if (debug){
						System.out.println(ghost+" Q value for action "+actions.get(ghost)[i]+":\t"+qvals[i]);
					}
				}
				//System.out.println();
				qvalues.put(ghost,qvals);
				if(qvals.length == 0)
				{
					//System.out.println("0 length");
				}
				bestActionIndex.put(ghost, 0);
				
				for (int i=0; i<actions.get(ghost).length; i++)
					if (qvals[i] > qvals[bestActionIndex.get(ghost)])
						bestActionIndex.put(ghost, i);
	
				// Explore or exploit
				if (!testMode && rnd.nextDouble() < EPSILON && actions.get(ghost).length!=0) {
					//System.out.println(actions.get(ghost).length);
					lastActionIndex.put(ghost,rnd.nextInt(actions.get(ghost).length));
					}
				else
					if(actions.get(ghost)[0] != MOVE.NEUTRAL)
					{
						lastActionIndex.put(ghost,bestActionIndex.get(ghost));
					}
				
				if (debug){
					Scanner scanner = new Scanner(new InputStreamReader(System.in));
					String input = scanner.nextLine();
				}
			}
			else
			{
				MOVE[] neut = {MOVE.NEUTRAL};
				actions.put(ghost,neut);
			}
		}
		
	}
	
	
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue) // Retrieves calculated moves for every ghost.
	{
		//evaluateMoves(game)
		for(GHOST ghost : GHOST.values())	//for each ghost
		{	
			
			if(game.doesGhostRequireAction(ghost))		//if ghost requires an action
			{
				//retreat from Ms Pac-Man if edible or if Ms Pac-Man is close to power pill
				//System.out.println(actions.get(ghost).length);
				
				myMoves.put(ghost,actions.get(ghost)[lastActionIndex.get(ghost)]);
				
			}
		}

		return myMoves;
	}
	
    //This helper function checks if Ms Pac-Man is close to an available power pill
	private boolean closeToPower(Game game)
    {
    	int[] powerPills=game.getPowerPillIndices();
    	
    	for(int i=0;i<powerPills.length;i++)
    		if(game.isPowerPillStillAvailable(i) && game.getShortestPathDistance(powerPills[i],game.getPacmanCurrentNodeIndex())<PILL_PROXIMITY)
    			return true;

        return false;
    }
}