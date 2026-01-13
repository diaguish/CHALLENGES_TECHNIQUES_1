import infrastructures.security.HashService;
import infrastructures.security.IntegrityStore;

import java.nio.file.Path;

public class HashDemo {
    public static void main(String[] args) {
        HashService hs = new HashService();

        Path root = Path.of("."); // dossier courant = Livrables
        IntegrityStore store = new IntegrityStore(root);

        Path file = Path.of("src/test.txt");

        String hashNow = hs.sha256(file);
        System.out.println("hashNow = " + hashNow);

        store.saveHash(file, hashNow);
        System.out.println("saved in " + store.getIntegrityDir());

        String hashStored = store.loadHash(file);
        System.out.println("hashStored = " + hashStored);

        System.out.println("integrity OK ? " + hashNow.equals(hashStored));
    }
}
