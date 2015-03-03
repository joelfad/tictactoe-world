package me.benthomas.tttworld.server;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import me.benthomas.tttworld.Crypto;
import me.benthomas.tttworld.net.PacketGlobalPlayerList.PlayerInfo;

public class Account {
    private static final String SALT_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SALT_LENGTH = 10;
    
    private static SecureRandom saltRandom = new SecureRandom();
    
    private final AccountManager manager;
    private final String name;
    
    private String passwordSalt;
    private byte[] passwordHash;
    
    private HashMap<String, String> meta;
    private List<MetadataListener> metaListeners = new ArrayList<MetadataListener>();
    
    private static String generateNewSalt() {
        StringBuilder b = new StringBuilder();
        
        for (int i = 0; i < Account.SALT_LENGTH; i++) {
            b.append(Account.SALT_CHARS.charAt(Account.saltRandom.nextInt(Account.SALT_CHARS.length())));
        }
        
        return b.toString();
    }
    
    protected Account(AccountManager manager, String name) {
        this.manager = manager;
        this.name = name;
        
        this.passwordSalt = null;
        this.passwordHash = null;
        
        this.meta = new HashMap<String, String>();
    }
    
    protected Account(AccountManager manager, String name, String data) {
        this.manager = manager;
        this.name = name;
        
        String[] dataParts = data.split("\\$");
        
        this.passwordSalt = dataParts[0];
        this.passwordHash = Crypto.decodeFromBase64(dataParts[1]);
        
        this.meta = new HashMap<String, String>();
        
        for (int i = 2; i < dataParts.length; i++) {
            String metaName = dataParts[i].substring(0, dataParts[i].indexOf("="));
            String metaValue = dataParts[i].substring(dataParts[i].indexOf("=") + 1);
            
            this.meta.put(metaName, metaValue);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    private byte[] calculatePasswordHash(String password) {
        return Crypto.calculateSHA1((password + this.passwordSalt).getBytes(StandardCharsets.UTF_8));
    }
    
    public boolean isPassword(String password) {
        return Arrays.equals(this.calculatePasswordHash(password), this.passwordHash);
    }
    
    public void setPassword(String password) {
        this.passwordSalt = Account.generateNewSalt();
        this.passwordHash = this.calculatePasswordHash(password);
        
        this.manager.saveAccount(this);
    }
    
    public void addMetadataListener(MetadataListener listen) {
        this.metaListeners.add(listen);
    }
    
    public void removeMetadataListener(MetadataListener listen) {
        this.metaListeners.remove(listen);
    }
    
    public String getMeta(String name, String defaultValue) {
        return (this.meta.containsKey(name)) ? this.meta.get(name) : defaultValue;
    }
    
    public void setMeta(String name, String value) {
        if (value != null) {
            this.meta.put(name, value);
        } else {
            this.meta.remove(name);
        }
        
        this.manager.saveAccount(this);
        
        for (MetadataListener l : this.metaListeners) {
            l.onSet(name, value);
        }
    }
    
    public boolean isBanned() {
        return Boolean.parseBoolean(this.getMeta("banned", "false"));
    }
    
    public void setBanned(boolean banned) {
        this.setMeta("banned", String.valueOf(banned));
    }
    
    public boolean isAdmin() {
        return Boolean.parseBoolean(this.getMeta("admin", "false"));
    }
    
    public void setAdmin(boolean admin) {
        this.setMeta("admin", String.valueOf(admin));
    }
    
    public List<String> getFriends() {
        String meta = this.getMeta("friends", null);
        
        if (meta == null) {
            return new ArrayList<String>();
        } else {
            ArrayList<String> f = new ArrayList<String>();
            Collections.addAll(f, meta.split(","));
            
            return f;
        }
    }
    
    public void setFriends(List<String> f) {
        if (f.isEmpty()) {
            this.setMeta("friends", null);
        } else {
            StringBuilder b = new StringBuilder(f.get(0));
            
            for (int i = 1; i < f.size(); i++) {
                b.append(",");
                b.append(f.get(i));
            }
            
            this.setMeta("friends", b.toString());
        }
    }
    
    public void addFriend(String friend) {
        List<String> f = this.getFriends();
        
        if (!f.contains(friend)) {
            f.add(friend);
            this.setFriends(f);
        }
    }
    
    public void removeFriend(String friend) {
        List<String> f = this.getFriends();
        
        if (f.contains(friend)) {
            f.remove(f);
            this.setFriends(f);
        }
    }
    
    public PlayerInfo toPlayerInfo() {
        return new PlayerInfo(this.name, this.isAdmin());
    }
    
    protected String getData() {
        StringBuilder b = new StringBuilder(this.passwordSalt);
        
        b.append("$");
        b.append(Crypto.encodeToBase64(this.passwordHash));
        
        for (Entry<String, String> meta : this.meta.entrySet()) {
            b.append("$");
            b.append(meta.getKey());
            b.append("=");
            b.append(meta.getValue());
        }
        
        return b.toString();
    }
    
    public interface MetadataListener {
        public void onSet(String name, String value);
    }
    
}
