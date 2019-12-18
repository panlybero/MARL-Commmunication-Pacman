package pacman.game.internal;

import pacman.game.Constants.MOVE;

/*
 * Data structure to hold all information pertaining to the ghosts.
 */
public class Ghost
{
	public int currentNodeIndex, edibleTime, lairTime, reward;	
	public MOVE lastMoveMade;

	public Ghost(int currentNodeIndex, int edibleTime, int lairTime, MOVE lastMoveMade)
	{
		this.currentNodeIndex = currentNodeIndex;
		this.edibleTime = edibleTime;
		this.lairTime = lairTime;
		this.lastMoveMade = lastMoveMade;
		this.reward = 0;
	}

	public Ghost copy()
	{
		return new Ghost(currentNodeIndex, edibleTime, lairTime, lastMoveMade);
	}
}