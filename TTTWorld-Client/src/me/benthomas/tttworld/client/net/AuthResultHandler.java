package me.benthomas.tttworld.client.net;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import me.benthomas.tttworld.net.PacketAuthResult;
import me.benthomas.tttworld.net.PacketAuthResult.Result;
import me.benthomas.tttworld.net.PacketChallenge;
import me.benthomas.tttworld.net.PacketGameUpdate;
import me.benthomas.tttworld.net.PacketGlobalChat;
import me.benthomas.tttworld.net.PacketGlobalPlayerList;
import me.benthomas.tttworld.net.TTTWConnection.PacketFilter;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;

public class AuthResultHandler implements PacketHandler<PacketAuthResult> {
    private TTTWServerConnection server;
    
    public AuthResultHandler(TTTWServerConnection server) {
        this.server = server;
    }
    
    @Override
    public void handlePacket(PacketAuthResult packet) throws IOException {
        Result r = packet.getResult();
        
        if (r == Result.BAD_CRED || r == Result.UNKNOWN) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "The username or password you entered was not recognized!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    
                }
            });
        } else if (r == Result.BANNED) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Your account is currently banned!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else if (r == Result.NAME_TAKEN) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "The name you tried to register is already taken!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        } else if (r == Result.PASSWORD_SHORT) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "The password you entered is too short!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        } else if (r == Result.UNKNOWN) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "An unknown error occurred during authentication!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        } else if (r == Result.OK) {
            this.server.getFrame().setAdmin(packet.isAdmin());
            this.server.getFrame().setUsername(packet.getUsername());
            
            this.server.addFilteredHandler(PacketAuthResult.class, new PostAuthResultFilter(), new PostAuthResultHandler(this.server));
            this.server.setDefaultHandler(PacketAuthResult.class, null);
            
            this.server.setDefaultHandler(PacketGlobalChat.class, new GlobalChatHandler(this.server));
            this.server.setDefaultHandler(PacketGlobalPlayerList.class, new GlobalPlayerListHandler(this.server));
            this.server.setDefaultHandler(PacketChallenge.class, new ChallengeHandler(this.server));
            this.server.setDefaultHandler(PacketGameUpdate.class, new GameUpdateHandler(this.server));
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AuthResultHandler.this.server.getFrame().getLoginDialog().close();
                    AuthResultHandler.this.server.getFrame().displayMainFrame();
                }
            });
        }
    }
    
    public static class PostAuthResultFilter implements PacketFilter<PacketAuthResult> {
        @Override
        public boolean isFiltered(PacketAuthResult packet) {
            return packet.getResult() != Result.OK;
        }
    }
    
    public static class PostAuthResultHandler implements PacketHandler<PacketAuthResult> {
        private TTTWServerConnection server;
        
        public PostAuthResultHandler(TTTWServerConnection server) {
            this.server = server;
        }
        
        @Override
        public void handlePacket(PacketAuthResult packet) throws IOException {
            this.server.getFrame().setUsername(packet.getUsername());
            this.server.getFrame().setAdmin(packet.isAdmin());
        }
        
    }
    
}
