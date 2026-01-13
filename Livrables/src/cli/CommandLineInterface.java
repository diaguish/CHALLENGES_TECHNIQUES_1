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
        
    this.MenuRenderer.displayWelcome();
    this.MenuRenderer.displayHelp();
     while (true) {
            System.out.print("sfm:" + context.pwd() + "> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String cmd = line.toLowerCase();

            if (cmd.equals("help")) {
                this.MenuRenderer.displayHelp();
            } else if (cmd.equals("pwd")) {
                System.out.println(context.pwd());
            } else if (cmd.equals("ls")) {
                String filesList = this.MenuRenderer.displayFiles(context.getCurrent());
                System.out.println(filesList);
            } else if (cmd.equals("create")) {
                System.out.print("Entrez le nom du fichier à créer: ");
                String filename = scanner.nextLine().trim();
                String result = fileService.createFile(context.getCurrent(), filename);
                System.out.println(result);
            } else if (cmd.equals("delete")) {
                System.out.print("Entrez le nom du fichier à supprimer: ");
                String filename = scanner.nextLine().trim();
                String result = fileService.deleteFile(context.getCurrent(), filename);
                System.out.println(result);
            } else if (cmd.equals("read")) {
                System.out.print("Entrez le nom du fichier à lire: ");
                String filename = scanner.nextLine().trim();
                String result = fileService.readFile(context.getCurrent(), filename);
                System.out.println(result);
            } else if (cmd.equals("exit")) {
                System.out.println("Au revoir.");
                break;
            } else {
                System.out.println("Commande inconnue. Tapez 'help'.");
            }
        }
    }
    }
