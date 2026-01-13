package cli;
import java.util.Scanner;
import application.WorkingContext;
import application.FileService;

public class CommandLineInterface {

    private final WorkingContext context;
    private final Scanner scanner;
    private MenuRenderer MenuRenderer;
    private FileService fileService;

    public CommandLineInterface() {
        this.context = new WorkingContext("root_app"); // root autorisé (à ajuster si besoin)
        this.scanner = new Scanner(System.in);
        this.MenuRenderer = MenuRenderer.getInstance();
        this.fileService = FileService.getInstance();
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
                    display = MenuRenderer.displayFiles(context.getCurrent());
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

                case "cd":
                    System.out.print("Entrez le chemin du répertoire: ");
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
