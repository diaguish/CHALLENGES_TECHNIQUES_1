package domain.model;

/**
 * Represents a secure file with name and path information.
 */
public class SecureFile {
    private final String name;
    private final String path;
    
    /**
     * Constructs a SecureFile with the given name and path.
     * 
     * @param name the name of the file
     * @param path the path of the file
     */
    public SecureFile(String name, String path) {
        this.name = name;
        this.path = path;
    }
    
    /**
     * Gets the name of the file.
     * 
     * @return the file name
     */
    public String getName() {
        return name;
    }
}

