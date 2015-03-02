package me.benthomas.tttworld.server.ai;

import me.benthomas.tttworld.Mark;

/**
 * Represents an AI play who will attempt to win when given the chance. If
 * winning is not possible in the next move, will act like a
 * {@link AIPlayerBlocking}.
 * 
 * @author Ben Thomas
 */
public class AIPlayerSmart extends AIPlayer {

    /**
     * Creates a new smart AI with the given name, using the given mark.
     * 
     * @param name The name of the player.
     * @param mark The mark with which the player should play.
     */
    public AIPlayerSmart(String name, Mark mark) {
        super(name, mark);

        this.behaviours.add(new AIBehaviourWin());
        this.behaviours.add(new AIBehaviourBlock());
        this.behaviours.add(new AIBehaviourRandom());
    }

}
