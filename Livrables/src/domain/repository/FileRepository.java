package domain.repository;

public interface FileRepository {
    void create(String filename);
    void delete(String filename);
    String read(String filename);
}
