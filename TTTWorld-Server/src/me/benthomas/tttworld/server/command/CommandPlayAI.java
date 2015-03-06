package me.benthomas.tttworld.server.command;

import java.io.IOException;
import java.util.UUID;

import me.benthomas.tttworld.Mark;
import me.benthomas.tttworld.server.NetPlayer;
import me.benthomas.tttworld.server.ai.AIPlayerBlocking;
import me.benthomas.tttworld.server.ai.AIPlayerRandom;
import me.benthomas.tttworld.server.ai.AIPlayerSmart;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * A command which causes the creation of a new game against an AI opponent. Has
 * a syntax of <code>:playai {random|blocking|smart} {x|o}</code>, with the
 * first argument indicating the behaviour of the AI and the second argument
 * indicating which mark the player will place.
 * <p>
 * Can be executed by any user on the server.
 *
 * @author Ben Thomas
 */
public class CommandPlayAI implements Command {
    
    private void sendSyntax(TTTWClientConnection client) {
        client.sendMessage("Correct syntax is :playai {random|blocking|smart} {x|o}");
    }
    
    @Override
    public String getCommandName() {
        return ":playai";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (args.length != 3) {
            this.sendSyntax(client);
            return;
        }
        
        Mark playerMark, aiMark;
        
        if (args[2].equalsIgnoreCase("x")) {
            playerMark = Mark.X;
            aiMark = Mark.O;
        } else if (args[2].equalsIgnoreCase("o")) {
            playerMark = Mark.O;
            aiMark = Mark.X;
        } else {
            this.sendSyntax(client);
            return;
        }
        
        if (args[1].equalsIgnoreCase("random")) {
            client.getServer().getGameManager()
                    .createGame(UUID.randomUUID(), new NetPlayer(client, playerMark), new AIPlayerRandom("AI", aiMark));
        } else if (args[1].equalsIgnoreCase("blocking")) {
            client.getServer().getGameManager()
                    .createGame(UUID.randomUUID(), new NetPlayer(client, playerMark), new AIPlayerBlocking("AI", aiMark));
        } else if (args[1].equalsIgnoreCase("smart")) {
            client.getServer().getGameManager()
                    .createGame(UUID.randomUUID(), new NetPlayer(client, playerMark), new AIPlayerSmart("AI", aiMark));
        } else {
            this.sendSyntax(client);
            return;
        }
    }
    
}
