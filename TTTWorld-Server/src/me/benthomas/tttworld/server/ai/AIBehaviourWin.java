package me.benthomas.tttworld.server.ai;

/**
 * Represents an AI behaviour where the player will attempt to win by completing
 * any lines they can.
 * 
 * @author Ben Thomas
 */
public class AIBehaviourWin extends AIBehaviourComplete {

    @Override
    public boolean tryMakeMove(AIPlayer p) {
        Point selectedPoint = this.findPoint(p.getGame().getBoard(), p.getMark());

        if (selectedPoint != null) {
            p.getGame().getBoard().setMark(selectedPoint.x, selectedPoint.y, p.getMark());
            return true;
        } else {
            return false;
        }
    }

}
