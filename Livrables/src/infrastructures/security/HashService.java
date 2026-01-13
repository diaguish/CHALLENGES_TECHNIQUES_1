package infrastructures.security;
import domain.exception.UnknowException;

import java.io.IOException;
import java.nio.file.Files; // permet de lire le contenu d'un fichier
import java.nio.file.Path; // représente un chemin de fichier
import java.security.MessageDigest; // pour le calcul des hash
import java.security.NoSuchAlgorithmException; // gère les exceptions si l'algorithme de hash n'est pas disponible


public class HashService {
    /**
     * Calcule le hash SHA-256 d'un fichier et le retourne en hexadécimal.
     */
    public String sha256(Path file) {
        try {
            byte[] content = Files.readAllBytes(file);
            return sha256(content);
        } catch (IOException e) {
            throw new UnknowException("Impossible de lire le fichier pour calculer le hash : " + file);
        }
    }

    /**
     * Calcule le hash SHA-256 d'un tableau de bytes et le retourne en hexadécimal.
     */
    public String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new UnknowException("SHA-256 indisponible sur cette JVM.");
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
