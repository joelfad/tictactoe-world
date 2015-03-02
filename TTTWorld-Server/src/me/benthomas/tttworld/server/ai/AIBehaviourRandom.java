package me.benthomas.tttworld.server.ai;

import java.util.ArrayList;
import java.util.Random;

/**
 * Represents an AI behaviour in which a player will make a move at a random
 * location on the board.
 * 
 * @author Ben Thomas
 */
public class AIBehaviourRandom implements AIBehaviour {
    private Random random = new Random();

    @Override
    public boolean tryMakeMove(AIPlayer p) {
        ArrayList<Point> openSpots = new ArrayList<Point>();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (!p.getGame().getBoard().getMark(x, y).isPlayer) {
                    openSpots.add(new Point(x, y));
                }
            }
        }

        if (openSpots.size() > 0) {
            Point selectedPoint = openSpots.get(this.random.nextInt(openSpots.size()));
            p.getGame().getBoard().setMark(selectedPoint.x, selectedPoint.y, p.getMark());

            return true;
        } else {
            return false;
        }
    }

}
