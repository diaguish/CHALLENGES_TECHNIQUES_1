package infrastructures.filesystem;

public class LocalFileRepository implements FileRepository {
    @Override
    public void create(String filename) {
        if(java.nio.file.Files.exists(java.nio.file.Paths.get(filename))) {
            throw new FileAlreadyExistsException("File already exists: " + filename);
        }

        try {
            java.nio.file.Files.createFile(java.nio.file.Paths.get(filename));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void delete(String filename) {
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get(filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filename));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String read(String filename) {
        if(!java.nio.file.Files.exists(java.nio.file.Paths.get(filename))) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        if(!java.nio.file.Files.isReadable(java.nio.file.Paths.get(filename))) {
            throw new FileNotReadableException("File not readable: " + filename);
        }

        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filename)));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
