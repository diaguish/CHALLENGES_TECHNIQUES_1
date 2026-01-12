package domain.model;

public class SecureFile {
    private final String name;
    private final String path;
    public SecureFile(String name, String path) {
        this.name = name;
        this.path = path;
    }
    public String getName() {
        return name;
    }
}

