package me.benthomas.tttworld;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Formatter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    
    public static byte[] calculateSHA1(byte[] full) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(full);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String formatFingerprint(byte[] fingerprint) {
        try (Formatter f = new Formatter()) {
            f.format("%02x", fingerprint[0]);
            
            for (int i = 1; i < fingerprint.length; i++) {
                f.format(":%02x", fingerprint[i]);
            }
            
            return f.toString();
        }
    }
    
    public static byte[] decryptSymmetric(byte[] encrypted, int start, byte[] key, byte[] iv) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key k = new SecretKeySpec(key, "AES");
            IvParameterSpec i = new IvParameterSpec(iv);
            
            c.init(Cipher.DECRYPT_MODE, k, i);
            
            return c.doFinal(encrypted, start, encrypted.length - start);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static byte[] encryptSymmetric(byte[] original, byte[] key, byte[] iv) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key k = new SecretKeySpec(key, "AES");
            IvParameterSpec i = new IvParameterSpec(iv);
            
            c.init(Cipher.ENCRYPT_MODE, k, i);
            
            return c.doFinal(original);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static byte[] decryptAsymmetric(byte[] encrypted, PrivateKey k) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        try {
            Cipher c = Cipher.getInstance("RSA");
            
            c.init(Cipher.DECRYPT_MODE, k);
            
            return c.doFinal(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static byte[] encryptAsymmetric(byte[] original, PublicKey k) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        try {
            Cipher c = Cipher.getInstance("RSA");
            
            c.init(Cipher.ENCRYPT_MODE, k);
            
            return c.doFinal(original);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Crypto() {
    }
}
