public class FileService {
    private final FileRepository repository;

    public FileService() {
        this.repository = new LocalFileRepository();
    }
    
    public void createFile(String filename) {
    // logique métier à implémenter
    }
}