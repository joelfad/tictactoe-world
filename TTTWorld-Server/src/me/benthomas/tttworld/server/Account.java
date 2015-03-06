package me.benthomas.tttworld.server;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import me.benthomas.tttworld.Crypto;
import me.benthomas.tttworld.net.PacketGlobalPlayerList.PlayerInfo;

/**
 * Represents a registered user account on a TTTW server.
 *
 * @author Ben Thomas
 */
public final class Account {
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
    
    Account(AccountManager manager, String name) {
        this.manager = manager;
        this.name = name;
        
        this.passwordSalt = null;
        this.passwordHash = null;
        
        this.meta = new HashMap<String, String>();
    }
    
    Account(AccountManager manager, String name, String data) {
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
    
    /**
     * Gets the canonical username of this user account.
     * 
     * @return The username of this user account.
     */
    public String getName() {
        return this.name;
    }
    
    private byte[] calculatePasswordHash(String password) {
        return Crypto.calculateSHA1((password + this.passwordSalt).getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Checks whether the given password is the correct password for this user.
     * 
     * @param password The password that should be checked against this account.
     * @return {@code true} if the passwords match; {@code false} otherwise.
     */
    public boolean isPassword(String password) {
        return Arrays.equals(this.calculatePasswordHash(password), this.passwordHash);
    }
    
    /**
     * Changes this user account's password to the given password. This
     * <strong>does not</strong> have any effect on a client which is already
     * connected. If the security of the user account is known to be
     * compromised, it is generally a good idea to disconnect any clients
     * connected using this account.
     * <p>
     * Also causes a new random password salt to be generated for this user.
     * 
     * @param password The password to which this user account's password should
     *            be changed.
     */
    public void setPassword(String password) {
        this.passwordSalt = Account.generateNewSalt();
        this.passwordHash = this.calculatePasswordHash(password);
        
        this.manager.saveAccount(this);
    }
    
    /**
     * Adds a new metadata listener that will be notified whenever any metadata
     * on this user account is changed. This is only useful on accounts which
     * are loaded into the user cache semi-permanently. If this account is not
     * marked as sticky in the {@link AccountManager}, then this instance of the
     * user account may be unloaded from memory without warning.
     * 
     * @param listen The metadata listener to be added.
     */
    public void addMetadataListener(MetadataListener listen) {
        this.metaListeners.add(listen);
    }
    
    /**
     * Removes the given metadata listener from this user account so that it is
     * no longer notified of metadata changes.
     * 
     * @param listen The metadata listener to be removed.
     */
    public void removeMetadataListener(MetadataListener listen) {
        this.metaListeners.remove(listen);
    }
    
    /**
     * Gets a metadata value associated with this user's account.
     * 
     * @param name The name of the metadata value to be retrieved.
     * @param defaultValue The default value to return if the given metadata is
     *            not found on this account.
     * @return The value of the requested metadata on this user, or the default
     *         value if it is not specified explicitly.
     */
    public String getMeta(String name, String defaultValue) {
        return (this.meta.containsKey(name)) ? this.meta.get(name) : defaultValue;
    }
    
    /**
     * Sets the given metadata on this user to the requested value.
     * Automatically notifies any relevant {@link MetadataListener}s of the
     * change.
     * 
     * @param name The name of the metadata value to be modified.
     * @param value The value to which the requested metadata should be changed.
     */
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
    
    /**
     * Checks whether this user has been banned from connecting to the server by
     * an administrator.
     * 
     * @return Whether the user is currently banned. If {@code true}, clients
     *         <strong>must</strong> be prevented from authenticating as this
     *         user.
     */
    public boolean isBanned() {
        return Boolean.parseBoolean(this.getMeta("banned", "false"));
    }
    
    /**
     * Sets whether or not this user is banned from connecting to this server.
     * If the user is already connected and is being banned, they must be
     * disconnected separately.
     * 
     * @param banned Whether this user account should be banned from connecting.
     */
    public void setBanned(boolean banned) {
        this.setMeta("banned", String.valueOf(banned));
    }
    
    /**
     * Checks whether this user is allowed to perform administrative actions on
     * this server.
     * 
     * @return Whether the user is an administrator.
     */
    public boolean isAdmin() {
        return Boolean.parseBoolean(this.getMeta("admin", "false"));
    }
    
    /**
     * Sets whether this user account is permitted to perform administrative
     * actions on this server. If this user is currently connected, a
     * {@link PacketAuthResult} must be sent to their client to notify it of the
     * change in administrative status.
     * 
     * @param admin Whether this user is permitted to perform administrative
     *            actions.
     */
    public void setAdmin(boolean admin) {
        this.setMeta("admin", String.valueOf(admin));
    }
    
    /**
     * Contructs a basic {@link PlayerInfo} class with information regarding
     * this user to be communicated with a client.
     * 
     * @return A class instance containing basic information about this user
     *         account.
     */
    public PlayerInfo toPlayerInfo() {
        return new PlayerInfo(this.name, this.isAdmin());
    }
    
    String getData() {
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
    
    /**
     * A listener which is notified whenever any metadata is changed on a
     * particular user account.
     *
     * @author Ben Thomas
     */
    public interface MetadataListener {
        
        /**
         * Notifies this listener that the specified metadata has been changed.
         * This is only called <strong>after</strong> the metadata has already
         * been updated and saved, and reversing a metadata change does not
         * prevent side effects of this change, such as disconnections.
         * 
         * @param name The name of the metadata value which has been changed.
         * @param value The value to which the metadata is now set.
         */
        public void onSet(String name, String value);
    }
    
}
