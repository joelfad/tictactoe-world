package me.benthomas.tttworld.server.ai;

import me.benthomas.tttworld.Mark;

/**
 * Represents an AI who will attempt to block the opponent from winning. Makes
 * random movements if no blocking is possible.
 * 
 * @author Ben Thomas
 */
public class AIPlayerBlocking extends AIPlayer {

    /**
     * Creates a new blocking AI with the given name, using the given mark.
     * 
     * @param name The name of the player.
     * @param mark The mark with which this player should play.
     */
    public AIPlayerBlocking(String name, Mark mark) {
        super(name, mark);

        this.behaviours.add(new AIBehaviourBlock());
        this.behaviours.add(new AIBehaviourRandom());
    }

}
