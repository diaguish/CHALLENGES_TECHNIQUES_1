package domain.model;
import java.io.File;

public interface Encrypt {
    String generateKey(String initialText) throws Exception;
    String encryptText(String value,String key) throws Exception;
    String decryptText(String value,String key) throws Exception;
    File encryptFile(File file, String key) throws Exception;
    File decryptFile(File file,String key) throws  Exception;
}