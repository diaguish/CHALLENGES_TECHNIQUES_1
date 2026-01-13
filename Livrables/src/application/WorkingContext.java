package application;

import domain.exception.FileAccessException;

import java.nio.file.Path;
import java.nio.file.Paths;
import infrastructures.filesystem.LocalFileRepository;


public class WorkingContext {

    private final Path root;
    private Path current;
    private LocalFileRepository fileRepository = new LocalFileRepository();

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

    public Path getCurrent() {
        return current;
    }
//Elle transforme ce que l’utilisateur tape en un chemin sûr,
//et empêche absolument de sortir du dossier autorisé (root).


    public String changeDirectory(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Le nom du répertoire ne peut pas être vide.";
        }
        if (input.equals("/")) {
            this.current = this.root;
            return "Changement de répertoire réussi.";
        }
        if (input.equals("..")) {
            if (this.current.equals(this.root)) {
                return "Vous êtes déjà au répertoire racine.";
            }
            this.current = this.current.getParent();
            return "Changement de répertoire réussi.";
        }
        final Path newPath = resolve(input);
        if (!fileRepository.isDirectory(newPath)) {
            return "Le chemin spécifié n'est pas un répertoire.";
        }
        this.current = newPath;
        return "Changement de répertoire réussi.";
    }
}
