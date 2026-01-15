package cli;
import java.util.Scanner;
import application.FileService;
import java.sql.SQLException;
import application.WorkingContext;
import application.UserService;

public class CommandLineInterface {

    private WorkingContext context;
    private Scanner scanner;
    private MenuRenderer MenuRenderer;
    private FileService fileService;
    private UserService userService;

    public CommandLineInterface() {
        try {
            this.context = WorkingContext.getInstance("root_app");
            this.scanner = new Scanner(System.in);
            this.MenuRenderer = MenuRenderer.getInstance();
            this.fileService = FileService.getInstance();
            this.fileService.configureIntegrity(this.context.getRoot());
            this.userService = UserService.getInstance();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void start() {
        // Check authentication first
        if (!ensureAuthentication()) {
            System.out.println("Impossible de continuer sans authentification.");
            return;
        }
        
        System.out.println(this.MenuRenderer.displayWelcome());
        System.out.println(this.MenuRenderer.displayHelp());
        while (true) {
            System.out.print("sfm:" + context.pwd() + "[" + userService.getCurrentUser() + "]> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String cmd = line.toLowerCase();
            String display = "";

            switch (cmd) {
                case "help":
                    display = MenuRenderer.displayHelp();
                    break;

                case "pwd":
                    display = "Répertoire courant: " + context.pwd();
                    break;

                case "ls":
                        String pathToDisplay = context.displayPath(context.getCurrent());
                        display = MenuRenderer.displayFiles(pathToDisplay, context.getCurrent());
                        break;

                case "create":
                    System.out.print("Entrez le nom du fichier à créer: ");
                    display = fileService.createFile(context.getCurrent(), scanner.nextLine().trim());
                    break;

                case "delete":
                    System.out.print("Entrez le nom du fichier à supprimer: ");
                    display = fileService.deleteFile(context.getCurrent(), scanner.nextLine().trim());
                    break;

                case "read":
                    System.out.print("Entrez le nom du fichier à lire: ");
                    display = fileService.readFile(context.getCurrent(), scanner.nextLine().trim());
                    break;

                case "create_repo":
                    System.out.print("Entrez le nom du répertoire à créer: ");
                    String dirName = scanner.nextLine().trim();
                    display = fileService.createRepository(context.getCurrent(), dirName);
                    break;

                case "update":
                    System.out.print("Entrez le nom du fichier à mettre à jour: ");
                    String filename = scanner.nextLine().trim();
                    System.out.print("Entrez le nouveau contenu du fichier: ");
                    String newContent = scanner.nextLine();
                    fileService.updateFile(context.getCurrent(), filename, newContent);
                    display = "Contenu du fichier mis à jour.";
                    break;

                case "cd":
                    System.out.print("Entrez le chemin du répertoire (.. pour remonter): ");
                    String path = scanner.nextLine().trim();
                    String result = context.changeDirectory(path);
                    if (result.isEmpty()) {
                        display = "Changement de répertoire réussi.";
                    } else {
                        display = result;
                    }
                    break;

                case "login":
                    display = handleLogin();
                    break;

                case "register":
                    display = handleRegister();
                    break;

                case "logout":
                    display = userService.logout();
                    if (!ensureAuthentication()) {
                        System.out.println("Vous devez vous reconnecter pour continuer.");
                        return;
                    }
                    break;

                case "exit":
                    System.out.println("Au revoir.");
                    return;

                default:
                    display = "Commande inconnue. Tapez 'help'.";
            }


            if (!display.isEmpty()) {
                System.out.println(display);
            }
        }
    }

    /**
     * Ensures that a user is authenticated before allowing access to the system
     * @return true if authentication is successful, false otherwise
     */
    private boolean ensureAuthentication() {
        while (!userService.isLoggedIn()) {
            System.out.println("\n=== Authentification requise ===");
            System.out.println("Tapez 'login' pour vous connecter, 'register' pour créer un compte, ou 'exit' pour quitter.");
            System.out.print("sfm:auth> ");
            
            String command = scanner.nextLine().trim().toLowerCase();
            
            switch (command) {
                case "login":
                    String loginResult = handleLogin();
                    System.out.println(loginResult);
                    if (loginResult.startsWith("Connexion réussie")) {
                        return true;
                    }
                    break;
                case "register":
                    String registerResult = handleRegister();
                    System.out.println(registerResult);
                    if (registerResult.startsWith("Utilisateur")) {
                        return true; // L'utilisateur est déjà connecté après l'inscription
                    }
                    break;
                case "exit":
                    return false;
                default:
                    System.out.println("Commande inconnue. Tapez 'login', 'register' ou 'exit'.");
            }
        }
        return true;
    }

    /**
     * Handles user login
     * @return login result message
     */
    private String handleLogin() {
        System.out.print("Nom d'utilisateur: ");
        String username = scanner.nextLine().trim();
        System.out.print("Mot de passe: ");
        String password = scanner.nextLine().trim();
        
        return userService.login(username, password);
    }

    /**
     * Handles user registration
     * @return registration result message
     */
    private String handleRegister() {
        System.out.print("Nom d'utilisateur souhaité: ");
        String username = scanner.nextLine().trim();
        System.out.print("Mot de passe: ");
        String password = scanner.nextLine().trim();
        System.out.print("Confirmer le mot de passe: ");
        String confirmPassword = scanner.nextLine().trim();
        
        if (!password.equals(confirmPassword)) {
            return "Erreur: Les mots de passe ne correspondent pas.";
        }
        
        String registerResult = userService.register(username, password);
        if (registerResult.startsWith("Utilisateur")) {
            // Auto-login after successful registration
            String loginResult = userService.login(username, password);
            if (loginResult.startsWith("Connexion réussie")) {
                return registerResult + " Vous êtes maintenant connecté.";
            } else {
                return registerResult + " Cependant, la connexion automatique a échoué. Veuillez vous connecter manuellement.";
            }
        }
        return registerResult;
    }
}
