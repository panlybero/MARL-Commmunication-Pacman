package pacman.entries.ghosts;

import pacman.entries.pacman.CustomFeatureSet;
import pacman.entries.pacman.FeatureSet;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GhostFeatures extends FeatureSet {

	//First Version of Ghosts fetures. Consists of direction to pacman, distance to pacman and whether itself is edible 
	// 
	public double[] values;
	int FEATURES;
	public GHOST ghost;
	public GhostFeatures(GHOST ghost_)
	{
		ghost = ghost_;
		init();
	}
	
	public GhostFeatures() {
		init();
		// TODO Auto-generated constructor stub
	}

	public void init()
	{
		FEATURES = 3;
		values = new double[FEATURES];
	}
	
	private void setValues_vector_directions(Game game, MOVE move) 
	{
		
		double pac_x = game.getNodeXCood(game.getPacmanCurrentNodeIndex());
		double pac_y = game.getNodeYCood(game.getPacmanCurrentNodeIndex());
		
		///System.out.println(game.getGhostCurrentNodeIndex(ghost));
		double gh_x = game.getNodeXCood(game.getNeighbour(game.getGhostCurrentNodeIndex(ghost), move));
		double gh_y = game.getNodeYCood(game.getNeighbour(game.getGhostCurrentNodeIndex(ghost), move));
		
		double dir_x = gh_x-pac_x;
		double dir_y = gh_y-pac_y;
		double dir_magn = Math.sqrt(dir_x*dir_x + dir_y*dir_y);
		
		if (dir_magn == 0)
		{
			dir_x = 0;
			dir_y = 0;
		}
		
		double m = Math.max(Math.abs(dir_x), Math.abs(dir_y));
		values[0] = Math.round(dir_x/m);
		values[1] = Math.round(dir_y/m);
		//values[2] = dir_magn;
		
		boolean edible = game.isGhostEdible(ghost);
		double is_edible = 1.0f;
		if (edible)
		{
			is_edible = -1.0f;
		}
		values[2] = is_edible;
		values[3] = Math.round(dir_x/m) * is_edible;
		values[4] = Math.round(dir_y/m)* is_edible;
		for(int i = 0;i<FEATURES;i++)
		{
			//System.out.print(values[i]+ " ");
		}
		//System.out.println();
	}
	private void setValues_standard(Game game, MOVE move) 
	{
		//is action towards pacman? (away when edible)
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		boolean edible = game.isGhostEdible(ghost);
		
		MOVE right_move = null;
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);

		double is_right_move = -1;
		if(right_move == move) is_right_move = 1;


		double is_edible = 1.0f;
		if (edible)
		{
			is_edible = -1.0f;
		}
		
		values[0] = is_right_move;
		values[1] = is_edible;
	}
	
	private void setValues_standard_pacman_pill_dist(Game game, MOVE move) 
	{
		//is action towards pacman? (away when edible)
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		boolean edible = game.isGhostEdible(ghost);
		
		MOVE right_move = null;
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);

		double is_right_move = -1;
		if(right_move == move) is_right_move = 1;


		double is_edible = 1.0f;
		if (edible)
		{
			is_edible = -1.0f;
		}
		
		values[0] = is_right_move;
		values[1] = is_edible;
		
		
		int pac = game.getPacmanCurrentNodeIndex();
		
		int[] pill_inds = game.getActivePowerPillsIndices();
		int min_dist = 500;
		
		if(pill_inds.length != 0)
		{
			min_dist = game.getShortestPathDistance(pac, pill_inds[0]);
			
			for(int i=0;i<pill_inds.length;i++)
			{
				int dist = game.getShortestPathDistance(pac, pill_inds[i]);
				if(dist<min_dist)
				{
					min_dist = dist;
				}
			}
		}
		
		
		values[2] = ((double)min_dist)/((double)game.currentMaze.graph.length) ;
		
		for(double v : values)
		{
			//System.out.print(v+" ");
		}
		//System.out.println("////");
	}
	
	
	private void setValues_withPill(Game game, MOVE move) 
	{
		//is action towards pacman? (away when edible)
		int curr_node = game.getGhostCurrentNodeIndex(ghost);
		boolean edible = game.isGhostEdible(ghost);
		
		MOVE right_move = null;
		if(!edible) right_move = game.getApproximateNextMoveTowardsTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		if(edible) right_move =game.getApproximateNextMoveAwayFromTarget(curr_node, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);

		double is_right_move = -1;
		if(right_move == move) is_right_move = 1;


		double is_edible = 1.0f;
		if (edible)
		{
			is_edible = -1.0f;
		}
		
		values[0] = is_right_move;
		values[1] = is_edible;
		
		int[] power_pill_inds = game.getActivePowerPillsIndices();
		if(power_pill_inds.length>0)
		{
			int closest_power_pill = power_pill_inds[0];
			double closest_pill_dist = game.getDistance(curr_node, closest_power_pill, DM.PATH);
			for(int pill_ind:power_pill_inds)
			{
				double dist = game.getDistance(curr_node, pill_ind, DM.PATH);
				if(dist<closest_pill_dist)
				{
					closest_pill_dist = dist;
					closest_power_pill = pill_ind;
				}
			}
			
			values[2] = closest_pill_dist/game.currentMaze.pillIndices.length;//game.getApproximateNextMoveTowardsTarget(curr_node, closest_power_pill, MOVE.NEUTRAL, DM.PATH);
			
		}else
		{
			values[2] = 0;
		}
		
		//System.out.println(values[2]);
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return FEATURES;
	}

	@Override
	public double get(int i) {
		// TODO Auto-generated method stub
		return values[i];
	}

	@Override
	public GhostFeatures extract(Game game, MOVE move) {
		GhostFeatures features = new GhostFeatures(ghost);
		features.setValues_standard_pacman_pill_dist(game, move);
		return features;
	}

}
