package me.benthomas.tttworld.server.command;

import java.io.IOException;

import me.benthomas.tttworld.server.net.TTTWClientConnection;

public interface Command {
    public String getCommandName();
    public void execute(TTTWClientConnection client, String[] args) throws CommandException, IOException;
}
