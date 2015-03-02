package me.benthomas.tttworld.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Properties;

public class AccountManager {
    private static final int DEAD_CACHE_SIZE = 10;
    private static final String ACCOUNT_FILE = "accounts.db";
    
    private Properties accountFile;
    
    private HashMap<String, Account> cache = new HashMap<String, Account>();
    private Deque<String> cacheOut = new ArrayDeque<String>(AccountManager.DEAD_CACHE_SIZE);
    
    public AccountManager() throws IOException {
        this.accountFile = new Properties();
        
        if (new File(AccountManager.ACCOUNT_FILE).exists()) {
            this.accountFile.load(new FileInputStream(AccountManager.ACCOUNT_FILE));
        } else {
            this.createAccount("admin", "P@ssw0rd", true);
            System.out.println("No existing account database! Added user 'admin' with password 'P@ssw0rd'!");
        }
    }
    
    public Account getAccount(String name) {
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
    
    public void setAccountSticky(String name, boolean sticky) {
        if (sticky && cacheOut.contains(name)) {
            cacheOut.remove(name);
        } else if (!sticky && cache.containsKey(name) && !cacheOut.contains(name)) {
            if (this.cacheOut.size() > AccountManager.DEAD_CACHE_SIZE) {
                this.cache.remove(this.cacheOut.removeFirst());
            }
            
            this.cacheOut.addLast(name);
        }
    }
    
    public Account createAccount(String name, String password, boolean admin) {
        Account a = new Account(this, name);
        
        a.setPassword(password);
        a.setAdmin(admin);
        
        return a;
    }
    
    protected void saveAccount(Account a) {
        accountFile.put(a.getName().toLowerCase(), a.getData());
        
        try {
            this.accountFile.store(new FileOutputStream(AccountManager.ACCOUNT_FILE), "Tic-Tac-Toe World Account Database");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
