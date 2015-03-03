package me.benthomas.tttworld.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import me.benthomas.tttworld.Crypto;

public class TTTWConnection {
    public static final int PROTOCOL_MAJOR_VERSION = 2;
    public static final int PROTOCOL_MINOR_VERSION = 1;
    
    public static final long MAX_CLOCK_DEVIATION = (60 * 1000) * 5;
    
    public static final boolean DEBUG_NO_ENCRYPTION = false;
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
    
    public TTTWConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        
        this.setDefaultHandler(PacketDisconnect.class, new DisconnectHandler());
        
        this.lastPacket = System.currentTimeMillis();
    }
    
    public long getTimeSinceLastPacket() {
        return System.currentTimeMillis() - this.lastPacket;
    }
    
    public boolean getKeepAliveSentSinceLastPacket() {
        return this.keepAliveSent;
    }
    
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
    
    public synchronized void addDisconnectListener(DisconnectListener l) {
        if (!this.disconnectListeners.contains(l)) {
            this.disconnectListeners.add(l);
        }
    }
    
    public synchronized void removeDisconnectListener(DisconnectListener l) {
        this.disconnectListeners.remove(l);
    }
    
    public String getAddress() {
        return this.socket.getInetAddress() + ":" + this.socket.getPort();
    }
    
    public boolean isEncrypted() {
        return this.cryptKey != null;
    }
    
    public byte[] getEncryptionKey() {
        return this.cryptKey;
    }
    
    public byte[] generateEncryptionKey() {
        byte[] key = new byte[16];
        this.cryptRandom.nextBytes(key);
        
        return key;
    }
    
    public synchronized void setEncryptionKey(byte[] key) {
        this.cryptKey = key;
    }
    
    public int getCompressionThreshold() {
        return this.compressThreshold;
    }
    
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
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
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
    
    public synchronized boolean isPacketWaiting() throws IOException {
        return this.input.available() > 0;
    }
    
    public synchronized Packet receivePacket() throws IOException {
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
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IOException("Bad encryption on packet!", e);
        }
    }
    
    private Packet readCompressedPacket(byte[] compressed, int start) throws IOException {
        return this.readPacket(TTTWConnection.decompress(compressed, start), 0);
    }
    
    private Packet readJsonPacket(byte[] p, int start) throws IOException {
        return Packet.readPacket(new String(p, start, p.length - start, StandardCharsets.UTF_8));
    }
    
    public void handleNextPacket() throws IOException {
        this.handlePacket(this.receivePacket());
    }
    
    public void handlePacket(Packet p) throws IOException {
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
    
    public boolean isAlive() {
        return this.socket != null && !this.socket.isClosed();
    }
    
    public interface PacketHandler<P extends Packet> {
        public void handlePacket(P packet) throws IOException;
    }
    
    public interface PacketFilter<P extends Packet> {
        public boolean isFiltered(P packet);
    }
    
    public interface DisconnectListener {
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
