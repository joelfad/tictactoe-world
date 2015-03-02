package me.benthomas.tttworld.server.ai;

import me.benthomas.tttworld.Mark;

/**
 * Represents an AI who makes completely random moves.
 * 
 * @author Ben Thomas
 */
public final class AIPlayerRandom extends AIPlayer {

    /**
     * Creates a new random AI with the given name, using the given mark.
     * 
     * @param name The name of the player.
     * @param mark The mark with which the player should play.
     */
    public AIPlayerRandom(String name, Mark mark) {
        super(name, mark);

        this.behaviours.add(new AIBehaviourRandom());
    }

}
