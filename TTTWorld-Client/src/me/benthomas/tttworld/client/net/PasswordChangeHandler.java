package me.benthomas.tttworld.client.net;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import me.benthomas.tttworld.net.PacketPasswordChangeResult;
import me.benthomas.tttworld.net.PacketPasswordChangeResult.Result;
import me.benthomas.tttworld.net.TTTWConnection.PacketHandler;
import me.benthomas.tttworld.client.ui.CreatePasswordDialog;

public class PasswordChangeHandler implements PacketHandler<PacketPasswordChangeResult> {
    private TTTWServerConnection server;
    private CreatePasswordDialog dialog;
    private boolean reset;
    
    public PasswordChangeHandler(TTTWServerConnection server, CreatePasswordDialog dialog, boolean reset) {
        this.server = server;
        this.dialog = dialog;
        this.reset = reset;
    }
    
    @Override
    public void handlePacket(PacketPasswordChangeResult packet) throws IOException {
        Result r = packet.getResult();
        
        if (r == Result.OK) {
            this.server.setHandler(PacketPasswordChangeResult.class, null);
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    PasswordChangeHandler.this.dialog.dispose();
                    
                    if (PasswordChangeHandler.this.reset) {
                        JOptionPane.showMessageDialog(PasswordChangeHandler.this.server.getFrame(),
                                "That user's password has been successfully reset!", "Information",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(PasswordChangeHandler.this.server.getFrame(),
                                "Your password has been successufully changed!", "Information", JOptionPane.INFORMATION_MESSAGE);
                    }
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
        } else if (r == Result.NOT_ALLOWED) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "You are not allowed to do that!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else if (r == Result.USER_NOT_FOUND) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "The user you tried to reset the password of couldn't be found", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "An unknown error occurred while performing the password change!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
}
