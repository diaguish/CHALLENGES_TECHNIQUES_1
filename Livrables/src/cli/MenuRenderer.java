
package cli;
import application.FileService;
import java.nio.file.Path;
public class MenuRenderer {

    public static void displayWelcome() {
        System.out.println("=== Secure File Manager (CLI) ===");
        System.out.println("Tapez 'help' pour voir les commandes.");
    }

    public static void displayHelp() {
        System.out.println("Commandes disponibles :");
        System.out.println("  help  - afficher cette aide");
        System.out.println("  pwd   - afficher le répertoire courant");
        System.out.println("  exit  - quitter");
    }

    public static String displayFiles(Path currentDirectory) {
        /**
        Display the list of files in the current directory.
        currentDirectory: String - the path of the current directory
        return the formatted string of files
        */
        String ret = "Fichiers dans " + currentDirectory + ":\n";
        ret += FileService.listFiles(currentDirectory);
        return ret;
    
    }
}

