package infrastructures.security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.nio.file.*;
import domain.model.Encrypt;
import domain.exception.CryptoException;
import domain.exception.HashException;
import static java.lang.System.exit;

public class CryptoService implements Encrypt {
    private static class ValueUtils {
        public static String AESGCM_ALGO;
        public static int TAG_LENGTH_BIT;
        public static int IV_LENGTH_BYTE;
        public static int AES_KEY_BIT;
        public static int SALT_LENGTH_BYTE;

    }

    public CryptoService() throws CryptoException {
        try {
            String envPath = ".env";
            Properties props = new Properties();
            props.load(new FileInputStream(envPath));
            
            ValueUtils.AESGCM_ALGO = props.getProperty("AESGCM_ALGO");
            ValueUtils.TAG_LENGTH_BIT = Integer.parseInt(props.getProperty("TAG_LENGTH_BITS"));
            ValueUtils.IV_LENGTH_BYTE = Integer.parseInt(props.getProperty("IV_LENGTH_BYTES"));
            ValueUtils.AES_KEY_BIT = Integer.parseInt(props.getProperty("AES_KEY_BITS"));
            ValueUtils.SALT_LENGTH_BYTE = Integer.parseInt(props.getProperty("SALT_LENGTH_BYTES"));
        } catch (IOException e) {
            throw new CryptoException("Error loading configuration from .env file: " + e.getMessage());
        }
    }

    /**
     * Generate a random nonce of specified number of bytes
     * @param numBytes Number of bytes of the nonce
     * @return byte array representing the nonce
     */
    public static byte[] getRandomNonce(int numBytes) {
        byte[] nonce = new byte[numBytes];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    /**
     * Generate AES key from a given password and salt
     * @param password The password to derive the key from
     * @param salt The salt to use in key derivation
     * @return SecretKey derived from the password and salt
     * @throws CryptoException
     */
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt) throws CryptoException {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, ValueUtils.AES_KEY_BIT);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return secret;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoException("Error generating AES key from password: " + e.getMessage());
        }
    }

    /**
     * Convert SecretKey to a Base64 encoded string
     * @param secretKey The SecretKey to convert
     * @return Base64 encoded string representation of the SecretKey
     * @throws CryptoException
     */
    public static String convertSecretKeyToString(SecretKey secretKey) throws CryptoException {
        try {
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new CryptoException("Error converting SecretKey to string: " + e.getMessage());
        }
    }

    /**
     * Convert a Base64 encoded string back to a SecretKey
     * @param key The Base64 encoded string representation of the SecretKey
     * @return The SecretKey
     * @throws CryptoException
     */
    public static SecretKey convertStringToSecretKey(String key) throws CryptoException {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } catch (Exception e) {
            throw new CryptoException("Error converting string to SecretKey: " + e.getMessage());
        }
    }

    /**
     * Encrypt the given text using AES-GCM
     * @param value The plaintext to encrypt
     * @param key The Base64 encoded string representation of the SecretKey
     * @return Base64 encoded string of the encrypted text with IV prepended
     * @throws CryptoException
     */
    public String encryptText(String value, String key) throws CryptoException {
        try {
            byte[] plainText = value.getBytes();

            byte[] iv = getRandomNonce(ValueUtils.IV_LENGTH_BYTE);
            SecretKey secretkey = convertStringToSecretKey(key);

            Cipher cipher = Cipher.getInstance(ValueUtils.AESGCM_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, secretkey, new GCMParameterSpec(ValueUtils.TAG_LENGTH_BIT, iv));

            byte[] encryptedText = cipher.doFinal(plainText);
            byte[] encryptedTextWithIv = ByteBuffer.allocate(iv.length + encryptedText.length)
                    .put(iv)
                    .put(encryptedText)
                    .array();
            return Base64.getEncoder().encodeToString(encryptedTextWithIv);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Error encrypting text: " + e.getMessage());
        }
    }

    /**
     * Decrypt the given encrypted text using AES-GCM
     * @param value The Base64 encoded string of the encrypted text with IV prepended
     * @param key The Base64 encoded string representation of the SecretKey
     * @return The decrypted plaintext
     * @throws CryptoException
     */
    public String decryptText(String value, String key) throws CryptoException, HashException {
        try {
            byte[] decode = null;
            try{
                decode = Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
            } catch(IllegalArgumentException e){
                throw new HashException("Invalid Base64 input for decryption: " + e.getMessage());
            }
            ByteBuffer bufferEncryptedText = ByteBuffer.wrap(decode);

            byte[] iv = new byte[ValueUtils.IV_LENGTH_BYTE];
            bufferEncryptedText.get(iv);

            byte[] cipherText = new byte[bufferEncryptedText.remaining()];
            bufferEncryptedText.get(cipherText);

            Cipher cipher = Cipher.getInstance(ValueUtils.AESGCM_ALGO);
            SecretKey secretKey = convertStringToSecretKey(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(ValueUtils.TAG_LENGTH_BIT, iv));
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (CryptoException e) {
            throw e;
        } catch (HashException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Error decrypting text: " + e.getMessage());
        }
    }

    /**
     * Encrypt a file using AES-GCM
     * @param file The file to encrypt
     * @param key The Base64 encoded string representation of the SecretKey
     * @return The encrypted file
     * @throws CryptoException
     */
    public File encryptFile(File file, String key) throws CryptoException {
        try {
            String pathOutput = file.getAbsolutePath() + ".encrypted";
            Path path = Paths.get(pathOutput);
            SecretKey secretKey = convertStringToSecretKey(key);
            byte[] iv = getRandomNonce(ValueUtils.IV_LENGTH_BYTE);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(ValueUtils.TAG_LENGTH_BIT, iv));

            FileInputStream inputStream = new FileInputStream(file);
            byte[] inputBytes = new byte[(int) file.length()];
            inputStream.read(inputBytes);
            byte[] outputBytes = cipher.doFinal(inputBytes);

            File fileEncryptOut = new File(path.toUri());
            FileOutputStream outputStream = new FileOutputStream(fileEncryptOut);
            outputStream.write(iv);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();
            return fileEncryptOut;
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Error encrypting file: " + e.getMessage());
        }
    }

    /**
     * Decrypt a file using AES-GCM
     * @param file The file to decrypt
     * @param key The Base64 encoded string representation of the SecretKey
     * @return The decrypted file
     * @throws CryptoException
     */
    public File decryptFile(File file, String key) throws CryptoException {
        try {
            String pathOutput = file.getAbsolutePath() + ".decrypted";
            Path path = Paths.get(pathOutput);
            SecretKey secretKey = convertStringToSecretKey(key);
            FileInputStream inputStream = new FileInputStream(file);
            byte[] iv = new byte[ValueUtils.IV_LENGTH_BYTE];
            inputStream.read(iv);
            byte[] inputBytes = new byte[(int) (file.length() - iv.length)];
            inputStream.read(inputBytes);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(ValueUtils.TAG_LENGTH_BIT, iv));
            byte[] outputBytes = cipher.doFinal(inputBytes);

            File fileEncryptOut = new File(path.toUri());
            FileOutputStream outputStream = new FileOutputStream(fileEncryptOut);
            outputStream.write(outputBytes);
            inputStream.close();
            outputStream.close();
            return fileEncryptOut;
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Error decrypting file: " + e.getMessage());
        }
    }

    /**
     * Generate a new AES key
     * @param initialValue The text to generate a key from
     * @return Base64 encoded string representation of the generated SecretKey
     * @throws CryptoException
     */
    public String[] generateKey(String initialValue) throws CryptoException {
        try {
            byte[] salt = getRandomNonce(ValueUtils.SALT_LENGTH_BYTE);
            SecretKey secretKey = getAESKeyFromPassword(initialValue.toCharArray(), salt);
            return new String[] {convertSecretKeyToString(secretKey), Base64.getEncoder().encodeToString(salt)};
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Error generating key: " + e.getMessage());
        }
    }

    public String[] generateKey(String initialValue, String saltStr) throws CryptoException {
        try {
            byte[] salt = Base64.getDecoder().decode(saltStr);
            SecretKey secretKey = getAESKeyFromPassword(initialValue.toCharArray(), salt);
            return new String[] {convertSecretKeyToString(secretKey), saltStr};
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Error generating key with provided salt: " + e.getMessage());
        }
    }

    /**
     * Génère un salt aléatoire (base64)
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 128 bits
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash un mot de passe avec un salt (SHA-256)
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
