package application;

import domain.exception.FileAccessException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WorkingContext {

    private final Path root;
    private Path current;

    public WorkingContext(String rootDirectory) {
        this.root = Paths.get(rootDirectory).toAbsolutePath().normalize();
        this.current = root;
    }

    public String pwd() {
        Path relative = root.relativize(current);
        return relative.toString().isEmpty() ? "/" : "/" + relative;
    }

    public Path resolve(String input) {
        Path resolved = current.resolve(input).normalize();
        if (!resolved.startsWith(root)) {
            throw new FileAccessException("Sortie du répertoire autorisé interdite.");
        }
        return resolved;
    }
//Elle transforme ce que l’utilisateur tape en un chemin sûr,
//et empêche absolument de sortir du dossier autorisé (root).


    public void moveTo(Path newPath) {
        this.current = newPath;
    }
}
