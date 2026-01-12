package domain.repository;

public class FileRepository {
    void create(String filename);
    void createRepository(String directoryName);
    void delete(String filename);
    void deleteRepository(String directoryName);
    String read(String filename);
}
