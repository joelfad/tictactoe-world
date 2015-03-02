package me.benthomas.tttworld.server.ai;

import java.util.ArrayList;
import java.util.List;

import me.benthomas.tttworld.Mark;
import me.benthomas.tttworld.server.Player;

/**
 * Represents an AI-controlled tic-tac-toe player who is making their moves
 * based on algorithms.
 * 
 * @author Ben Thomas
 */
public abstract class AIPlayer extends Player {
    protected List<AIBehaviour> behaviours = new ArrayList<AIBehaviour>();
    
    /**
     * Creates a new AI-controlled player with the given name and playing the
     * given mark.
     * 
     * @param name The name of the player.
     * @param mark The mark that this player should play.
     */
    public AIPlayer(String name, Mark mark) {
        super(name, mark);
    }
    
    @Override
    public final void notifyUpdate(boolean myTurn) {
        if (myTurn) {
            for (AIBehaviour behaviour : this.behaviours) {
                if (behaviour.tryMakeMove(this)) {
                    this.game.tick();
                    return;
                }
            }
            
            throw new RuntimeException("AI behaviours failed to make a move!");
        }
    }
    
    @Override
    public void notifyWon() {
    }
    
    @Override
    public void notifyLost() {
    }
    
    @Override
    public void notifyDrawn() {
    }
}
