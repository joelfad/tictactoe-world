package me.benthomas.tttworld.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import me.benthomas.tttworld.Crypto;
import me.benthomas.tttworld.net.PacketGlobalPlayerList;
import me.benthomas.tttworld.net.PacketGlobalPlayerList.PlayerInfo;
import me.benthomas.tttworld.net.PacketKeepAlive;
import me.benthomas.tttworld.net.TTTWConnection;
import me.benthomas.tttworld.server.net.TTTWClientConnection;

/**
 * Represents a TTTW-compliant Tic-Tac-Toe server.
 *
 * @author Ben Thomas
 */
public class Server {
    private static final int PACKET_HANDLE_THREADS = 3;
    private static final long DISPATCH_PERIOD = 50;
    
    private static final long KEEP_ALIVE_SEND_TIME = 20000;
    private static final long KEEP_ALIVE_DISCONNECT_TIME = 30000;
    
    /**
     * Starts a server using the given command-line arguments.
     * 
     * @param args Command-line arguments to pass to the server.
     * @throws IOException An unrecoverable error has occurred while loading
     *             server properties.
     */
    public static void main(String[] args) throws IOException {
        Properties p = new Properties();
        
        if (new File("server.properties").isFile()) {
            p.load(new FileInputStream(new File("server.properties")));
        }
        
        new Server(new Properties()).start(15060);
    }
    
    private ClientAcceptThread clientAccepter;
    private ServerSocket acceptSocket;
    
    private DispatchThread dispatchThread;
    private List<PacketHandleThread> packetThreads = new ArrayList<PacketHandleThread>();
    
    private List<TTTWClientConnection> connectedPlayers = new ArrayList<TTTWClientConnection>();
    
    private AccountManager accountManager;
    private GameManager gameManager;
    
    private String serverName;
    private int compressThreshold;
    private boolean allowRegister;
    
    private KeyPair keyPair;
    
    private Server(Properties p) {
        try {
            this.accountManager = new AccountManager();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read account database!");
        }
        
        this.gameManager = new GameManager(Long.parseLong(p.getProperty("challenge_timeout", "60")) * 1000);
        
        this.serverName = p.getProperty("server_name", "Aviansie Ben's Tic-Tac-Toe World");
        this.compressThreshold = Integer.parseInt(p.getProperty("compress_threshold", "256"));
        this.allowRegister = Boolean.parseBoolean(p.getProperty("allow_register", "true"));
        
        try {
            this.loadKeyPair(p.getProperty("key_file", "server.pk8"), p.getProperty("public_key_file", "server.crt"));
            System.out.println("Public key fingerprint is " + this.getPublicKeyFingerprint());
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new RuntimeException("Private/public key pair could not be read!", e);
        }
        
        if (TTTWConnection.DEBUG_NO_ENCRYPTION) {
            System.out.println("## WARNING: Server is operating without encryption for debugging purposes! ##");
        }
    }
    
    private void loadKeyPair(String privateFile, String publicFile) throws NoSuchAlgorithmException, IOException,
            InvalidKeySpecException {
        File pr = new File(privateFile);
        File pu = new File(publicFile);
        
        if (pr.exists() && pu.exists()) {
            PrivateKey prKey;
            PublicKey puKey;
            
            // Load the private key
            try (FileInputStream i = new FileInputStream(pr)) {
                byte[] encoded = new byte[(int) pr.length()];
                i.read(encoded);
                
                prKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
            }
            
            // Load the public key
            try (FileInputStream i = new FileInputStream(pu)) {
                byte[] encoded = new byte[(int) pu.length()];
                i.read(encoded);
                
                puKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
            }
            
            this.keyPair = new KeyPair(puKey, prKey);
        } else if (!pr.exists() && !pu.exists()) {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(4096);
            
            System.out.println("Generating new key pair... This may take a while...");
            this.keyPair = gen.generateKeyPair();
            
            // Save the private key
            try (FileOutputStream o = new FileOutputStream(pr)) {
                PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(this.keyPair.getPrivate().getEncoded());
                
                o.write(privKeySpec.getEncoded());
            }
            
            // Save the public key
            try (FileOutputStream o = new FileOutputStream(pu)) {
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(this.keyPair.getPublic().getEncoded());
                
                o.write(pubKeySpec.getEncoded());
            }
        } else {
            throw new RuntimeException("Private/public key pair could not be read!");
        }
    }
    
    private String getPublicKeyFingerprint() {
        return Crypto.formatFingerprint(Crypto.calculateSHA1(this.getEncodedPublicKey()));
    }
    
    /**
     * Gets the key pair that this server is using for security.
     * 
     * @return The RSA key pair of this server.
     */
    public KeyPair getKeyPair() {
        return this.keyPair;
    }
    
    /**
     * Gets this server's RSA public key in an encoded format to be sent to a
     * client.
     * 
     * @return This server's RSA in an encoded format to be sent to a client.
     */
    public byte[] getEncodedPublicKey() {
        return new X509EncodedKeySpec(this.keyPair.getPublic().getEncoded()).getEncoded();
    }
    
    /**
     * Gets this server's account manager.
     * 
     * @return This server's account manager.
     */
    public AccountManager getAccountManager() {
        return this.accountManager;
    }
    
    /**
     * Gets this server's game manager.
     * 
     * @return This server's game manager.
     */
    public GameManager getGameManager() {
        return this.gameManager;
    }
    
    /**
     * Gets the name of this server as it should be sent to clients.
     * 
     * @return This server's name.
     */
    public String getName() {
        return this.serverName;
    }
    
    /**
     * Gets the compression thershold used by this server.
     * 
     * @return This server's compression threshold.
     */
    public int getCompressionThreshold() {
        return this.compressThreshold;
    }
    
    /**
     * Checks whether self-registration is allowed on this server.
     * 
     * @return Whether this server allows self-registration of user accounts.
     */
    public boolean isRegistrationAllowed() {
        return this.allowRegister;
    }
    
    /**
     * Gets a list of clients that are currently connected to this server.
     * 
     * @return A list of connected clients.
     */
    public List<TTTWClientConnection> getConnectedPlayers() {
        return this.connectedPlayers;
    }
    
    /**
     * Gets the client connection authenticated as a given account.
     * 
     * @param a The account to find the authenticated client for.
     * @return The client authenticated as the provided user account, or
     *         {@code null} if that user is not connected.
     */
    public TTTWClientConnection getPlayer(Account a) {
        synchronized (this.connectedPlayers) {
            for (TTTWClientConnection player : this.connectedPlayers) {
                if (player.getAccount() == a) {
                    return player;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets the client connection authenticated as a user account with the given
     * name.
     * 
     * @param name The name of the user account to find the client of.
     * @return The client authenticated as the user account with the given name,
     *         or {@code null} if that user is not connected.
     */
    public TTTWClientConnection getPlayer(String name) {
        synchronized (this.connectedPlayers) {
            for (TTTWClientConnection player : this.connectedPlayers) {
                if (player.getAccount() != null && player.getAccount().getName().equalsIgnoreCase(name)) {
                    return player;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Sends a global chat broadcast to all players who are currently connected
     * to this server.
     * 
     * @param message The message to be broadcast to all connected clients.
     */
    public void sendGlobalBroadcast(String message) {
        synchronized (this.connectedPlayers) {
            for (TTTWClientConnection player : this.connectedPlayers) {
                if (player.getAccount() != null) {
                    player.sendMessage(message);
                }
            }
        }
        
        synchronized (System.out) {
            System.out.println(message);
        }
    }
    
    /**
     * Broadcasts the current list of connected players to all connected
     * clients.
     */
    public void sendPlayerList() {
        HashMap<TTTWClientConnection, Account> players = new HashMap<TTTWClientConnection, Account>();
        
        synchronized (this.connectedPlayers) {
            for (TTTWClientConnection player : this.connectedPlayers) {
                synchronized (player) {
                    if (!player.isDisconnecting() && player.isAlive() && player.getAccount() != null) {
                        players.put(player, player.getAccount());
                    }
                }
            }
        }
        
        for (Entry<TTTWClientConnection, Account> sendPlayer : players.entrySet()) {
            List<PlayerInfo> playerList = new ArrayList<PlayerInfo>();
            
            for (Entry<TTTWClientConnection, Account> listPlayer : players.entrySet()) {
                if (sendPlayer.getKey() != listPlayer.getKey()) {
                    playerList.add(listPlayer.getValue().toPlayerInfo());
                }
            }
            
            try {
                sendPlayer.getKey().sendPacket(new PacketGlobalPlayerList(playerList));
            } catch (IOException e) {
                // Ignore it. We can't disconnect users while we're updating
                // player lists...
            }
        }
    }
    
    private void start(int port) throws IOException {
        this.acceptSocket = new ServerSocket(port);
        
        this.clientAccepter = new ClientAcceptThread();
        this.clientAccepter.start();
        
        for (int i = 0; i < Server.PACKET_HANDLE_THREADS; i++) {
            PacketHandleThread t = new PacketHandleThread(i + 1);
            packetThreads.add(t);
            t.start();
        }
        
        this.dispatchThread = new DispatchThread();
        this.dispatchThread.start();
        
        synchronized (System.out) {
            System.out.println("Server is now accepting clients...");
        }
    }
    
    /**
     * Stops the server, causing all packet handler threads, the dispatch
     * thread, and the client accepter thread to be stopped.
     */
    public void stop() {
        this.clientAccepter.interrupt();
        
        try {
            this.acceptSocket.close();
        } catch (IOException e) {
            // Ignore problems closing the server socket
        }
        
        this.dispatchThread.interrupt();
        
        for (PacketHandleThread t : packetThreads) {
            t.interrupt();
        }
    }
    
    private class ClientAcceptThread extends Thread {
        private ClientAcceptThread() {
            super("Client Accepter");
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    Socket s = Server.this.acceptSocket.accept();
                    
                    synchronized (Server.this.connectedPlayers) {
                        TTTWClientConnection client = new TTTWClientConnection(s, Server.this);
                        client.setCompressionThreshold(Server.this.compressThreshold);
                        
                        Server.this.connectedPlayers.add(client);
                        
                        synchronized (System.out) {
                            System.out.println(client.getAddress() + " has connected");
                        }
                    }
                } catch (IOException e) {
                    if (Thread.interrupted()) {
                        break; // The socket was closed because the server is
                               // shutting down
                    } else {
                        synchronized (System.out) {
                            System.err.println("Failed to accept client:");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    private class DispatchThread extends Thread {
        public DispatchThread() {
            super("Dispatch Thread");
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(Server.DISPATCH_PERIOD);
                    
                    synchronized (Server.this.connectedPlayers) {
                        Iterator<TTTWClientConnection> i = Server.this.connectedPlayers.iterator();
                        
                        while (i.hasNext()) {
                            TTTWClientConnection c = i.next();
                            
                            try {
                                if (!c.isAlive()) {
                                    i.remove();
                                } else if (!c.isHandling() && c.isPacketWaiting()) {
                                    // Find an idle packet handler to handle the
                                    // packet
                                    for (PacketHandleThread t : Server.this.packetThreads) {
                                        if (t.handling == null) {
                                            c.setHandling(true);
                                            t.handling = c;
                                            
                                            synchronized (t.wakeUp) {
                                                t.wakeUp.notifyAll();
                                            }
                                            
                                            break;
                                        }
                                    }
                                } else if (c.getTimeSinceLastPacket() > Server.KEEP_ALIVE_DISCONNECT_TIME) {
                                    c.disconnect("Connection timed out");
                                    i.remove();
                                } else if (c.getTimeSinceLastPacket() > Server.KEEP_ALIVE_SEND_TIME
                                        && !c.getKeepAliveSentSinceLastPacket()) {
                                    c.sendPacket(new PacketKeepAlive());
                                }
                            } catch (IOException e) {
                                c.disconnect("Error while checking for new packets!");
                                i.remove();
                            }
                        }
                    }
                    
                    Server.this.gameManager.tick();
                }
            } catch (InterruptedException e) {
                // Server is shutting down. Terminate the thread.
            }
            
            synchronized (Server.this.connectedPlayers) {
                for (TTTWClientConnection client : Server.this.connectedPlayers) {
                    client.disconnect("Server is shutting down!");
                }
            }
        }
    }
    
    private class PacketHandleThread extends Thread {
        private volatile TTTWClientConnection handling;
        private Object wakeUp = new Object();
        
        private PacketHandleThread(int n) {
            super("Packet Handler #" + n);
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (this.wakeUp) {
                        this.wakeUp.wait();
                    }
                    
                    if (this.handling != null) {
                        synchronized (this.handling) {
                            try {
                                this.handling.handleNextPacket();
                            } catch (IOException e) {
                                synchronized (System.out) {
                                    System.err.println("Error receiving packet from client:");
                                    e.printStackTrace();
                                }
                                
                                this.handling.disconnect("Error reading packet");
                            }
                        }
                        
                        this.handling.setHandling(false);
                        this.handling = null;
                    }
                }
            } catch (InterruptedException e) {
                // Server is shutting down. Terminate the thread.
            }
        }
    }
}
