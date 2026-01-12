public class FileService {
    private final FileRepository repository;

    public FileService() {
        this.repository = new LocalFileRepository();
    }
    
    public void createFile(String filename) {
        try {
            repository.create(filename);
        } catch (FileAlreadyExistsException e) {
            throw new FileAccessException("Cannot create file: " + e.getMessage());
        }
    }
}