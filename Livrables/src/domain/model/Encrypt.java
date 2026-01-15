package domain.model;
import java.io.File;

/**
 * Interface for encryption and decryption operations.
 */
public interface Encrypt {
    /**
     * Generates an encryption key from the provided text.
     * 
     * @param initialText the initial text to generate key from
     * @return an array containing the generated key and salt
     * @throws Exception if key generation fails
     */
    String[] generateKey(String initialText) throws Exception;
    
    /**
     * Encrypts text using the provided key.
     * 
     * @param value the text to encrypt
     * @param key the encryption key
     * @return the encrypted text
     * @throws Exception if encryption fails
     */
    String encryptText(String value,String key) throws Exception;
    
    /**
     * Decrypts text using the provided key.
     * 
     * @param value the encrypted text
     * @param key the decryption key
     * @return the decrypted text
     * @throws Exception if decryption fails
     */
    String decryptText(String value,String key) throws Exception;
    
    /**
     * Encrypts a file using the provided key.
     * 
     * @param file the file to encrypt
     * @param key the encryption key
     * @return the encrypted file
     * @throws Exception if file encryption fails
     */
    File encryptFile(File file, String key) throws Exception;
    
    /**
     * Decrypts a file using the provided key.
     * 
     * @param file the encrypted file
     * @param key the decryption key
     * @return the decrypted file
     * @throws Exception if file decryption fails
     */
    File decryptFile(File file,String key) throws  Exception;
}