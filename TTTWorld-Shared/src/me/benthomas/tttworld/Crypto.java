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
import javax.xml.bind.DatatypeConverter;

/**
 * Contains helper functions for running cryptographic operations and other
 * closely-related functions.
 *
 * @author Ben Thomas
 */
public class Crypto {
    
    /**
     * Calculates the SHA-1 hash of a given byte array.
     * 
     * @param full The byte array for which the SHA-1 hash should be calculated.
     * @return The SHA-1 hash, in byte array format, of the given data.
     */
    public static byte[] calculateSHA1(byte[] full) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(full);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Displays a SHA-1 hash in the form of a human readable hex fingerprint.
     * Used to display the fingerprint of a public key to the end user for
     * verification.
     * 
     * @param fingerprint The fingerprint which should be formatted.
     * @return The human-readable, formatted string corresponding to the given
     *         fingerprint.
     */
    public static String formatFingerprint(byte[] fingerprint) {
        try (Formatter f = new Formatter()) {
            f.format("%02x", fingerprint[0]);
            
            for (int i = 1; i < fingerprint.length; i++) {
                f.format(":%02x", fingerprint[i]);
            }
            
            return f.toString();
        }
    }
    
    /**
     * Decrypts the given encrypted data using AES-128 with the given key and
     * IV.
     * 
     * @param encrypted The byte array containing the encrypted data.
     * @param start The index at which the encrypted data starts in the byte
     *            array.
     * @param key The key with which to decrypt the data.
     * @param iv The IV with which to decrypt the data.
     * @return The decrypted data in a byte array.
     * 
     * @throws CryptoException An error occurred during a cryptographic
     *             operation.
     */
    public static byte[] decryptSymmetric(byte[] encrypted, int start, byte[] key, byte[] iv) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key k = new SecretKeySpec(key, "AES");
            IvParameterSpec i = new IvParameterSpec(iv);
            
            c.init(Cipher.DECRYPT_MODE, k, i);
            
            return c.doFinal(encrypted, start, encrypted.length - start);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException(e);
        }
    }
    
    /**
     * Encrypts the given raw data using AES-128 with the given key and IV.
     * 
     * @param original The data which should be encrypted.
     * @param key The key with which to encrypt the data.
     * @param iv The IV with which to encrypt the data.
     * @return The data in an encrypted format as a byte array.
     * 
     * @throws CryptoException An error occurred during a cryptographic
     *             operation.
     */
    public static byte[] encryptSymmetric(byte[] original, byte[] key, byte[] iv) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key k = new SecretKeySpec(key, "AES");
            IvParameterSpec i = new IvParameterSpec(iv);
            
            c.init(Cipher.ENCRYPT_MODE, k, i);
            
            return c.doFinal(original);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException(e);
        }
    }
    
    /**
     * Decrypts the given data using RSA with the given private key.
     * 
     * @param encrypted The encrypted data to be decrypted.
     * @param k The private key with which the data should be decrypted.
     * @return The decrypted data in a byte array.
     * 
     * @throws CryptoException An error occurred during a cryptographic
     *             operation.
     */
    public static byte[] decryptAsymmetric(byte[] encrypted, PrivateKey k) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance("RSA");
            
            c.init(Cipher.DECRYPT_MODE, k);
            
            return c.doFinal(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException(e);
        }
    }
    
    /**
     * Encrypts the given data using RSA with the given public key.
     * 
     * @param original The original data that will be encrypred.
     * @param k The public key with which to encrypt the data.
     * @return An RSA-encrypted representation of the input data in a byte
     *         array.
     * 
     * @throws CryptoException An error occurred during a cryptographic
     *             operation.
     */
    public static byte[] encryptAsymmetric(byte[] original, PublicKey k) throws CryptoException {
        try {
            Cipher c = Cipher.getInstance("RSA");
            
            c.init(Cipher.ENCRYPT_MODE, k);
            
            return c.doFinal(original);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException(e);
        }
    }
    
    /**
     * Encodes the given byte array into base64 for putting into a JSON packet.
     * 
     * @param b The byte array that should be encoded.
     * @return The base64 encoded representation of the given byte array.
     */
    public static String encodeToBase64(byte[] b) {
        return DatatypeConverter.printBase64Binary(b);
    }
    
    /**
     * Decodes the given base64 representation found in a JSON packet into its
     * original byte array.
     * 
     * @param s The base64 encoded string to be decoded.
     * @return A byte array containing the decoded information.
     */
    public static byte[] decodeFromBase64(String s) {
        return DatatypeConverter.parseBase64Binary(s);
    }
    
    private Crypto() {
    }
    
    /**
     * Represents an exception that occurred while performing cryptographic
     * operations to encrypt/decrypt data. To get the original exception, use
     * {@link CryptoException#getCause()}.
     *
     * @author Ben Thomas
     */
    public static class CryptoException extends Exception {
        private static final long serialVersionUID = 1L;
        
        private CryptoException(Throwable cause) {
            super(cause);
        }
    }
}
