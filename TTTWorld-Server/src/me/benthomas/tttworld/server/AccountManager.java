package me.benthomas.tttworld.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Properties;

/**
 * A class which manages loading, unloading, storing, and searching user
 * accounts from a server database.
 *
 * @author Ben Thomas
 */
public class AccountManager {
    private static final int DEAD_CACHE_SIZE = 10;
    private static final String ACCOUNT_FILE = "accounts.db";
    
    private Properties accountFile;
    
    private HashMap<String, Account> cache = new HashMap<String, Account>();
    private Deque<String> cacheOut = new ArrayDeque<String>(AccountManager.DEAD_CACHE_SIZE);
    
    AccountManager() throws IOException {
        this.accountFile = new Properties();
        
        if (new File(AccountManager.ACCOUNT_FILE).exists()) {
            this.accountFile.load(new FileInputStream(AccountManager.ACCOUNT_FILE));
        } else {
            this.createAccount("admin", "P@ssw0rd", true);
            System.out.println("No existing account database! Added user 'admin' with password 'P@ssw0rd'!");
        }
    }
    
    /**
     * Gets the account which has the specified username. The username of the
     * returned user may not exactly match the provided username if the user has
     * more than one name.
     * <p>
     * If the user account is not currently loaded, it is loaded into memory. It
     * is not, however, set as sticky in the user cache; this can be done after
     * the user account is loaded using
     * {@link #setAccountSticky(String, boolean)}.
     * 
     * @param name The username of the user account to be found.
     * @return The user account instance with the given username.
     */
    public synchronized Account getAccount(String name) {
        if (this.cache.containsKey(name.toLowerCase())) {
            return this.cache.get(name.toLowerCase());
        } else if (this.accountFile.containsKey(name.toLowerCase())) {
            Account a = new Account(this, name, this.accountFile.get(name.toLowerCase()).toString());
            cache.put(a.getName(), a);
            setAccountSticky(a.getName(), false);
            
            return a;
        } else {
            return null;
        }
    }
    
    /**
     * Sets whether the user account with the given name is sticky in the user
     * cache. When a user account is sticky, it will not be unloaded from the
     * user cache.
     * <p>
     * If the cache does not contain the specified user,
     * 
     * @param name The name of the user for which the stickyness should be set.
     * @param sticky Whether the given user is sticky in the user cache.
     */
    public synchronized void setAccountSticky(String name, boolean sticky) {
        if (sticky && cacheOut.contains(name.toLowerCase())) {
            cacheOut.remove(name.toLowerCase());
        } else if (!sticky && cache.containsKey(name.toLowerCase()) && !cacheOut.contains(name.toLowerCase())) {
            this.cacheOut.addLast(name.toLowerCase());
            
            if (this.cacheOut.size() > AccountManager.DEAD_CACHE_SIZE) {
                this.cache.remove(this.cacheOut.removeFirst());
            }
        }
    }
    
    /**
     * Creates a new account with the specified properties.
     * 
     * @param name The name of the new account to be created.
     * @param password The password with which the account should be created.
     * @param admin Whether the new account should be marked as an
     *            administrator.
     * @return The created user account. If a user account already exists with
     *         the given name, {@code null} is returned.
     */
    public synchronized Account createAccount(String name, String password, boolean admin) {
        if (!this.accountFile.contains(name.toLowerCase())) {
            Account a = new Account(this, name);
            
            a.setPassword(password);
            a.setAdmin(admin);
            
            this.cache.put(name.toLowerCase(), a);
            this.cacheOut.addLast(name.toLowerCase());
            
            if (this.cacheOut.size() > AccountManager.DEAD_CACHE_SIZE) {
                this.cache.remove(this.cacheOut.removeFirst());
            }
            
            return a;
        } else {
            return null;
        }
    }
    
    synchronized void saveAccount(Account a) {
        accountFile.put(a.getName().toLowerCase(), a.getData());
        
        try {
            this.accountFile.store(new FileOutputStream(AccountManager.ACCOUNT_FILE), "Tic-Tac-Toe World Account Database");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
