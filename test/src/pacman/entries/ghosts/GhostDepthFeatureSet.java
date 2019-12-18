package pacman.entries.ghosts;

import java.util.HashMap;

import pacman.entries.pacman.FeatureSet;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Features based on object counts a few junctions ahead in the move direction.
 */
public class GhostDepthFeatureSet extends FeatureSet {
	
	boolean useMessages = true;
	
	// Lazy enum
	private int REGULAR_PILL = 0;
	private int POWER_PILL = 2;
	private int REGULAR_GHOST = 2;
	private int EDIBLE_GHOST = 3;
	private int PACMAN = 0;
	private int PACMAN_WHILEBLUE = 1;

	private int OBJECTS = 2; // How many
	private int DEPTH = 4; // Of search
	private int FEAT_LEN = 3+0+4;//useMessages ? OBJECTS*DEPTH +5+4 :OBJECTS*DEPTH +3 ; 
	
	private HashMap<Integer,Integer> nodeDepths = new HashMap<Integer,Integer>(); // To avoid double-counting
	
	private int[][] counts = new int[DEPTH][OBJECTS]; // How many at each depth
	

	public double[] values = new double[FEAT_LEN]; // Feature values
	//public double[] values = new double[2];
	public GHOST ghost;
	int FEATURES;
	

	public GhostDepthFeatureSet(GHOST ghost_)
	{
		ghost = ghost_;

		init();
	}
	
	public GhostDepthFeatureSet() {
		init();
		// TODO Auto-generated constructor stub
	}
	
	public void init()
	{
		//FEATURES = 2;//
		FEATURES = FEAT_LEN;//OBJECTS*DEPTH+3;

	}
	/** Report how many features there are. */
	public int size() {
		return values.length;
	}

	/** Retrieve a feature value. */
	public double get(int i) {
		return values[i];
	}
	
	/** Extract a feature set for this state-action pair. */
	public GhostDepthFeatureSet extract(Game game, MOVE move) {
		GhostDepthFeatureSet features = new GhostDepthFeatureSet(ghost);
		features.setValues_seeGhosts(game, move);
		return features;
	}
	
	public GhostDepthFeatureSet extract_with_messages(Game game, MOVE move, Integer[] last_messages) {
		GhostDepthFeatureSet features = new GhostDepthFeatureSet(ghost);
		//features.setValues_seeGhosts_with_messages(game, move, last_messages);
		//features.setValues_standard_with_messages(game,move,last_messages);
		features.setValues_towards_and_away_with_messages(game,move,last_messages);
		return features;
	}
	private void setValues_towards_and_away_with_messages(Game game, MOVE move, Integer[] messages) 
	{
		//is action towards pacman? (away when edible)
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		boolean edible = game.isGhostEdible(ghost);
		double my_dist = game.getDistance(curr_node, game.getPacmanCurrentNodeIndex(), DM.PATH);

		
		MOVE toward_move = null;
		MOVE away_move = null;
		toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		away_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);

		double is_toward_move = 0;
		if(toward_move == move) is_toward_move = 1;
		double is_away_move = 0;
		if(away_move == move) is_away_move = 1;
		
		if(my_dist>Math.sqrt(game.currentMaze.graph.length)) is_toward_move = 0;
		if(my_dist>Math.sqrt(game.currentMaze.graph.length)) is_away_move = 0;

		double is_edible = 0.0f;
		if (edible)
		{
			is_edible = 1.0f;
		}

		boolean is_closest = true;
		


		int v = 0;
		//System.out.println(my_dist+" "+game.currentMaze.graph.length/4);
		if(my_dist>Math.sqrt(game.currentMaze.graph.length)) is_toward_move = 0;
		if(my_dist>Math.sqrt(game.currentMaze.graph.length)) is_away_move = 0;
		
		values[v++] = is_toward_move;
		values[v++] = is_away_move;
		//values[v++] = is_closest? 1:0;
		values[v++] = 0;//is_edible;
		
		GHOST[] others = new GHOST[3];
		int c=0;
		for(GHOST ghost_:GHOST.values())
		{
			if(ghost_ != ghost)
			{
				others[c++]=ghost_;
			}
		}
		
		double[] vals = new double[3];
		
		for(int i = 0;i<vals.length;i++)
		{
			GHOST other = others[i];
			//System.out.println(other);
			int other_node = game.getGhostCurrentNodeIndex(other);
			toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, other_node, game.getGhostLastMoveMade(ghost), DM.PATH);
			int dist = game.getApproximateShortestPathDistance(curr_node, game.getGhostCurrentNodeIndex(other), game.getGhostLastMoveMade(ghost));
			int is_near = dist<20? 1:0;
			if(move == toward_move && ghost!= other) 
			{
				vals[i]=1 ;//*messages[other.ordinal()];
			}
		}
		
		//values[v++] = vals[0];
		//values[v++] = vals[1];
		//values[v++] = vals[2];
		
		for(GHOST other :GHOST.values())
		{
			if(other!=ghost || true)
			{
				//values[v++] = messages[other.ordinal()];
			}
		}
		boolean coms = true;
		
		
		
		if(coms)
		{
			//messages[ghost.ordinal()] = 0;
			
			values[v++] = messages[0];
			values[v++] = messages[1];
			values[v++] = messages[2];
			values[v++] = messages[3];
		}
		else
		{
			values[v++] = 0;//messages[0];
			values[v++] = 0;//messages[1];
			
			values[v++] = 0;//messages[2];
			values[v++] = 0;//messages[3];
		}
		
	}
	private void setValues_standard_with_messages(Game game, MOVE move, Integer[] messages) 
	{
		//is action towards pacman? (away when edible)
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		boolean edible = game.isGhostEdible(ghost);
		
		MOVE right_move = null;
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);

		double is_right_move = 0;
		if(right_move == move) is_right_move = 1;


		double is_edible = 0.0f;
		if (edible)
		{
			is_edible = 1.0f;
		}
		double my_dist = game.getDistance(curr_node, game.getPacmanCurrentNodeIndex(), DM.PATH);
		boolean is_closest = true;
		
		int dist_mod = 1;
		if(is_right_move == 1)
			if(!edible) dist_mod = -1;
		for(GHOST ghost_:GHOST.values())
		{
			double dist = game.getDistance(game.getGhostCurrentNodeIndex(ghost_), game.getPacmanCurrentNodeIndex(), DM.PATH);
			if(dist<my_dist+dist_mod) is_closest = false;
		}
		
		int v = 0;
		//System.out.println(my_dist+" "+game.currentMaze.graph.length/4);
		if(my_dist>Math.sqrt(game.currentMaze.graph.length)) is_right_move = 0;
		
		values[v++] = is_right_move;
		values[v++] = is_closest? 1:0;
		//values[v++] = is_edible;
		
		GHOST[] others = new GHOST[3];
		int c=0;
		for(GHOST ghost_:GHOST.values())
		{
			if(ghost_ != ghost)
			{
				others[c++]=ghost_;
			}
		}
		
		double[] vals = new double[3];
		
		for(int i = 0;i<vals.length;i++)
		{
			GHOST other = others[i];
			//System.out.println(other);
			int other_node = game.getGhostCurrentNodeIndex(other);
			right_move = game.getApproximateNextMoveTowardsTarget(curr_node, other_node, game.getGhostLastMoveMade(ghost), DM.PATH);
			int dist = game.getApproximateShortestPathDistance(curr_node, game.getGhostCurrentNodeIndex(other), game.getGhostLastMoveMade(ghost));
			int is_near = dist<20? 1:0;
			if(move == right_move && ghost!= other) 
			{
				vals[i]=1 ;//*messages[other.ordinal()];
			}
		}
		
		//values[v++] = vals[0];
		//values[v++] = vals[1];
		//values[v++] = vals[2];
		
		for(GHOST other :GHOST.values())
		{
			if(other!=ghost || true)
			{
				//values[v++] = messages[other.ordinal()];
			}
		}
		boolean coms = false;
		
		//System.out.println(messages[0]+" "+messages[1]+" "+messages[2]+" "+messages[3]);
		
		if(coms)
		{
			values[v++] = messages[0];
			values[v++] = messages[1];
			
			values[v++] = messages[2];
			values[v++] = messages[3];
		}
		else
		{
			values[v++] = 0;//messages[0];
			values[v++] = 0;//messages[1];
			
			values[v++] = 0;//messages[2];
			values[v++] = 0;//messages[3];
		}
		
	}
	
	private void setValues_standard(Game game, MOVE move) 
	{
		//is action towards pacman? (away when edible)
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		boolean edible = game.isGhostEdible(ghost);
		
		MOVE right_move = null;
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);

		double is_right_move = 0;
		if(right_move == move) is_right_move = 1;


		double is_edible = 0.0f;
		if (edible)
		{
			is_edible = 1.0f;
		}
		
		values[0] = is_right_move;
		values[1] = is_edible;
	}
	
	private void setValues_towardsAndAway(Game game, MOVE move) 
	{
		//is action towards pacman? (away when edible)
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		boolean edible = game.isGhostEdible(ghost);
		
		MOVE toward_move = null;
		MOVE away_move = null;
		toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		away_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);

		double is_toward_move = 0;
		if(toward_move == move) is_toward_move = 1;
		double is_away_move = 0;
		if(away_move == move) is_away_move = 1;

		double is_edible = 0.0f;
		if (edible)
		{
			is_edible = 1.0f;
		}
		
		values[0] = is_toward_move;
		values[1] = is_away_move;
		values[2] = is_edible;
	}	
	
private void setValues_seeGhosts(Game game, MOVE move) {
		
		int nodeIndex = game.getGhostCurrentNodeIndex(ghost);
		setCounts(game, nodeIndex, move, 0);
		int v = 0;
		
		double numGhosts = game.constants.NUM_GHOSTS;
		if (numGhosts < 1)
			numGhosts = 1;
		

		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][PACMAN]); // How many at each depth
		
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][PACMAN_WHILEBLUE]);
			
		
		/*
		boolean edible = game.isGhostEdible(ghost);
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		MOVE right_move = null;
		
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		
		double is_right_move = -1;
		if(right_move == move) is_right_move = 1;
		values[v++] = is_right_move;
		*/
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		MOVE toward_move = null;
		MOVE away_move = null;
		
		toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		away_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		
		double is_toward_move = 0;
		if(toward_move == move) is_toward_move = 1;
		double is_away_move = 0;
		if(away_move == move) is_away_move = 1;
		boolean edible = game.isGhostEdible(ghost);
		
		//values[v++] = is_toward_move;
		//values[v++] = is_away_move;
		values[v++] = edible? 1:0;
		
		GHOST[] others = new GHOST[3];
		int c=0;
		for(GHOST ghost_:GHOST.values())
		{
			if(ghost_ != ghost)
			{
				others[c++]=ghost_;
			}
		}
		
		double[] vals = new double[3];
		
		for(int i = 0;i<vals.length;i++)
		{
			GHOST other = others[i];
			//System.out.println(other);
			int other_node = game.getGhostCurrentNodeIndex(other);
			toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, other_node, game.getGhostLastMoveMade(ghost), DM.PATH);
			if(move == toward_move) vals[i]=1;
		}
		values[v++] = vals[0];
		values[v++] = vals[1];
		values[v++] = vals[2];
		
		/*
		double is_edible = 1.0f;
		if (edible)
		{
			is_edible = -1.0f;
		}
		values[v++] = is_edible;
		*/
		if (v != values.length) {
			System.out.println("Feature vector length error: said "+values.length+", got "+v);
			System.exit(0);
		}
	}


private void setValues_seeGhosts_with_messages(Game game, MOVE move, Integer[] messages) {
		
		int nodeIndex = game.getGhostCurrentNodeIndex(ghost);
		setCounts(game, nodeIndex, move, 0);
		int v = 0;
		
		double numGhosts = game.constants.NUM_GHOSTS;
		if (numGhosts < 1)
			numGhosts = 1;
		

		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][PACMAN]); // How many at each depth
		
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][PACMAN_WHILEBLUE]);
		
		/*
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][POWER_PILL] * 1.0 / game.currentMaze.powerPillIndices.length); // How many at each depth
		*/
		/*
		boolean edible = game.isGhostEdible(ghost);
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		MOVE right_move = null;
		
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		
		double is_right_move = -1;
		if(right_move == move) is_right_move = 1;
		values[v++] = is_right_move;
		*/
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		MOVE toward_move = null;
		MOVE away_move = null;
		
		toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		away_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		
		double is_toward_move = 0;
		if(toward_move == move) is_toward_move = 1;
		double is_away_move = 0;
		if(away_move == move) is_away_move = 1;
		boolean edible = game.isGhostEdible(ghost);
		
		values[v++] = is_toward_move;
		values[v++] = is_away_move;
		//values[v++] = edible? 1:0;
		
		GHOST[] others = new GHOST[3];
		int c=0;
		for(GHOST ghost_:GHOST.values())
		{
			if(ghost_ != ghost)
			{
				others[c++]=ghost_;
			}
		}
		
		double[] vals = new double[3];
		
		for(int i = 0;i<vals.length;i++)
		{
			GHOST other = others[i];
			//System.out.println(other);
			int other_node = game.getGhostCurrentNodeIndex(other);
			toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, other_node, game.getGhostLastMoveMade(ghost), DM.PATH);
			if(move == toward_move) vals[i]=1;
		}
		values[v++] = vals[0];
		values[v++] = vals[1];
		values[v++] = vals[2];
		
		
		
		int sum = (messages[0]+messages[1]+messages[2]+messages[3]);
		
		

		values[v++] = messages[0];
		values[v++] = messages[1];
		values[v++] = messages[2];
		values[v++] = messages[3];

		
		/*
		double is_edible = 1.0f;
		if (edible)
		{
			is_edible = -1.0f;
		}
		values[v++] = is_edible;
		*/
		if (v != values.length) {
			System.out.println("Feature vector length error: said "+values.length+", got "+v);
			System.exit(0);
		}
	}
	
	/** Compute feature values in [0,1). */
	private void setValues(Game game, MOVE move) {
		
		int nodeIndex = game.getGhostCurrentNodeIndex(ghost);
		setCounts(game, nodeIndex, move, 0);
		int v = 0;
		
		double numGhosts = game.constants.NUM_GHOSTS;
		if (numGhosts < 1)
			numGhosts = 1;
		/*
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][REGULAR_PILL] * 1.0 / game.currentMaze.pillIndices.length); // How many at each depth
		*/
		/*
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][POWER_PILL] * 1.0 / game.currentMaze.powerPillIndices.length); // How many at each depth
		*/
		/*
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][REGULAR_GHOST] * 1.0 / numGhosts); // How many at each depth
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][EDIBLE_GHOST] * 1.0 / numGhosts); // How many at each depth
		*/
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][PACMAN]); // How many at each depth
		
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][PACMAN_WHILEBLUE]);
		/*
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][POWER_PILL] * 1.0 / game.currentMaze.powerPillIndices.length); // How many at each depth
		
		*/
		/*
		boolean edible = game.isGhostEdible(ghost);
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		MOVE right_move = null;
		
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		
		double is_right_move = -1;
		if(right_move == move) is_right_move = 1;
		values[v++] = is_right_move;
		*/
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		MOVE toward_move = null;
		MOVE away_move = null;
		
		toward_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		away_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		
		double is_toward_move = 0;
		if(toward_move == move) is_toward_move = 1;
		double is_away_move = 0;
		if(away_move == move) is_away_move = 1;
		boolean edible = game.isGhostEdible(ghost);
		
		values[v++] = is_toward_move;
		values[v++] = is_away_move;
		values[v++] = edible? 1:0;
		
		/*
		double is_edible = 1.0f;
		if (edible)
		{
			is_edible = -1.0f;
		}
		values[v++] = is_edible;
		*/
		if (v != values.length) {
			System.out.println("Feature vector length error: said "+values.length+", got "+v);
			System.exit(0);
		}
	}
	
	/** Count objects a few junctions ahead. */
	private void setCounts(Game game, int nextIndex, MOVE move, int depth) {

		if (depth >= DEPTH)
			return;

		// Count objects in this segment
		while (true) {
			nextIndex = game.getNeighbour(nextIndex, move);
			countObjectsAtIndex(game, nextIndex, depth);

			// Stop at a junction or corner
			if (game.isJunction(nextIndex) || game.getNeighbour(nextIndex, move) == -1)
				break;
		}

		// Continue to next depth
		MOVE[] possibleMoves = game.getPossibleMoves(nextIndex, move);
		for (MOVE nextMove : possibleMoves)
			setCounts(game, nextIndex, nextMove, depth+1);
	}
	
	/** Count objects at this node index. */
	private void countObjectsAtIndex(Game game, int nodeIndex, int depth) {
		
		
		// Don't double-count nodes
		int oldDepth = -1;
		if (nodeDepths.containsKey(nodeIndex)) {
			oldDepth = nodeDepths.get(nodeIndex);
			if (oldDepth <= depth)
				return;
		}
		nodeDepths.put(nodeIndex, depth);
		/*
		// Regular pill
		int pillIndex = game.getPillIndex(nodeIndex);
		if (pillIndex >= 0 && game.isPillStillAvailable(pillIndex)) {
			counts[depth][REGULAR_PILL]++;
			if (oldDepth != -1)
				counts[oldDepth][REGULAR_PILL]--;
		}
		*/
		
		// Power pill
		/*
		int powerPillIndex = game.getPowerPillIndex(nodeIndex);
		if (powerPillIndex >= 0 && game.isPowerPillStillAvailable(powerPillIndex)) {
			counts[depth][POWER_PILL]++;
			if (oldDepth != -1)
				counts[oldDepth][POWER_PILL]--;
		}
		
		*/
		/*
		// Enemy or feast
		for (GHOST _ghost : GHOST.values()) {
			if (!_ghost.equals(ghost))
			{
				int ghostIndex = game.getGhostCurrentNodeIndex(_ghost);
				if (ghostIndex == game.getCurrentMaze().lairNodeIndex)
					ghostIndex = game.getGhostInitialNodeIndex();
	
				if (ghostIndex == nodeIndex) {
					if (game.isGhostEdible(_ghost)) {
						counts[depth][EDIBLE_GHOST]++;
						if (oldDepth != -1)
							counts[oldDepth][EDIBLE_GHOST]--;
					}
					else {
						counts[depth][REGULAR_GHOST]++;
						if (oldDepth != -1)
							counts[oldDepth][REGULAR_GHOST]--;
					}
				}
			}
			
		}
		*/
		
		boolean edible = game.isGhostEdible(ghost);
		
		int pacmanIndex = game.getPacmanCurrentNodeIndex();
		if (pacmanIndex == nodeIndex) {

			if(!edible )
				counts[depth][PACMAN]++;
			else
				counts[depth][PACMAN_WHILEBLUE]++;
			
			if (oldDepth != -1)
				if(!edible )
					counts[oldDepth][PACMAN]--;
				else
					counts[oldDepth][PACMAN_WHILEBLUE]--;
				
		}
		
		/*
		int pacmanIndex = game.getPacmanCurrentNodeIndex();
		if (pacmanIndex == nodeIndex) {
				counts[depth][PACMAN]++;
				if (oldDepth != -1)
					counts[oldDepth][PACMAN]--;
		}
		*/
		
	}
}
