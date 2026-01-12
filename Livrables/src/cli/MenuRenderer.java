
package cli;
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
}
