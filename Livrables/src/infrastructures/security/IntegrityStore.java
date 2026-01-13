package infrastructures.security;
import domain.exception.UnknowException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IntegrityStore {
    private final Path integrityDir;

    public IntegrityStore(Path rootDir) {
        this.integrityDir = rootDir.resolve(".integrity").toAbsolutePath().normalize();
        try {
            Files.createDirectories(integrityDir);
        } catch (IOException e) {
            throw new UnknowException("Impossible de créer le dossier d'intégrité : " + integrityDir);
        }
    }

    public void saveHash(Path file, String hashHex) {
        Path hashFile = hashPathFor(file);
        try {
            Files.writeString(
                    hashFile,
                    hashHex + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new UnknowException("Impossible d'écrire l'empreinte pour : " + file);
        }
    }

    public String loadHash(Path file) {
        Path hashFile = hashPathFor(file);
        if (!Files.exists(hashFile)) {
            return null;
        }
        try {
            return Files.readString(hashFile).trim();
        } catch (IOException e) {
            throw new UnknowException("Impossible de lire l'empreinte pour : " + file);
        }
    }

    public void deleteHash(Path file) {
        Path hashFile = hashPathFor(file);
        try {
            Files.deleteIfExists(hashFile);
        } catch (IOException e) {
            throw new UnknowException("Impossible de supprimer l'empreinte pour : " + file);
        }
    }

    private Path hashPathFor(Path file) {
        // Simple : on utilise juste le nom du fichier (pas le chemin complet)
        String name = file.getFileName().toString();
        return integrityDir.resolve(name + ".sha256");
    }

    public Path getIntegrityDir() {
        return integrityDir;
    }
}
