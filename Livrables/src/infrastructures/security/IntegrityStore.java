package infrastructures.security;

import domain.exception.UnknowException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 * Stockage d'intégrité (IT2) :
 * - Tous les fichiers d'intégrité sont dans .integrity/ (pas de miroir).
 * - Un fichier JSON par fichier surveillé.
 * - On ajoute une entrée à chaque sauvegarde (historique), sans écraser.
 * - Vérification possible via la dernière entrée (hash + size).
 */
public class IntegrityStore {

    private final Path integrityDir;
    private final Path rootDir;


    public IntegrityStore(Path rootDir) {
        this.rootDir = rootDir.toAbsolutePath().normalize();
        this.integrityDir = this.rootDir.getParent().resolve(".integrity").toAbsolutePath().normalize();

        try {
            Files.createDirectories(integrityDir);
        } catch (IOException e) {
            throw new UnknowException("Impossible de créer le dossier d'intégrité : " + integrityDir);
        }
    }

    /**
     * Ajoute une nouvelle entrée (hash + timestamp + size) dans le JSON du fichier.
     * Ne supprime pas l'historique.
     */
    public void appendEntry(Path file, String hashHex, long size) {
        Path integrityFile = integrityPathFor(file);

        String entryJson = "    { \"hash\": " + jsonString(hashHex)
                + ", \"timestamp\": " + jsonString(LocalDateTime.now().toString())
                + ", \"size\": " + size + " }";

        try {
            if (!Files.exists(integrityFile)) {
                // Crée un JSON initial
                String initial = "{\n" +
                        "  \"path\": " + jsonString(normalizedKey(file)) + ",\n" +
                        "  \"entries\": [\n" +
                        entryJson + "\n" +
                        "  ]\n" +
                        "}\n";

                Files.writeString(
                        integrityFile,
                        initial,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                );
                return;
            }

            // Ajoute une entrée sans écraser : insertion avant le dernier "]"
            String content = Files.readString(integrityFile);
            int idx = content.lastIndexOf("]");
            if (idx == -1) {
                throw new UnknowException("Format d'intégrité invalide : " + integrityFile);
            }

            // Si déjà une entrée, on ajoute une virgule
            boolean hasAtLeastOneEntry = content.contains("\"entries\": [") && content.contains("{");

            String insertion = (hasAtLeastOneEntry ? ",\n" : "\n") + entryJson;

            String updated = content.substring(0, idx) + insertion + "\n  " + content.substring(idx);

            Files.writeString(
                    integrityFile,
                    updated,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

        } catch (IOException e) {
            throw new UnknowException("Impossible d'écrire l'intégrité pour : " + file);
        }
    }

    /**
     * Retourne la dernière entrée (hash + size) pour la vérification.
     * null si aucun fichier d'intégrité n'existe.
     */
    public IntegrityEntry loadLastEntry(Path file) {
        Path integrityFile = integrityPathFor(file);
        if (!Files.exists(integrityFile)) {
            return null;
        }

        try {
            String content = Files.readString(integrityFile);

            int lastHashIdx = content.lastIndexOf("\"hash\"");
            int lastSizeIdx = content.lastIndexOf("\"size\"");

            if (lastHashIdx == -1 || lastSizeIdx == -1) {
                throw new UnknowException("Format d'intégrité invalide : " + integrityFile);
            }

            String hash = extractJsonStringValue(content, lastHashIdx);
            long size = extractJsonLongValue(content, lastSizeIdx);

            return new IntegrityEntry(hash, size);

        } catch (IOException e) {
            throw new UnknowException("Impossible de lire l'intégrité pour : " + file);
        }
    }

    public void deleteIntegrity(Path file) {
        Path integrityFile = integrityPathFor(file);
        try {
            Files.deleteIfExists(integrityFile);
        } catch (IOException e) {
            throw new UnknowException("Impossible de supprimer l'intégrité pour : " + file);
        }
    }

    /**
     * Calcule le chemin du fichier d'intégrité (tous dans .integrity/).
     * Exemple : "src/test.txt" -> ".integrity/src_test.txt.integrity.json"
     */
    private Path integrityPathFor(Path file) {
        String key = normalizedKey(file).replace('/', '_');
        return integrityDir.resolve(key + ".integrity.json");
    }

    /**
     * Normalise la clé de fichier (uniformise les séparateurs).
     */
    private String normalizedKey(Path file) {
    Path absFile = file.toAbsolutePath().normalize();

    String rel;
    if (absFile.startsWith(rootDir)) {
        rel = rootDir.relativize(absFile).toString();
    } else {
        rel = file.normalize().toString();
    }

    return rel.replace('\\', '/');
}


    private String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String extractJsonStringValue(String content, int keyIdx) {
        int colon = content.indexOf(":", keyIdx);
        int firstQuote = content.indexOf("\"", colon + 1);
        int secondQuote = content.indexOf("\"", firstQuote + 1);
        return content.substring(firstQuote + 1, secondQuote);
    }

    private long extractJsonLongValue(String content, int keyIdx) {
        int colon = content.indexOf(":", keyIdx);
        int i = colon + 1;
        while (i < content.length() && Character.isWhitespace(content.charAt(i))) i++;
        int j = i;
        while (j < content.length() && Character.isDigit(content.charAt(j))) j++;
        return Long.parseLong(content.substring(i, j));
    }

    public Path getIntegrityDir() {
        return integrityDir;
    }

    public static class IntegrityEntry {
        public final String hash;
        public final long size;

        public IntegrityEntry(String hash, long size) {
            this.hash = hash;
            this.size = size;
        }
    }
}
