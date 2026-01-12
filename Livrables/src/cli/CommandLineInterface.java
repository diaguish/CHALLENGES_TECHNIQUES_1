package cli;
public class CommandLineInterface {

    private final FileService fileService;

    public CommandLineInterface() {
        this.fileService = new FileService();
    }
    
    public void start() {
    MenuRenderer.displayWelcome();
        // boucle CLI à implémenter
    }
}