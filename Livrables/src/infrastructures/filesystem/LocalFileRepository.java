package infrastructures.filesystem;
import domain.repository.FileRepository;

public class LocalFileRepository implements FileRepository {
@Override
public void create(String filename) {
// accès disque uniquement ici
}
}
