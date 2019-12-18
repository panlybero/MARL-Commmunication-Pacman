package pacman;

import pacman.game.Constants;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;

import javax.imageio.ImageIO;

import pacman.controllers.examples.StarterPacMan;
import pacman.entries.ghosts.*;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.CustomFeatureSet;
import pacman.entries.pacman.CustomFeatureSetV2;
import pacman.entries.pacman.DepthFeatureSet;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QPacMan;
import pacman.entries.pacman.RLPacMan;
import pacman.entries.pacman.SarsaPacMan;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.internal.Maze;
import pacman.utils.DataFile;
import pacman.utils.LearningCurve;
import pacman.utils.Stats;

public class RunTest {
	
// Note on notation:
// custom refers to the CustomFeatureSet (using something else defaults to DepthFeatureSet)
// S refers to SarsaPacMan (using something else defaults to QPacMan)
	
	
/* 	4 options for RLPacMan learner type
 * 		- teacher: teacher RLPacMan loads policy
 * 		- independent: student starts from scratch
 * 		- transfer: student loads policy
 * 		- (baseline, advise, correct, predict): uses teacher+independent and a strategy for teacher 
 * 
 * 	Notes on folder names in mydata
 * 		- mydata/TEACHER/STUDENT/LEARNER-TYPE_MAZE-NUM/
 * 
 * 
 * 
 */	
	
	
	//options for features include depth or custom, options for algoirithms are S or Q
	//i.e., customQ, customS, depthS, depthQ are the legal values for STUDENT
	public static String STUDENT = "depthS"; // Student feature set and algorithm
	
	
	public static int LENGTH = 500; // 10; // 100; // Points per curve
	public static int TEST = 1; //30; // Test episodes per point
	public static int TRAIN = 10; //10 // Train episodes per point

	public static Random rng = new Random();
	
	// Ghost "policies"
	public static StandardGhosts ghostsS = new StandardGhosts();
    public static RandomGhosts ghostsR = new RandomGhosts();
    public static ChaserGhosts ghostsC = new ChaserGhosts();
    public static LineGhosts ghostsL = new LineGhosts();
    public static RLGhosts ghostsRL;
    
    public static int ghostType = 4;		// 0 = RandomGhosts, 1 = StandardGhosts, 2 = ChaserGhosts , 4 = RL
    
    // Task parameters
    public static int mazeNum = 1;
    // Controls how fast ghosts move when pacman has eaten power pill. Lower numbers = slower (1 = frozen). 
    public static int ghostSlowdown = 4;
    public static int nGhosts = 4;
    static EnumMap<GHOST, Integer> messages = new EnumMap<GHOST, Integer>(GHOST.class); 
    
    
    static EnumMap<GHOST,Double> sum_per_ghost = new EnumMap<GHOST,Double>(GHOST.class);
    static EnumMap<GHOST,Double[]> score_per_ghost = new EnumMap<GHOST,Double[]>(GHOST.class);
    
    public static Constants defaultConstants;
    
	/**
	 * Run experiments.
	 */
	public static void main(String[] args) {
		try {
			demo();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void demo() throws IOException{
		defaultConstants = new Constants();
		
		//train params
		LENGTH = 800; //how many train episodes
		TEST = 5;
		
		mazeNum = 2; //which maze;
		ghostType = 4; //which of three types of ghosts
		ghostSlowdown = 3; //ghost speed when edible
		nGhosts = 4; //how many ghosts
		
		defaultConstants.MAZE_NUM = mazeNum; //which maze;
		defaultConstants.GHOST_TYPE = ghostType; //which of three types of ghosts
		defaultConstants.GHOST_SPEED_REDUCTION = ghostSlowdown; //ghost speed
		defaultConstants.NUM_GHOSTS = nGhosts; //how many ghosts
		//defaultConstants.NUM_LIVES = 3;
		//defaultConstants.nodeNames[4]="mymaze";
		//defaultConstants.distNames[4]="dmymaze";

		//increase delay for watching
		defaultConstants.DELAY = defaultConstants.DELAY*5;
		int runs = 5;
		//train
		//double[][] all_train_scores = new double[runs][LENGTH];
		
		RLPacMan pacman = create("independent");
		
		String demo_name = "independent_temp";
		//train(demo_name,0,true,false, "pacDepth3");
		//((SarsaPacMan)pacman).loadPolicy("random_ghost_pacman.txt");
		
		for(GHOST g:GHOST.values())
		{
			score_per_ghost.put(g,new Double[LENGTH]);
		}
		
		
		double[] avg_scores = new double[LENGTH];
		Double[] avg_ghost1 = new Double[LENGTH];
		Double[] avg_ghost2 = new Double[LENGTH];
		Double[] avg_ghost3 = new Double[LENGTH];
		Double[] avg_ghost4 = new Double[LENGTH];
		
		for(int i=0;i<avg_ghost1.length;i++)
		{
			avg_ghost1[i] = 0.0;
			avg_ghost2[i] = 0.0;
			avg_ghost3[i] = 0.0;
			avg_ghost4[i] = 0.0;
		}
		System.out.println(avg_ghost1[0]);
		for(int run=0;run<runs;run++)
		{
			System.out.println(run);
			double[] scores  = train_ghosts("test",0,false ,false, false);
			Double[] tmp1 = score_per_ghost.get(GHOST.BLINKY);
			Double[] tmp2 = score_per_ghost.get(GHOST.INKY);
			Double[] tmp3 = score_per_ghost.get(GHOST.PINKY);
			Double[] tmp4 = score_per_ghost.get(GHOST.SUE);
			
			for(int i=0;i<scores.length;i++)
			{
				avg_scores[i]+=scores[i];
				
				avg_ghost1[i] += tmp1[i];
				avg_ghost2[i] += tmp2[i];
				avg_ghost3[i] += tmp3[i];
				avg_ghost4[i] += tmp4[i];
				
			}
			
		
		}
		String out = "";
		String out_g1 = "";
		String out_g2 = "";
		String out_g3 = "";
		String out_g4 = "";
		for(int i=0;i<avg_scores.length;i++)
		{
			avg_scores[i]/=runs;
			out+=avg_scores[i]+"\n";
			
			avg_ghost1[i]/=runs;
			out_g1+=avg_ghost1[i]+"\n";		
			
			avg_ghost2[i]/=runs;
			out_g2+=avg_ghost2[i]+"\n";		
			
			avg_ghost3[i]/=runs;
			out_g3+=avg_ghost3[i]+"\n";		
			
			avg_ghost4[i]/=runs;
			out_g4+=avg_ghost4[i]+"\n";		
			
			
			//System.out.println(avg_scores[i]);
		}
		
		String filename = "test";
		
		//BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\test.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\"+filename+".txt"));
	    writer.write(out);
	    writer.close();
	    
	  //BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\test.txt"));
	  		BufferedWriter writer_g1 = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\"+filename+"_g1.txt"));
	  	    writer_g1.write(out_g1);
	  	    writer_g1.close();
	  	    
	  	//BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\test.txt"));
			BufferedWriter writer_g2 = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\"+filename+"_g2.txt"));
		    writer_g2.write(out_g2);
		    writer_g2.close();
		    
		  //BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\test.txt"));
			BufferedWriter writer_g3 = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\"+filename+"_g3.txt"));
		    writer_g3.write(out_g3);
		    writer_g3.close();
		    
		  //BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\test.txt"));
			BufferedWriter writer_g4 = new BufferedWriter(new FileWriter("D:\\work\\Classes\\Tufts\\Reinforcement Learning\\Project\\"+filename+"_g4.txt"));
		    writer_g4.write(out_g4);
		    writer_g4.close();
		System.out.println("----------DONE----------");
		
		/*
		EnumMap<GHOST, RLSingleGhost> ghosts = create_ghosts(4);
		for(GHOST ghost:GHOST.values())
		{
			((SarsaSingleGhost)ghosts.get(ghost)).loadPolicy(ghost+"_policy.txt");
		}
		watch(ghosts,false);
		*/
	}
	
	
	
	public static String generateDirectoryName(String learner, 
						int mazeNum_i, int ghostType_i, 
						int ghostSlowdown_i, int nGhosts){
		return new String(learner+"_maze"+mazeNum_i+"_type"+ghostType_i+"_slow"+ghostSlowdown_i+"_ghosts"+nGhosts+"");
	}

	/** Set up a learner. */
	public static RLPacMan create(String learner) {
		
		//create feature set based on constant STUDENT
		FeatureSet studentProto = STUDENT.startsWith("custom") ? new CustomFeatureSet() : new DepthFeatureSet();

		// Lone student
		if (learner.startsWith("independent")) {
			return STUDENT.endsWith("S") ? new SarsaPacMan(studentProto) : new QPacMan(studentProto);
		}
		
		return null;
	}
	
	public static EnumMap<GHOST, RLSingleGhost> create_ghosts(int nGhosts) {
		
		//create feature set based on constant STUDENT
		//GhostFeatures studentProto = new GhostFeatures(); //STUDENT.startsWith("custom") ? new CustomFeatureSet() : new DepthFeatureSet();
		
		EnumMap<GHOST, RLSingleGhost> ghosts = new EnumMap<GHOST, RLSingleGhost>(GHOST.class);
		
		for (GHOST ghost: GHOST.values())
		{
			GhostDepthFeatureSet studentProto = new GhostDepthFeatureSet(ghost);
			ghosts.put(ghost, new SarsaSingleGhost(studentProto, ghost));
		}
		
		return ghosts;
		// Lone student
		//if (learner.startsWith("independent")) {
			//return STUDENT.endsWith("S") ? new SarsaPacMan(studentProto) : new QPacMan(studentProto);
		//}
		
		//return null;
	}
	
	/** Set up learning ghost */
	
	
	
	/** Generate learning curves. */
	/** Generate learning curves. */
	public static void train(String learner, int start, boolean watchAtEnd, boolean watchDuring, String pac_name) {
		
		String directory = generateDirectoryName(learner,mazeNum,ghostType,ghostSlowdown,nGhosts);
		
		
		// This just sets up the student or teacher with the specified feature set and learning algorithm
		RLPacMan pacman = create(learner);
			
		// First point
		double[] initialData = pacman.episodeData();		// Starts off empty
			
		// evaluate the random policy
		double [] eval_result = evaluate(pacman, TEST);
		
		System.out.println("score at start = "+eval_result[0]);
			
		double initialScore = eval_result[0];		// This returns the average score over TEST number of games
		double initialTime = eval_result[1];		// This returns the average score over TEST number of games
			
		// Rest of the points
		int num_train_episodes = 0;
		
		// for each train episode...
		for (int x=1; x<=LENGTH; x++) {
			double[] data = new double[initialData.length];
				
			
			//make PacMan play an episode
			episode(pacman);	
			num_train_episodes++;
					
					
			double[] episodeData = pacman.episodeData();
			for (int d=0; d<data.length; d++)
				data[d] += episodeData[d];
			
				
				
			// now evaluate 	
			double [] eval_result2 =evaluate(pacman, TEST) ;
			double score = eval_result2[0];		// This returns the average score over TEST number of games
			double time = eval_result2[1];		// This returns the average number of game ssteps over TEST number of games
		
			//System.out.println("e"+num_train_episodes+" score:\t"+score+" ");
			
			System.out.println(""+num_train_episodes+","+score);
			
			if (watchDuring){
				((SarsaPacMan)pacman).debug=false;
				watch(pacman,true);
				((SarsaPacMan)pacman).debug=false;
			}
			
		}
		
		if (watchAtEnd)
			watch(pacman, false);
		
		
		System.out.println("Done.");
		pacman.getQFunction().save(pac_name+"_"+"random_ghost_pacman.txt");
	}
public static void train_ghosts(String learner, RLPacMan pacman, int start, boolean watchAtEnd, boolean watchDuring)
{
	train_ghosts(learner, pacman, start, watchAtEnd, watchDuring, false);
}
public static double[] train_ghosts(String learner, int start, boolean watchAtEnd, boolean watchDuring, boolean load_ghosts) {
	
	String directory = generateDirectoryName(learner,mazeNum,ghostType,ghostSlowdown,nGhosts);
	
	EnumMap<GHOST, RLSingleGhost> ghosts = create_ghosts(defaultConstants.NUM_GHOSTS);
	if (load_ghosts)
	{
		for(GHOST ghost:GHOST.values())
		{
			((SarsaSingleGhost)ghosts.get(ghost)).loadPolicy(ghost+"_policy.txt");
		}
	}
	//ghostsRL = ghosts;
	// This just sets up the student or teacher with the specified feature set and learning algorithm

	// First point
	double[] initialData = new double[0];//ghosts.values()[0].episodeData();		// Starts off empty
	double[] scores = new double[LENGTH];
	// evaluate the random policy
	double [] eval_result = evaluate_SingleGhosts(ghosts, TEST, true);
	
	System.out.println("score at start = "+eval_result[0]);
		
	double initialScore = eval_result[0];		// This returns the average score over TEST number of games
	double initialTime = eval_result[1];		// This returns the average score over TEST number of games
		
	// Rest of the points
	int num_train_episodes = 0;
	
	for(GHOST g:GHOST.values())
	{
		score_per_ghost.put(g,new Double[LENGTH]);
	}
	// for each train episode...
	for (int x=1; x<=LENGTH; x++) {
		double[] data = new double[initialData.length];
			
		
		//make Ghosts play an episode
		
		EnumMap<GHOST,Integer> rewards = episode_rewards(ghosts,true);
		
		
		
		int weight_length = ghosts.get(GHOST.BLINKY).getQFunction().weights.length;
		
		//System.out.println(rewards);
		double max_reward = rewards.get(GHOST.BLINKY);
		GHOST best = GHOST.BLINKY;
		for(GHOST ghost : GHOST.values())
		{
			
			
			if(rewards.get(ghost)>= max_reward)
			{
				max_reward = rewards.get(ghost);
				best = ghost;
			}
		}
		double[] shared_weights = ghosts.get(best).getQFunction().weights; //get best weights.
		
		for(GHOST ghost : GHOST.values())
		{
			//((SarsaSingleGhost)ghosts.get(ghost)).Qfunction.weights = shared_weights;
		}
		
		/*
		double[][] all_weights = new double[4][weight_length];
		int i = 0;
		for(GHOST ghost :GHOST.values())
		{
			all_weights[i] = ghosts.get(ghost).getQFunction().weights;
			i+=1;
		}
		*/
		num_train_episodes++;
				
				
		double[] episodeData = new double[0];//ghosts.get()episodeData();
		for (int d=0; d<data.length; d++)
			data[d] += episodeData[d];
		
			
			
		// now evaluate 	
		double [] eval_result2 =evaluate_SingleGhosts(ghosts, TEST,true) ;
		
		for(GHOST g:GHOST.values())
		{
			Double[] tmp = score_per_ghost.get(g);
			tmp[num_train_episodes-1] = sum_per_ghost.get(g);
			score_per_ghost.put(g,tmp);
		}
		
		double score = eval_result2[0];		// This returns the average score over TEST number of games
		double time = eval_result2[1];		// This returns the average number of game ssteps over TEST number of games
	
		//System.out.println("e"+num_train_episodes+" score:\t"+score+" ");
		
		//System.out.println(""+num_train_episodes+","+score);
		
		scores[num_train_episodes-1]=score;
		
		if(num_train_episodes-1 == 550)
		{
			for(int k=0;k<4;k++)
				System.out.print(ghosts.get(GHOST.BLINKY).getQFunction().weights[k] + " ");
			System.out.println();
		}
		
		if (watchDuring){
			//((SarsaPacMan)pacman).debug=false;
			watch(ghosts,true);
			//((SarsaPacMan)pacman).debug=false;
		}
		
	}
	
	if (watchAtEnd)
		watch( ghosts, false);
	
	
	System.out.println("Done.");
	double[] weights = ghosts.get(GHOST.BLINKY).getQFunction().weights;
	double[] m_weights = ((SarsaSingleGhost)ghosts.get(GHOST.BLINKY)).m_Qfunction.weights;
	for (double w : weights)
	{
		System.out.print(w+" ");
	}
	System.out.println("\n---------coms----------");
	for (double w : m_weights)
	{
		System.out.print(w+" ");
	}
	System.out.println();
	for(GHOST ghost: GHOST.values())
	{
		ghosts.get(ghost).getQFunction().save(ghost+"_policy.txt");
	}
	return scores;
	
}
public static void train_ghosts(String learner, RLPacMan pacman, int start, boolean watchAtEnd, boolean watchDuring, boolean load_ghosts) {
		
		String directory = generateDirectoryName(learner,mazeNum,ghostType,ghostSlowdown,nGhosts);
		
		EnumMap<GHOST, RLSingleGhost> ghosts = create_ghosts(defaultConstants.NUM_GHOSTS);
		if (load_ghosts)
		{
			for(GHOST ghost:GHOST.values())
			{
				((SarsaSingleGhost)ghosts.get(ghost)).loadPolicy(ghost+"_policy.txt");
			}
		}
		//ghostsRL = ghosts;
		// This just sets up the student or teacher with the specified feature set and learning algorithm

		// First point
		double[] initialData = new double[0];//ghosts.values()[0].episodeData();		// Starts off empty
		
		// evaluate the random policy
		double [] eval_result = evaluate_SingleGhosts(pacman, ghosts, TEST);
		
		System.out.println("score at start = "+eval_result[0]);
			
		double initialScore = eval_result[0];		// This returns the average score over TEST number of games
		double initialTime = eval_result[1];		// This returns the average score over TEST number of games
			
		// Rest of the points
		int num_train_episodes = 0;
		
		// for each train episode...
		for (int x=1; x<=LENGTH; x++) {
			double[] data = new double[initialData.length];
				
			
			//make PacMan play an episode
			episode(pacman, ghosts);	
			num_train_episodes++;
					
					
			double[] episodeData = new double[0];//ghosts.get()episodeData();
			for (int d=0; d<data.length; d++)
				data[d] += episodeData[d];
			
				
				
			// now evaluate 	
			double [] eval_result2 =evaluate_SingleGhosts(pacman,ghosts, TEST) ;
			double score = eval_result2[0];		// This returns the average score over TEST number of games
			double time = eval_result2[1];		// This returns the average number of game ssteps over TEST number of games
			double comsScore = eval_result2[2];
			//System.out.println("e"+num_train_episodes+" score:\t"+score+" ");
			
			System.out.println(""+num_train_episodes+","+score);
			
			if (watchDuring){
				((SarsaPacMan)pacman).debug=false;
				watch(pacman,ghosts,true);
				((SarsaPacMan)pacman).debug=false;
			}
			
		}
		
		if (watchAtEnd)
			watch(pacman, ghosts, false);
		
		
		System.out.println("Done.");
		double[] weights = ghosts.get(GHOST.BLINKY).getQFunction().weights;
		for (double w : weights)
		{
			System.out.println(w);
		}
		for(GHOST ghost: GHOST.values())
		{
			ghosts.get(ghost).getQFunction().save(ghost+"_policy.txt");
		}
		
	}


public static EnumMap<GHOST,Integer> episode_rewards(EnumMap<GHOST,RLSingleGhost> ghosts) {
	return episode_rewards(ghosts,false);
}


public static EnumMap<GHOST,Integer> episode_rewards(EnumMap<GHOST,RLSingleGhost> ghosts, boolean coms) {

	
	
	Game game = new Game(rng.nextLong(), defaultConstants);
	StarterPacMan pacman = new StarterPacMan();
	
	
	
	for (GHOST ghost: GHOST.values())
	{
		messages.put(ghost,0);
		ghosts.get(ghost).startEpisode(game, false);
		
	}
	while(!game.gameOver()) {
		game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMoveAndMessage(game, game.constants.GHOST_TYPE,ghosts));				
		
		for (GHOST ghost: GHOST.values())
		{	
			if(coms)
			{
				ghosts.get(ghost).processStep(game,messages);
				
			}	
			else
				ghosts.get(ghost).processStep(game);
			
		}
		
		
	}
	//System.out.println(game.rewards);
	return game.rewards;
}


public static void episode(EnumMap<GHOST,RLSingleGhost> ghosts) {

	Game game = new Game(rng.nextLong(), defaultConstants);
	StarterPacMan pacman = new StarterPacMan();
	for (GHOST ghost: GHOST.values())
	{
		
		ghosts.get(ghost).startEpisode(game, false);
		
	}
	while(!game.gameOver()) {
		game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE,ghosts));				
		
		for (GHOST ghost: GHOST.values())
		{
			ghosts.get(ghost).processStep(game);
			
		}
		
		
	}
}

public static void episode(RLPacMan pacman, EnumMap<GHOST,RLSingleGhost> ghosts) {

		Game game = new Game(rng.nextLong(), defaultConstants);
		pacman.startEpisode(game, true);
		for (GHOST ghost: GHOST.values())
		{
			
			ghosts.get(ghost).startEpisode(game, false);
			
		}
		while(!game.gameOver()) {
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE,ghosts));				
			pacman.processStep(game);
			
			for (GHOST ghost: GHOST.values())
			{
				ghosts.get(ghost).processStep(game);
				
			}
			
			
		}
	}
	/** Train a learner for one more episode. */
// Basically does the same thing as evaluate but doesn't return the score	
	public static void episode(RLPacMan pacman, RLGhosts ghosts) {

		Game game = new Game(rng.nextLong(), defaultConstants);
		pacman.startEpisode(game, true);
		ghosts.startEpisode(game, false);
		while(!game.gameOver()) {
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE));				
			pacman.processStep(game);
			
			for (GHOST ghost : GHOST.values())
			{
				boolean can_act = game.doesGhostRequireAction(ghost);
				ghosts.can_act.put(ghost, can_act);
				
			}
			
			ghosts.processStep(game);
			
		}
	}
	public static void episode(RLPacMan pacman) {

		Game game = new Game(rng.nextLong(), defaultConstants);
		pacman.startEpisode(game, false);

		while(!game.gameOver()) {
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE));				
			pacman.processStep(game);
		}
	}

public static double[] evaluate_SingleGhosts(EnumMap<GHOST,RLSingleGhost> ghosts, int width)
{
	return evaluate_SingleGhosts(ghosts,width,false);
}
			
public static double[] evaluate_SingleGhosts(EnumMap<GHOST,RLSingleGhost> ghosts, int width, boolean coms) {
		
		double[] scoreAndTime = new double[3+4];	
		
		for(GHOST ghost :GHOST.values())
		{
			sum_per_ghost.put(ghost,0.0);
		}
		double sumScore = 0;
		double sumSteps = 0;
		double m_scoreSum = 0;
// For each test episode		
		for(int i=0; i<width; i++) {
// Create and start a new game			
			Game game = new Game(rng.nextLong(), defaultConstants);
			StarterPacMan pacman = new StarterPacMan();

			for (GHOST ghost: GHOST.values())
			{
				
				ghosts.get(ghost).startEpisode(game, true);
			}
			while(!game.gameOver()) {
// getMove in pacman learner returns the lastAction	
// This will then recompute the next move to make.
				
				if(coms)
					game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMoveAndMessage(game, game.constants.GHOST_TYPE, ghosts));			
				else
					game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE, ghosts));			
				

				for (GHOST ghost: GHOST.values())
				{
					if(game.doesGhostRequireAction(ghost)) {
						
						ghosts.get(ghost).processStep(game,messages);

					}
					
				}
			}
			
			for(GHOST ghost : GHOST.values())
			{
				sumScore +=ghosts.get(ghost).getScore();
				m_scoreSum += ghosts.get(ghost).getComsScore();
				
				sum_per_ghost.put(ghost,sum_per_ghost.get(ghost)+ghosts.get(ghost).getScore());
				
			}
			

			sumSteps += game.getTotalTime();
			//System.out.println("time taken: " + game.getTotalTime());
		}

		scoreAndTime[0] = sumScore/width;
		scoreAndTime[1] = sumSteps/width;
		scoreAndTime[2] = m_scoreSum/width;
		
		return scoreAndTime;
	}
public static double[] evaluate_SingleGhosts(RLPacMan pacman, EnumMap<GHOST,RLSingleGhost> ghosts, int width) {
		
		double[] scoreAndTime = new double[2];		
		double sumScore = 0;
		double sumSteps = 0;
		
// For each test episode		
		for(int i=0; i<width; i++) {
// Create and start a new game			
			Game game = new Game(rng.nextLong(), defaultConstants);
			pacman.startEpisode(game, true);
			for (GHOST ghost: GHOST.values())
			{
				ghosts.get(ghost).startEpisode(game, true);
			}
			while(!game.gameOver()) {
// getMove in pacman learner returns the lastAction	
// This will then recompute the next move to make.
				game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE, ghosts));			
				pacman.processStep(game);
				for (GHOST ghost: GHOST.values())
				{
					if(game.doesGhostRequireAction(ghost)) {
						
						ghosts.get(ghost).processStep(game);

					}
					
				}
			}
			for(GHOST ghost : GHOST.values())
			{
				sumScore +=ghosts.get(ghost).getScore();
			}
			

			sumSteps += game.getTotalTime();
			//System.out.println("time taken: " + game.getTotalTime());
		}

		scoreAndTime[0] = sumScore/width;
		scoreAndTime[1] = sumSteps/width;
		
		
		return scoreAndTime;
	}
	
	
	
	
	/** Estimate the current performance of a learner. */
	// Width is the number of episodes to test for (i.e. variable TEST above)	
		public static double[] evaluate_ghosts(RLGhosts ghosts, RLPacMan pacman, int width) {
			
			double[] scoreAndTime = new double[2];		
			double sumScore = 0;
			double sumSteps = 0;
			
	// For each test episode		
			for(int i=0; i<width; i++) {
	// Create and start a new game			
				Game game = new Game(rng.nextLong(), defaultConstants);
				ghosts.startEpisode(game, true);
				pacman.startEpisode(game, true);
				while(!game.gameOver()) 
				{
	// getMove in pacman learner returns the lastAction	
	// This will then recompute the next move to make.
					game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE));
					if(game.gameOver())
					{
						for(Integer rs : game.rewards.values())
						{
							sumScore += rs;
						}
						
					}
					pacman.processStep(game);
					ghosts.processStep(game);
					
					
				}
				
				sumSteps += game.getTotalTime();
				//System.out.println("time taken: " + game.getTotalTime());
			}
			double score = 0;
			if (ghosts.pacman_caught) {
				
				score = 10;
				System.out.println(score);
			}
			ghosts.pacman_caught = false;
			scoreAndTime[0] = sumScore/width;
			scoreAndTime[1] = sumSteps/width;
			
			
			return scoreAndTime;
		}
	
	
	
	
	/** Estimate the current performance of a learner. */
// Width is the number of episodes to test for (i.e. variable TEST above)	
	public static double[] evaluate(RLPacMan pacman, int width) {
		
		double[] scoreAndTime = new double[2];		
		double sumScore = 0;
		double sumSteps = 0;
		
// For each test episode		
		for(int i=0; i<width; i++) {
// Create and start a new game			
			Game game = new Game(rng.nextLong(), defaultConstants);
			pacman.startEpisode(game, true);
			while(!game.gameOver()) {
// getMove in pacman learner returns the lastAction	
// This will then recompute the next move to make.
				game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE));			
				pacman.processStep(game);
			}
			
			sumScore += game.getScore();
			sumSteps += game.getTotalTime();
			//System.out.println("time taken: " + game.getTotalTime());
		}

		scoreAndTime[0] = sumScore/width;
		scoreAndTime[1] = sumSteps/width;
		
		
		return scoreAndTime;
	}
	
	public static void computeGraphFeatures(){
		Game game=new Game(rng.nextLong(), defaultConstants);
		
		//get maze
		Maze M = game.getCurrentMaze();
		
				
		//get number of nodes
		int num_nodes = M.graph.length;
			
		//get starting positions for ghosts and pacman
		int lair_index = M.lairNodeIndex;
		int ghost_start = M.initialGhostNodeIndex;
		int pacman_start = M.initialPacManNodeIndex;
		
		//get pill indeces
		int [] pill_indeces = M.pillIndices;
		int [] power_pill_indeces = M.powerPillIndices;
		
		//node to pill ratio:
		double node_to_pill_ratio = (double)num_nodes/(double)pill_indeces.length;
		
		//starting distance between pacman and ghosts
		int dist_ghost_to_pacman = game.getShortestPath(ghost_start, pacman_start).length;
		int dist_lair_to_pacman = game.getManhattanDistance(lair_index, pacman_start);
		
		//average distance between power pills
		double avg_pp_to_pp_distance = 0;
		int c = 0;
		for (int i = 0; i < power_pill_indeces.length; i ++){
			for (int j = 0; j < power_pill_indeces.length; j++){
				if (i!=j){
					avg_pp_to_pp_distance += (double)game.getShortestPath(ghost_start, pacman_start).length;
					c++;
				}
			}
		}
		avg_pp_to_pp_distance = avg_pp_to_pp_distance /(double)c;
		
		//average distance between power pills and lair
		double avg_lair_to_pp_distance = 0;
		for (int i = 0; i < power_pill_indeces.length; i ++){
			avg_lair_to_pp_distance += game.getShortestPath(ghost_start, power_pill_indeces[i]).length;
		}
		avg_lair_to_pp_distance = avg_lair_to_pp_distance / (double)power_pill_indeces.length;
		
		//average effective eccentricity
		double avg_effective_eccentricity = 0;
		double max_effective_eccentricity = Double.MIN_VALUE;
		for (int i = 0; i < M.graph.length; i ++){
			double max = Double.MIN_VALUE;
			for (int j = 0; j < M.graph.length; j ++){
			
				if (game.getNeighbouringNodes(M.graph[i].nodeIndex).length > 0
						&& game.getNeighbouringNodes(M.graph[j].nodeIndex).length > 0){
					double d_uv = game.getShortestPath(M.graph[i].nodeIndex, M.graph[j].nodeIndex).length;
					if (d_uv > max)
						max = d_uv;
				}
			}
			
			avg_effective_eccentricity+=max;
			
			if (max > max_effective_eccentricity)
				max_effective_eccentricity = max;
		}
		avg_effective_eccentricity = avg_effective_eccentricity / (double)M.graph.length;
		
		//average effective eccentricity for between junctions
		double avg_effective_eccentricity_junct = 0;
		double max_effective_eccentricity_junct = Double.MIN_VALUE;
		for (int i = 0; i < M.junctionIndices.length; i ++){
			double max = Double.MIN_VALUE;
			for (int j = 0; j < M.junctionIndices.length; j ++){
			
				if (game.getNeighbouringNodes(M.junctionIndices[i]).length > 0
						&& game.getNeighbouringNodes(M.junctionIndices[j]).length > 0){
					double d_uv = game.getShortestPath(M.junctionIndices[i], M.junctionIndices[j]).length;
					if (d_uv > max)
						max = d_uv;
				}
			}
			
			avg_effective_eccentricity_junct+=max;
			
			if (max > max_effective_eccentricity_junct)
				max_effective_eccentricity_junct = max;
		}
		avg_effective_eccentricity_junct = avg_effective_eccentricity_junct / (double)M.junctionIndices.length;
		
		//for each pair of junctions, compute shortest path and count how many junctions are on it
		double avg_num_junctions_between_junctions = 0;
		for (int i = 0; i < M.junctionIndices.length; i ++){
			for (int j = 0; j < M.junctionIndices.length; j ++){
				int [] path_ij = game.getShortestPath(M.junctionIndices[i], M.junctionIndices[j]);
				
				double num_juncts_in_path = 0;
				
				for (int k = 0; k < path_ij.length; k++){
					for (int p = 0; p < M.junctionIndices.length; p ++){
						if (path_ij[k]==M.junctionIndices[p] && i != p && j != p)
							num_juncts_in_path+=1.0;
					}
				}
				
				avg_num_junctions_between_junctions += num_juncts_in_path;
			}
		}
		avg_num_junctions_between_junctions = avg_num_junctions_between_junctions/(M.junctionIndices.length*M.junctionIndices.length);
		
		//histogram of degrees of nodes
		int [] hist = new int[5];
		for (int i = 0; i < 5; i ++){
			hist[i]=0;
		}
		for (int i = 0; i < M.graph.length; i ++){
			int degree_i = game.getNeighbouringNodes(M.graph[i].nodeIndex).length;
			hist[degree_i]++;
		}
		
		
		
		System.out.println("num_nodes\t"+num_nodes);
		//System.out.println("starting positions:\t"+lair_index+"\t"+ghost_start+"\t"+pacman_start);
		System.out.println("num_pills\t"+pill_indeces.length);
		//and power pills:\t"+pill_indeces.length+"\t"+power_pill_indeces.length);
		
		System.out.println("node_to_pill_ratio\t"+node_to_pill_ratio);
		System.out.println("dist_ghost_to_pacman\t"+dist_ghost_to_pacman);
	
		System.out.println("avg_pp_to_pp_distance\t"+avg_pp_to_pp_distance);
		System.out.println("avg_lair_to_pp_distance\t"+avg_lair_to_pp_distance);
		System.out.println("avg_effective_eccentricity\t"+avg_effective_eccentricity);
		System.out.println("avg_effective_eccentricity_junct\t"+avg_effective_eccentricity_junct);
		
		System.out.println("max_effective_eccentricity\t"+max_effective_eccentricity);
		System.out.println("max_effective_eccentricity_junct\t"+max_effective_eccentricity_junct);
		
		System.out.println("avg_num_junctions_between_junctions\t"+avg_num_junctions_between_junctions);
		
		//System.out.println("Histogram of degrees:");
		for (int i = 2; i < 5; i ++){
			System.out.println( "dhist_"+(i)+"\t"+hist[i]);
		}
		
		
	}
	
	/** Observe a learner play a game. */
	public static void watchAndSave(RLPacMan pacman, boolean destroyWindow, String prefix, String path) {
		
		
		Game game=new Game(rng.nextLong(), defaultConstants);
		
		pacman.startEpisode(game, true);
		GameView gv=new GameView(game).showGame();
		int c = 0;
		

		while(!game.gameOver()) {
			
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE));	
			pacman.processStep(game);
			
			try{Thread.sleep(defaultConstants.DELAY);}catch(Exception e){}
			gv.repaint();
	
			//Image img_i = gv.createImage(100, 100);
			Image img_i = gv.getImage();
			BufferedImage bi = (BufferedImage)img_i;
			
			
			StringBuffer leadingZeros = new StringBuffer();
			if (c < 10)
				leadingZeros.append("0000");
			else if (c < 100)
				leadingZeros.append("000");
			else if (c < 1000)
				leadingZeros.append("00");
			else if (c < 10000)
				leadingZeros.append("0");
			
			
			
			File f = new File(new String(path+"/img"+leadingZeros.toString()+c+".png"));
			c++;
			try {
				ImageIO.write(bi, "png", f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (destroyWindow){
			gv.setVisible(false);
			gv.setEnabled(false);
			gv.getFrame().dispose();
		}
	}
public static void watch(EnumMap<GHOST,RLSingleGhost> ghosts, boolean destroyWindow) {
		
		
		Game game=new Game(rng.nextLong(), defaultConstants);
		
		StarterPacMan pacman = new StarterPacMan();
		for(GHOST ghost: GHOST.values())
		{
			ghosts.get(ghost).startEpisode(game, true);
		}
		
		GameView gv=new GameView(game).showGame();
		
		

		while(!game.gameOver()) {
			
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMoveAndMessage(game, game.constants.GHOST_TYPE,ghosts));	
			

			for(GHOST ghost: GHOST.values())
			{
				ghosts.get(ghost).processStep(game,messages);
			}
			
			
			try{Thread.sleep(defaultConstants.DELAY);}catch(Exception e){}
			gv.repaint();
			Image img_i = gv.createImage(100, 100);
			BufferedImage bi = (BufferedImage)img_i;
			File f = new File("./output.png");
			try {
				ImageIO.write(bi, "png", f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (destroyWindow){
			gv.setVisible(false);
			gv.setEnabled(false);
			gv.getFrame().dispose();
		}
	}
public static void watch(RLPacMan pacman, EnumMap<GHOST,RLSingleGhost> ghosts, boolean destroyWindow) {
		
		
		Game game=new Game(rng.nextLong(), defaultConstants);
		
		pacman.startEpisode(game, true);
		for(GHOST ghost: GHOST.values())
		{
			ghosts.get(ghost).startEpisode(game, true);
		}
		
		GameView gv=new GameView(game).showGame();
		
		

		while(!game.gameOver()) {
			
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE,ghosts));	
			pacman.processStep(game);
			for(GHOST ghost: GHOST.values())
			{
				ghosts.get(ghost).processStep(game);
			}
			
			
			try{Thread.sleep(defaultConstants.DELAY);}catch(Exception e){}
			gv.repaint();
			Image img_i = gv.createImage(100, 100);
			BufferedImage bi = (BufferedImage)img_i;
			File f = new File("./output.png");
			try {
				ImageIO.write(bi, "png", f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (destroyWindow){
			gv.setVisible(false);
			gv.setEnabled(false);
			gv.getFrame().dispose();
		}
	}
	/** Observe a learner play a game. */
	public static void watch(RLPacMan pacman, RLGhosts ghosts, boolean destroyWindow) {
		
		
		Game game=new Game(rng.nextLong(), defaultConstants);
		
		pacman.startEpisode(game, true);
		ghosts.startEpisode(game, false);
		GameView gv=new GameView(game).showGame();
		
		

		while(!game.gameOver()) {
			
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE));	
			pacman.processStep(game);
			ghosts.processStep(game);
			
			try{Thread.sleep(defaultConstants.DELAY);}catch(Exception e){}
			gv.repaint();
			Image img_i = gv.createImage(100, 100);
			BufferedImage bi = (BufferedImage)img_i;
			File f = new File("./output.png");
			try {
				ImageIO.write(bi, "png", f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (destroyWindow){
			gv.setVisible(false);
			gv.setEnabled(false);
			gv.getFrame().dispose();
		}
	}
		
	
public static void watch(RLPacMan pacman, boolean destroyWindow) {
		
		
		Game game=new Game(rng.nextLong(), defaultConstants);
		
		pacman.startEpisode(game, true);
		GameView gv=new GameView(game).showGame();
		
		

		while(!game.gameOver()) {
			
			game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game, game.constants.GHOST_TYPE));	
			pacman.processStep(game);
			
			try{Thread.sleep(defaultConstants.DELAY);}catch(Exception e){}
			gv.repaint();
			Image img_i = gv.createImage(100, 100);
			BufferedImage bi = (BufferedImage)img_i;
			File f = new File("./output.png");
			try {
				ImageIO.write(bi, "png", f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (destroyWindow){
			gv.setVisible(false);
			gv.setEnabled(false);
			gv.getFrame().dispose();
		}
	}
	private static EnumMap<GHOST, MOVE> getGhostMove(Game game, int ghostType){

		switch(ghostType){
			case 0:
				return ghostsR.getMove(game.copy(), -1);
			case 1:
				return ghostsS.getMove(game.copy(), -1);
			case 2:
				return ghostsC.getMove(game.copy(), -1);
			case 3:
				return ghostsL.getMove(game.copy(), -1);
			case 4:
			{
				
				return ghostsRL.getMove(game.copy(), -1);
			}
			default:
				System.err.println("INVALID GHOST TYPE");
				System.exit(1);
				return null;				// Compilier is complaining... 
		}
	}
	private static EnumMap<GHOST, MOVE> getGhostMove(Game game, int ghostType,EnumMap<GHOST, RLSingleGhost> ghosts){

		switch(ghostType){
			case 0:
				return ghostsR.getMove(game.copy(), -1);
			case 1:
				return ghostsS.getMove(game.copy(), -1);
			case 2:
				return ghostsC.getMove(game.copy(), -1);
			case 3:
				return ghostsL.getMove(game.copy(), -1);
			case 4:
			{
				EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
				for(GHOST ghost: GHOST.values())
				{
					//((SarsaSingleGhost)ghosts.get(ghost)).dump();
					//System.out.println(ghosts.get(ghost).getMove());
					moves.put(ghost, ghosts.get(ghost).getMove(game,0));
				}
				//System.out.println(moves);
				//if(moves.get(GHOST.BLINKY)!=MOVE.NEUTRAL) System.out.println(moves.get(GHOST.BLINKY));
				return moves;
			}
			default:
				System.err.println("INVALID GHOST TYPE");
				System.exit(1);
				return null;				// Compilier is complaining... 
		}
	}
	
	private static EnumMap<GHOST, MOVE> getGhostMoveAndMessage(Game game, int ghostType,EnumMap<GHOST, RLSingleGhost> ghosts){

		switch(ghostType){
			case 0:
				return ghostsR.getMove(game.copy(), -1);
			case 1:
				return ghostsS.getMove(game.copy(), -1);
			case 2:
				return ghostsC.getMove(game.copy(), -1);
			case 3:
				return ghostsL.getMove(game.copy(), -1);
			case 4:
			{
				EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
				
				for(GHOST ghost: GHOST.values())
				{
					//((SarsaSingleGhost)ghosts.get(ghost)).dump();
					//System.out.println(ghosts.get(ghost).getMove());
					moves.put(ghost, ghosts.get(ghost).getMove(game,0));
					messages.put(ghost, ((SarsaSingleGhost)ghosts.get(ghost)).getMessage(game,0));
					

				}
				//System.out.println(messages);
				//if(moves.get(GHOST.BLINKY)!=MOVE.NEUTRAL) System.out.println(moves.get(GHOST.BLINKY));
				return moves;
			}
			default:
				System.err.println("INVALID GHOST TYPE");
				System.exit(1);
				return null;				// Compilier is complaining... 
		}
	}
	
}