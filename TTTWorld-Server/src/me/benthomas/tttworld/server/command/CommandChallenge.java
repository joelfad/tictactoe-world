package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

public class CommandChallenge implements Command {
    
    @Override
    public String getCommandName() {
        return ":challenge";
    }
    
    @Override
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException {
        if (args.length != 2) {
            client.sendPacket(new PacketGlobalChat("Correct syntax is :challenge <player>"));
            return;
        }
        
        TTTWClientConnection toChallenge = client.getServer().getPlayer(args[1]);
        
        if (toChallenge != null) {
            if (!client.getServer().getGameManager().doesChallengeExist(client, toChallenge)) {
                if (toChallenge != client) {
                    client.getServer().getGameManager().sendChallenge(client, toChallenge);
                    client.sendPacket(new PacketGlobalChat("Your challenge has been sent..."));
                } else {
                    client.sendPacket(new PacketGlobalChat("You can't duel yourself, crazy person!"));
                }
            } else {
                client.sendPacket(new PacketGlobalChat("You already challenged this player. Give them some time to accept!"));
            }
        } else {
            client.sendPacket(new PacketGlobalChat("Could not find player " + args[1]));
        }
    }
    
}
