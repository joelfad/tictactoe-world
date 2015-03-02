package me.benthomas.tttworld.client;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JOptionPane;

import me.benthomas.tttworld.Crypto;

public class KnownHosts {
    private static HashMap<String, byte[]> hosts;
    
    public static boolean doPrompt(Component parent, String host, byte[] fingerprint) {
        try {
            if (!KnownHosts.isKnown(host)) {
                if (JOptionPane.showOptionDialog(parent, KnownHosts.getUnknownWarning(fingerprint), "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] { "Yes", "No" }, "Yes") == 1) {
                    return false;
                } else {
                    try {
                        KnownHosts.addHost(host, fingerprint);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null,
                                "Failed to save the known hosts file!\nYou will be asked to verify again next time...", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (!Arrays.equals(fingerprint, KnownHosts.getHostFingerprint(host))) {
                if (JOptionPane.showOptionDialog(parent, KnownHosts.getChangedWarning(fingerprint), "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] { "Yes", "No" }, "Yes") == 1) {
                    return false;
                } else {
                    try {
                        KnownHosts.addHost(host, fingerprint);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null,
                                "Failed to save the known hosts file!\nYou will be asked to verify again next time...", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String getUnknownWarning(byte[] fingerprint) {
        return "The server you are currently connecting to is not a known host. The fingerprint of\n"
                + "server you are connecting to is:\n\n" + Crypto.formatFingerprint(fingerprint) + "\n\n"
                + "It is strongly recommended that you verify that this fingerprint is correct before\n"
                + "attempting to connect. Do you wish to continue connecting?";
    }
    
    private static String getChangedWarning(byte[] fingerprint) {
        return "The public key of the server you are connecting to has changed since you last\n"
                + "connected. This could be a sign that your connection is being tampered with.\n"
                + "The fingerprint of the server you are connecting to is:\n\n" + Crypto.formatFingerprint(fingerprint) + "\n\n"
                + "It is strongly recommended that you stop trying to connect immediately and\n"
                + "verify that the public key change was intentional! Do you wish to continue\n" + "connecting?";
    }
    public static void load() throws IOException {
        KnownHosts.hosts = new HashMap<String, byte[]>();
        
        if (new File("hosts.txt").exists()) {
            Properties p = new Properties();
            
            try (FileInputStream in = new FileInputStream("hosts.txt")) {
                p.load(in);
            }
            
            for (Entry<Object, Object> host : p.entrySet()) {
                try {
                    KnownHosts.hosts.put(host.getKey().toString(), Base64.getDecoder().decode(host.getValue().toString()));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid entries
                }
            }
        }
    }
    
    public static boolean isKnown(String host) throws IOException {
        if (KnownHosts.hosts == null) {
            KnownHosts.load();
        }
        
        return KnownHosts.hosts.containsKey(host.toLowerCase());
    }
    
    public static byte[] getHostFingerprint(String host) throws IOException {
        if (KnownHosts.hosts == null) {
            KnownHosts.load();
        }
        
        return KnownHosts.hosts.get(host);
    }
    
    public static void addHost(String host, byte[] fingerprint) throws IOException {
        if (KnownHosts.hosts == null) {
            KnownHosts.load();
        }
        
        KnownHosts.hosts.put(host, fingerprint);
        KnownHosts.save();
    }
    
    private static void save() throws IOException {
        Properties p = new Properties();
        
        for (Entry<String, byte[]> e : KnownHosts.hosts.entrySet()) {
            p.put(e.getKey(), Base64.getEncoder().encodeToString(e.getValue()));
        }
        
        try (FileOutputStream out = new FileOutputStream("hosts.txt")) {
            p.store(out, "Tic-Tac-Toe World Known Hosts");
        }
    }
}
