import cli.CommandLineInterface;

/**
 * Entry point of the Secure File Manager application.
 */
public class Main {
    /**
     * Main entry point.
     * Initializes and starts the command line interface.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface();
        cli.start();
    }
}