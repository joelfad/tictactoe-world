package me.benthomas.tttworld.client;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;
import me.benthomas.tttworld.client.ui.MainFrame;

public class Main {
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().displayServerSelectDialog();
            }
        });
    }
}
