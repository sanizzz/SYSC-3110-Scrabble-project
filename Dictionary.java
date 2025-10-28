import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Simple word list that checks whether a play is legal.
 */
public class Dictionary {
    private final Set<String> words = new HashSet<>();

    public Dictionary(String path) {
        loadWordList(path);
    }

    private void loadWordList(String path) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String sanitized = line.replaceAll("[^A-Za-z]", " ");
                for (String token : sanitized.split("\\s+")) {
                    if (!token.isEmpty()) {
                        words.add(token.toUpperCase(Locale.ROOT));
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dictionary from " + path, e);
        }
    }

    /**
     * @return true when the word appears in the dictionary.
     */
    public boolean isValidWord(String token) {
        if (token == null) {
            return false;
        }
        return words.contains(token.toUpperCase(Locale.ROOT));
    }
}
