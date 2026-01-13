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
        /**
         * Initialize the working context with a root directory. Sets the current directory to root.
         * rootDirectory: String - the path of the root directory
         */
        this.root = Paths.get(rootDirectory).toAbsolutePath().normalize();
        this.current = root;
    }

    public String pwd() {
        /**
         * Get the current working directory relative to the root.
         * return the relative path as a string
         */
        Path relative = root.relativize(current);
        return relative.toString().isEmpty() ? "/" : "/" + relative;
    }

    public Path resolve(String input) {
        /**
         * Resolve the input path against the current directory, ensuring it stays within the root.
         * input: String - the input path to resolve
         * return the resolved Path
         * throws FileAccessException if the resolved path is outside the root
         */
        Path resolved = current.resolve(input).normalize();
        if (!resolved.startsWith(root)) {
            throw new FileAccessException("Sortie du répertoire autorisé interdite.");
        }
        return resolved;
    }

    public Path getCurrent() {
        /**
        * Get the current working directory as a Path.
        * return the current Path
        */
        return current;
    }

    public String changeDirectory(String input) {
        /**
        * Change the current working directory based on the input.
        * input: String - the input path to change to
        * return success or error message
        */
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
        try {
            if (!fileRepository.isDirectory(newPath)) {
                return "Le répertoire spécifié n'existe pas.";
            }
            this.current = newPath;
            return "Changement de répertoire réussi.";
        } catch (IllegalArgumentException e) {
            return "Chemin invalide: " + e.getMessage();
        }
    }
        public Path getRoot() {
            return root;
        }
}
