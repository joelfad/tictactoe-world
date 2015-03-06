package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A command used to send a new tic-tac-toe challenge to the specified user. Has
 * a syntax of <code>:challenge &lt;player&gt;</code>.
 * <p>
 * Can be executed by any user on the server.
 *
 * @author Ben Thomas
 */
public class CommandChallenge implements Command {
    
    @Override
    public String getCommandName() {
        return ":challenge";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (args.length != 2) {
            client.sendMessage("Correct syntax is :challenge <player>");
            return;
        }
        
        TTTWClientConnection toChallenge = client.getServer().getPlayer(args[1]);
        
        if (toChallenge != null) {
            if (!client.getServer().getGameManager().doesChallengeExist(client, toChallenge)) {
                if (toChallenge != client) {
                    client.getServer().getGameManager().sendChallenge(client, toChallenge);
                    client.sendMessage("Your challenge has been sent...");
                } else {
                    client.sendMessage("You can't duel yourself, crazy person!");
                }
            } else {
                client.sendMessage("You already challenged this player. Give them some time to accept!");
            }
        } else {
            client.sendMessage("Could not find player " + args[1]);
        }
    }
    
}
