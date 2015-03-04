package me.benthomas.tttworld.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import me.benthomas.tttworld.Crypto;
import me.benthomas.tttworld.Crypto.CryptoException;

/**
 * Represents a connection to a TTTW-compliant endpoint. This connection
 * supports sending and receiving any subclasses of {@link Packet} and supports
 * event-driven handling of these packets. This class should be subclassed if
 * further state information is required.
 *
 * @author Ben Thomas
 */
public class TTTWConnection {
    /**
     * The current TTTW protocol major version.
     */
    public static final int PROTOCOL_MAJOR_VERSION = 2;
    
    /**
     * The current TTTW protocol minor version.
     */
    public static final int PROTOCOL_MINOR_VERSION = 1;
    
    /**
     * The maximum time (in milliseconds) that a timestamp on a packet is
     * permitted to deviate from current system time as returned by
     * {@link System#currentTimeMillis()}. Packets which have timestamps with
     * larger deviations than this will be rejected.
     */
    public static final long MAX_CLOCK_DEVIATION = (60 * 1000) * 5;
    
    /**
     * Disables encryption for debugging reasons. This should
     * <strong>never</strong> be {@code true} in a production release.
     */
    public static final boolean DEBUG_NO_ENCRYPTION = false;
    
    /**
     * Disables compression for debugging reasons. This should
     * <strong>never</strong> be {@code true} in a production release.
     */
    public static final boolean DEBUG_NO_COMPRESSION = false;
    
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    
    private int compressThreshold = -1;
    
    private byte[] cryptKey;
    private SecureRandom cryptRandom = new SecureRandom();
    
    private HashMap<Class<? extends Packet>, FilteredHandler<?>> handlers = new HashMap<Class<? extends Packet>, FilteredHandler<?>>();
    private List<DisconnectListener> disconnectListeners = new ArrayList<DisconnectListener>();
    
    private byte[] packetBuffer;
    private int packetBufferPos;
    
    private long lastPacket;
    private boolean keepAliveSent;
    
    /**
     * Creates a new TTTW-compliant connection on top of the given socket.
     * 
     * @param socket The socket on which this connection should operate.
     * 
     * @throws IOException There was an error opening the input or output
     *             streams on the given socket.
     */
    public TTTWConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        
        this.setDefaultHandler(PacketDisconnect.class, new DisconnectHandler());
        
        this.lastPacket = System.currentTimeMillis();
    }
    
    /**
     * Gets the amount of time (in milliseconds) since the last packet was
     * processed.
     * 
     * @return The amount of time since the last processed packet.
     */
    public long getTimeSinceLastPacket() {
        return System.currentTimeMillis() - this.lastPacket;
    }
    
    /**
     * Gets a value indicating whether a {@link PacketKeepAlive} has been sent
     * since the last packet was processed from this connection.
     * 
     * @return Whether a {@link PacketKeepAlive} has been sent to this
     *         connection since the last received packet.
     */
    public boolean getKeepAliveSentSinceLastPacket() {
        return this.keepAliveSent;
    }
    
    /**
     * Sets the default handler for the given type of packet. The default
     * handler for a packet is used only when no filtered handler is capable of
     * handling the packet.
     * 
     * @param <P> The type of packet being handled.
     * 
     * @param packetType The type of packet for which to set the default
     *            handler.
     * @param handler The default handler which should be used for the given
     *            packet type.
     */
    @SuppressWarnings("unchecked")
    public synchronized <P extends Packet> void setDefaultHandler(Class<P> packetType, PacketHandler<P> handler) {
        if (!this.handlers.containsKey(packetType)) {
            if (handler != null) {
                FilteredHandler<P> internalHandler = new FilteredHandler<P>();
                internalHandler.defaultHandler = handler;
                
                this.handlers.put(packetType, internalHandler);
            }
        } else {
            FilteredHandler<P> internalHandler = ((FilteredHandler<P>) this.handlers.get(packetType));
            internalHandler.defaultHandler = handler;
            
            if (internalHandler.handlers.isEmpty() && internalHandler.defaultHandler == null) {
                this.handlers.remove(packetType);
            }
        }
    }
    
    /**
     * Adds a filtered handler for the given type of packet. A filtered handler
     * is preferred over the default handler for a specific type of packet if
     * the filtered handler is capable of handling the packet. If multiple
     * filtered handlers are capable of handling a packet, it is
     * <em>undefined</em> which handler is used.
     * 
     * @param <P> The type of packet being handled.
     * 
     * @param packetType The type of packet for which a filtered handler should
     *            be added.
     * @param filter The filter that should be used to filter packets destined
     *            for this handler.
     * @param handler The handler which should be executed for any packets
     *            matching the given filter.
     */
    @SuppressWarnings("unchecked")
    public synchronized <P extends Packet> void addFilteredHandler(Class<P> packetType, PacketFilter<? super P> filter,
            PacketHandler<P> handler) {
        if (!this.handlers.containsKey(packetType)) {
            FilteredHandler<P> internalHandler = new FilteredHandler<P>();
            internalHandler.handlers.put(handler, filter);
            
            this.handlers.put(packetType, internalHandler);
        } else {
            ((FilteredHandler<P>) this.handlers.get(packetType)).handlers.put(handler, filter);
        }
    }
    
    /**
     * Removes a filtered handler for the given type of packet. If the handler
     * provided is not a filtered handler for the given type of packet, no
     * action is taken.
     * 
     * @param <P> The type of packet being handled.
     * 
     * @param packetType The type of packet for which a filtered handler should
     *            be removed.
     * @param handler The filtered handler that should be removed.
     */
    @SuppressWarnings("unchecked")
    public synchronized <P extends Packet> void removeFilteredHandler(Class<P> packetType, PacketHandler<P> handler) {
        if (this.handlers.containsKey(packetType)) {
            FilteredHandler<P> internalHandler = (FilteredHandler<P>) this.handlers.get(packetType);
            internalHandler.handlers.remove(handler);
            
            if (internalHandler.handlers.isEmpty() && internalHandler.defaultHandler == null) {
                this.handlers.remove(packetType);
            }
        }
    }
    
    /**
     * Adds a new disconnection listener to this connection. Disconnection
     * listeners will be executed in the order in which they were added when a
     * client disconnects or is disconnected. Adding the same handler multiple
     * times to a single connection is not allowed, and will be ignored.
     * 
     * @param l The disconnection listener which should be added to this
     *            connection.
     */
    public synchronized void addDisconnectListener(DisconnectListener l) {
        if (!this.disconnectListeners.contains(l)) {
            this.disconnectListeners.add(l);
        }
    }
    
    /**
     * Removes a disconnection listener from this connection. If the given
     * disconnection listener is not listening on this connection, no action is
     * taken.
     * 
     * @param l The disconnection listener which should be removed from this
     *            connection.
     */
    public synchronized void removeDisconnectListener(DisconnectListener l) {
        this.disconnectListeners.remove(l);
    }
    
    /**
     * Gets the IP address and remote port of this connection in a
     * human-readable format.
     * 
     * @return A string representing the IP address and remote port of this
     *         connection.
     */
    public String getAddress() {
        return this.socket.getInetAddress() + ":" + this.socket.getPort();
    }
    
    /**
     * Checks whether encryption is currently enabled for outgoing packets on
     * this connection. If {@code true}, encrypted packets can be received and
     * decrypted and all outgoing packets will be encrypted. If {@code false},
     * encrypted packets cannot be received or sent using this connection.
     * 
     * @return Whether or not encryption is currently enabled on this
     *         connection.
     */
    public boolean isEncrypted() {
        return this.cryptKey != null;
    }
    
    /**
     * Gets the current encryption key being used to encrypt and decrypt packets
     * by this connection.
     * 
     * @return The AES-128 encryption key currently in use by this connection.
     */
    public byte[] getEncryptionKey() {
        return this.cryptKey;
    }
    
    /**
     * Generates a new, random, 128-bit encryption key. This method <strong>does
     * not</strong> change the encryption key being used by this connection.
     * 
     * @return A random 128-bit encryption key.
     */
    public byte[] generateEncryptionKey() {
        byte[] key = new byte[16];
        this.cryptRandom.nextBytes(key);
        
        return key;
    }
    
    /**
     * Sets the encryption key in use by this connection for encrypting and
     * decrypting packets.
     * 
     * @param key The AES-128 key to be used for packet encryption and
     *            decryption, or {@code null} to disable packet
     *            encryption/decryption.
     */
    public synchronized void setEncryptionKey(byte[] key) {
        this.cryptKey = key;
    }
    
    /**
     * Gets the minimum size of a JSON payload (in bytes) that will be gzip
     * compressed before being sent.
     * 
     * @return The minimum packet size before packets are compressed.
     */
    public int getCompressionThreshold() {
        return this.compressThreshold;
    }
    
    /**
     * Sets the minimum size of a JSON payload (in bytes) that will be gzip
     * compressed before being sent.
     * 
     * @param compressThreshold The new compression threshold.
     */
    public synchronized void setCompressionThreshold(int compressThreshold) {
        this.compressThreshold = compressThreshold;
    }
    
    private synchronized void sendPacketInternal(Packet p) throws IOException {
        if (this.output == null) {
            return; // Closed sockets tell no tales
        }
        
        byte[] pb = this.writeJsonPacket(p);
        
        if (!TTTWConnection.DEBUG_NO_COMPRESSION && this.compressThreshold >= 0 && pb.length >= this.compressThreshold) {
            pb = this.writeCompressedPacket(pb);
        }
        
        if (!TTTWConnection.DEBUG_NO_ENCRYPTION && this.cryptKey != null) {
            pb = this.writeEncryptedPacket(pb);
        }
        
        this.output.writeInt(pb.length);
        this.output.write(pb);
        this.output.flush();
        
        if (p instanceof PacketKeepAlive) {
            this.keepAliveSent = true;
        }
    }
    
    /**
     * Sends a packet to this connection. The packet may be gzip compressed
     * and/or encrypted before being sent depending on the settings of this
     * connection. If this connection is already closed, no action will be
     * taken.
     * 
     * @param p The packet that should be sent to this connection.
     * @throws IOException An error occured while sending the packet.
     */
    public synchronized void sendPacket(Packet p) throws IOException {
        this.sendPacketInternal(p);
    }
    
    private byte[] writeEncryptedPacket(byte[] payload) throws IOException {
        if (this.cryptKey == null) {
            throw new IOException("Encrypted packet sent before encryption initialized!");
        }
        
        try {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
                byteOut.write('e');
                
                byte[] iv = new byte[16];
                this.cryptRandom.nextBytes(iv);
                byteOut.write(iv);
                
                byteOut.write(Crypto.encryptSymmetric(payload, this.cryptKey, iv));
                
                return byteOut.toByteArray();
            }
        } catch (CryptoException e) {
            throw new IOException("Bad encryption on packet!", e);
        }
    }
    
    private byte[] writeCompressedPacket(byte[] payload) throws IOException {
        return TTTWConnection.compress(payload);
    }
    
    private byte[] writeJsonPacket(Packet p) throws IOException {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            byteOut.write('j');
            byteOut.write(p.write().toString().getBytes(StandardCharsets.UTF_8));
            
            return byteOut.toByteArray();
        }
    }
    
    /**
     * Checks whether a packet is waiting to be handled.
     * 
     * @return {@code true} if there is data waiting on the input stream;
     *         {@code false} otherwise.
     * @throws IOException An error occurred while polling the input stream for
     *             new data.
     */
    public synchronized boolean isPacketWaiting() throws IOException {
        return this.input.available() > 0;
    }
    
    private synchronized Packet receivePacket() throws IOException {
        int l;
        
        if (this.packetBuffer == null) {
            this.packetBuffer = new byte[this.input.readInt()];
            this.packetBufferPos = 0;
        }
        
        while (this.packetBufferPos < this.packetBuffer.length
                && (l = this.input.read(this.packetBuffer, this.packetBufferPos, this.packetBuffer.length - this.packetBufferPos)) > 0) {
            this.packetBufferPos += l;
        }
        
        if (this.packetBufferPos == this.packetBuffer.length) {
            byte[] b = this.packetBuffer;
            this.packetBuffer = null;
            
            Packet p = this.readPacket(b, 0);
            
            if (Math.abs(System.currentTimeMillis() - p.getTimestamp()) > TTTWConnection.MAX_CLOCK_DEVIATION) {
                throw new IOException("Bad timestamp on packet! Check your system clock!");
            } else {
                this.lastPacket = System.currentTimeMillis();
                this.keepAliveSent = false;
                
                return p;
            }
        } else {
            return null;
        }
    }
    
    private Packet readPacket(byte[] p, int start) throws IOException {
        if (p[start] == 'e') {
            return this.readEncryptedPacket(p, start + 1);
        } else if (p[start] == 'z') {
            return this.readCompressedPacket(p, start + 1);
        } else if (p[start] == 'j') {
            return this.readJsonPacket(p, start + 1);
        } else {
            throw new IOException("Unrecognized packet type!");
        }
    }
    
    private Packet readEncryptedPacket(byte[] encrypted, int start) throws IOException {
        if (this.cryptKey == null) {
            throw new IOException("Encrypted packet sent before encryption initialized!");
        }
        
        byte[] iv = Arrays.copyOfRange(encrypted, start, start + 16);
        
        try {
            return this.readPacket(Crypto.decryptSymmetric(encrypted, start + 16, this.cryptKey, iv), 0);
        } catch (CryptoException e) {
            throw new IOException("Bad encryption on packet!", e);
        }
    }
    
    private Packet readCompressedPacket(byte[] compressed, int start) throws IOException {
        return this.readPacket(TTTWConnection.decompress(compressed, start), 0);
    }
    
    private Packet readJsonPacket(byte[] p, int start) throws IOException {
        return Packet.readPacket(new String(p, start, p.length - start, StandardCharsets.UTF_8));
    }
    
    /**
     * Handles the next packet on this connection's input stream. If the packet
     * could not be fully read, the currently read buffer will be stored and
     * another attempt to read the remaining data will be made on the next call.
     * 
     * @throws IOException An error occurred while reading or handling the
     *             packet.
     */
    public void handleNextPacket() throws IOException {
        this.handlePacket(this.receivePacket());
    }
    
    private void handlePacket(Packet p) throws IOException {
        if (p == null) {
            return;
        } else if (handlers.containsKey(p.getClass())) {
            this.handlePacket(handlers.get(p.getClass()), p);
        } else if (!(p instanceof PacketKeepAlive)) {
            throw new IOException("Unexpected packet!");
        }
    }
    
    @SuppressWarnings("unchecked")
    private <P extends Packet> void handlePacket(FilteredHandler<P> handler, Packet p) throws IOException {
        handler.handlePacket((P) p);
    }
    
    private static byte[] compress(byte[] payload) throws IOException {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            byteOut.write('z');
            
            GZIPOutputStream gzOut = new GZIPOutputStream(byteOut);
            
            gzOut.write(payload);
            gzOut.finish();
            
            return byteOut.toByteArray();
        }
    }
    
    private static byte[] decompress(byte[] compressed, int start) throws IOException {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(compressed, start, compressed.length - start)) {
                GZIPInputStream gzIn = new GZIPInputStream(byteIn);
                
                byte[] buf = new byte[4096];
                int length;
                
                while ((length = gzIn.read(buf)) > 0) {
                    byteOut.write(buf, 0, length);
                }
            }
            
            return byteOut.toByteArray();
        }
    }
    
    /**
     * Closes this connection, preventing any further data from being sent to
     * it.
     */
    public void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            // Exceptions while closing the connection should be ignored
        }
        
        this.socket = null;
        this.input = null;
        this.output = null;
    }
    
    /**
     * Disconnects this connection with the given disconnection message. Before
     * the actual disconnection, any {@link DisconnectListener}s on this
     * connections will be called.
     * 
     * @param message The message which should be sent when disconnecting.
     */
    public void disconnect(String message) {
        try {
            for (DisconnectListener listener : new ArrayList<DisconnectListener>(TTTWConnection.this.disconnectListeners)) {
                listener.onDisconnect(false, message);
            }
            
            try {
                this.sendPacketInternal(new PacketDisconnect(message));
            } catch (IOException e) {
                // Ignore problems sending the disconnect packet
            }
        } finally {
            this.close();
        }
    }
    
    /**
     * Checks whether this connection is still alive and capable of sending and
     * receiving packets.
     * 
     * @return {@code true} if this connection is still alive; {@code false}
     *         otherwise.
     */
    public boolean isAlive() {
        return this.socket != null && !this.socket.isClosed();
    }
    
    /**
     * Represents a handler for a specific type of packet. May or may not have
     * packets filtered based on how it is registered.
     * 
     * @param <P> The type of packet that this handler is capable of handling.
     * 
     * @author Ben Thomas
     */
    public interface PacketHandler<P extends Packet> {
        /**
         * Handles the given packet, performing any necessary action.
         * 
         * @param packet The packet to be handled.
         * @throws IOException An error occurred while handling the packet.
         */
        public void handlePacket(P packet) throws IOException;
    }
    
    /**
     * Represents a filter that is capable of deciding which packets should be
     * passed onto a given handler and which should not.
     * 
     * @param <P> The type of packet that this filter is capable of filtering.
     *
     * @author Ben Thomas
     */
    public interface PacketFilter<P extends Packet> {
        /**
         * Checks whether the given packet should be passed onto the associated
         * {@link PacketHandler}.
         * 
         * @param packet The packet which should be checked for filtering.
         * @return {@code false} if the handler associated with this filter
         *         should handle the given packet; {@code true} otherwise.
         */
        public boolean isFiltered(P packet);
    }
    
    /**
     * Represents a listener that listens for a specific connection to either
     * send a {@link PacketDisconnect} or be disconnected by the
     * {@link TTTWConnection#disconnect(String)} method.
     *
     * @author Ben Thomas
     */
    public interface DisconnectListener {
        /**
         * Notifies this listener that the associated connection has
         * disconnected or is being disconnected. Unless some form of recursion
         * protection is in place, this method <strong>must never</strong> cause
         * the associated connection to be disconnected.
         * 
         * @param fromRemote {@code true} if this event was the result of a
         *            {@link PacketDisconnect} being received; {@code false} if
         *            this event is the result of a call to
         *            {@link TTTWConnection#disconnect(String)}.
         * @param reason The reason that was provided for the disconnection.
         */
        public void onDisconnect(boolean fromRemote, String reason);
    }
    
    private class FilteredHandler<P extends Packet> {
        private HashMap<PacketHandler<P>, PacketFilter<? super P>> handlers = new HashMap<PacketHandler<P>, PacketFilter<? super P>>();
        private PacketHandler<P> defaultHandler;
        
        public void handlePacket(P packet) throws IOException {
            for (Entry<PacketHandler<P>, PacketFilter<? super P>> handler : this.handlers.entrySet()) {
                if (!handler.getValue().isFiltered(packet)) {
                    handler.getKey().handlePacket(packet);
                    return;
                }
            }
            
            if (defaultHandler != null) {
                defaultHandler.handlePacket(packet);
            } else {
                throw new IOException("Unexpected packet!");
            }
        }
        
    }
    
    private class DisconnectHandler implements PacketHandler<PacketDisconnect> {
        
        @Override
        public void handlePacket(PacketDisconnect packet) {
            for (DisconnectListener listener : new ArrayList<DisconnectListener>(TTTWConnection.this.disconnectListeners)) {
                listener.onDisconnect(true, packet.getReason());
            }
            
            TTTWConnection.this.close();
        }
        
    }
}
