package domain.repository;

public interface FileRepository {
    void create(String filename);
    void createRepository(String directoryName);
    void delete(String filename);
    void deleteRepository(String directoryName);
    String read(String filename);
}
