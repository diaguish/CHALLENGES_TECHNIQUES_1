package cli;
import java.util.Scanner;
import application.FileService;
import application.WorkingContext;

public class CommandLineInterface {

    private final FileService fileService;
    private final WorkingContext context;
    private final Scanner scanner;

    public CommandLineInterface() {
        this.fileService = new FileService();
        this.context = new WorkingContext("root_app"); // root autorisé (à ajuster si besoin)
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        
    MenuRenderer.displayWelcome();
    MenuRenderer.displayHelp();
     while (true) {
            System.out.print("sfm:" + context.pwd() + "> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String cmd = line.toLowerCase();

            if (cmd.equals("help")) {
                MenuRenderer.displayHelp();
            } else if (cmd.equals("pwd")) {
                System.out.println(context.pwd());
            }else if (cmd.equals("ls")) {
                String filesList = MenuRenderer.displayFiles(context.getCurrent());
                System.out.println(filesList);
            } else if (cmd.equals("exit")) {
                System.out.println("Au revoir.");
                break;
            } else {
                System.out.println("Commande inconnue. Tapez 'help'.");
            }
        }
    }
    }
