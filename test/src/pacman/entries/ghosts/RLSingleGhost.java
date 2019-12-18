package pacman.entries.ghosts;

import java.util.EnumMap;

import pacman.controllers.Controller;
import pacman.entries.pacman.QFunction;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public abstract class RLSingleGhost extends Controller<MOVE> 
{
	public abstract void startEpisode(Game game, boolean testMode);
	public abstract void processStep(Game game);
	public abstract void processStep(Game game, EnumMap<GHOST, Integer> messages);
	public abstract void savePolicy(String filename);
	public QFunction Qfunction;
	public double[] episodeData() { // Override to add data to learning curves
		double[] data = new double[0];
		return data;
	}
	
	public abstract QFunction getQFunction();
	public abstract int getScore();
	public abstract double getComsScore();
	//public abstract MOVE getMove();
}
