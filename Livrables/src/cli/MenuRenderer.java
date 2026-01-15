
package cli;
import application.FileService;
import java.nio.file.Path;
import java.sql.SQLException;
import application.WorkingContext;

/**
 * Singleton class to render menu and display messages to the Interface.
 */
public class MenuRenderer {
    private static MenuRenderer instance;
    private FileService fileService;

    /**
     * Private constructor for singleton pattern.
     * Initializes the FileService instance.
     * 
     * @throws SQLException if FileService initialization fails
     */
    private MenuRenderer() throws SQLException {
        this.fileService = FileService.getInstance();
    }

    /**
     * Gets the singleton instance of MenuRenderer.
     * 
     * @return the MenuRenderer instance
     * @throws SQLException if initialization fails
     */
    public static synchronized MenuRenderer getInstance() throws SQLException {
        if (instance == null) {
            instance = new MenuRenderer();
        }
        return instance;
    }

    /**
     * Displays the welcome message for the application.
     * 
     * @return the formatted welcome message
     */
    public String displayWelcome() {
        String welcome = "=== Secure File Manager (CLI) ===\n";
        welcome += "Tapez 'help' pour voir les commandes.\n";
        return welcome;
    }

    /**
     * Displays the help message listing all available commands.
     * 
     * @return the formatted help message
     */
    public String displayHelp() {
        String help = "Commandes disponibles :\n";
        help += "  help  - afficher cette aide\n";
        help += "  pwd   - afficher le répertoire courant\n";
        help += "  ls    - lister les fichiers dans le répertoire courant\n";
        help += "  create - créer un nouveau fichier\n";
        help += "  create_repo - créer un nouveau répertoire\n";
        help += "  delete - supprimer un fichier\n";
        help += "  update - mettre à jour le contenu d'un fichier\n";
        help += "  read  - lire le contenu d'un fichier\n";
        help += "  cd    - changer de répertoire\n";
        help += "  login - se connecter\n";
        help += "  register - créer un compte\n";
        help += "  logout - se déconnecter\n";
        help += "  exit  - quitter\n";
        return help;
    }

    /**
     * Displays the list of files in the current directory.
     * 
     * @param displayPath the formatted path of the current directory
     * @param currentDirectory the Path object of the current directory
     * @return the formatted string listing files in the directory
     */
    public String displayFiles(String displayPath ,Path currentDirectory ) {
        String ret = "Fichiers dans " + displayPath + ":\n";
        ret += fileService.listFiles(currentDirectory);
        return ret;
    }
}
