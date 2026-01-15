package cli;
import java.util.Scanner;
import application.FileService;
import java.sql.SQLException;
import application.WorkingContext;

public class CommandLineInterface {

    private WorkingContext context;
    private Scanner scanner;
    private MenuRenderer MenuRenderer;
    private FileService fileService;

    public CommandLineInterface() {
        try {
            this.context = new WorkingContext("root_app"); // define the root directory
            this.scanner = new Scanner(System.in);
            this.MenuRenderer = MenuRenderer.getInstance();
            this.fileService = FileService.getInstance();
            this.fileService.configureIntegrity(this.context.getRoot());

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void start() {
        
        System.out.println(this.MenuRenderer.displayWelcome());
        System.out.println(this.MenuRenderer.displayHelp());
        while (true) {
            System.out.print("sfm:" + context.pwd() + "> ");
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
                    display = fileService.updateFile(context.getCurrent(), filename, newContent);
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

                case "exit":
                    System.out.println("Au revoir.");
                    return; // ou break de ta boucle principale

                default:
                    display = "Commande inconnue. Tapez 'help'.";
            }


            if (!display.isEmpty()) {
                System.out.println(display);
            }
        }
    }
}
