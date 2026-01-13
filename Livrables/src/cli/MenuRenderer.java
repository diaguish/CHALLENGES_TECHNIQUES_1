
package cli;
import application.FileService;
import java.nio.file.Path;
public class MenuRenderer {
    /**
     * Singleton class to create message to the Interface
     */
    private static MenuRenderer instance;
    private FileService fileService;

    private MenuRenderer() {
        this.fileService = FileService.getInstance();
    }

    public static synchronized MenuRenderer getInstance() {
        if (instance == null) {
            instance = new MenuRenderer();
        }
        return instance;
    }

    public String displayWelcome() {
        /**
         * Display the welcome message.
         * return the welcome string
         */
        String welcome = "=== Secure File Manager (CLI) ===\n";
        welcome += "Tapez 'help' pour voir les commandes.\n";
        return welcome;
    }

    public String displayHelp() {
        /**
         * Display the help message.
         * return the help string
         */
        String help = "Commandes disponibles :\n";
        help += "  help  - afficher cette aide\n";
        help += "  pwd   - afficher le répertoire courant\n";
        help += "  ls    - lister les fichiers dans le répertoire courant\n";
        help += "  create - créer un nouveau fichier\n";
        help += "  create_repo - créer un nouveau répertoire\n";
        help += "  delete - supprimer un fichier\n";
        help += "  read  - lire le contenu d'un fichier\n";
        help += "  exit  - quitter\n";
        return help;
    }

    public String displayFiles(Path currentDirectory) {
        /**
        Display the list of files in the current directory.
        currentDirectory: String - the path of the current directory
        return the formatted string of files
        */
        String ret = "Fichiers dans " + currentDirectory + ":\n";
        ret += fileService.listFiles(currentDirectory);
        return ret;
    }
}
