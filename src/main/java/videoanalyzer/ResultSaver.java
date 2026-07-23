package videoanalyzer;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Ali Abusaleh
 */
public class ResultSaver {
    private final File outputFile;
    private final ObjectMapper objectMapper;
    private final Set<String> existingTweetIds;

    public ResultSaver(String outputPath) {
        this.outputFile = new File(outputPath);
        this.objectMapper = new ObjectMapper();
        this.existingTweetIds = loadExistingTweetIds();
    }

    private Set<String> loadExistingTweetIds() {
        Set<String> ids = new HashSet<>();
        if (!outputFile.exists()) return ids;

        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonNode node = objectMapper.readTree(line);
                    if (node.has("tweet_id")) {
                        ids.add(node.get("tweet_id").asText());
                    }
                } catch (IOException e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public boolean isAlreadySaved(String tweetId) {
        return existingTweetIds.contains(tweetId);
    }

    public void save(String tweetId, String responseJson) {
        if (isAlreadySaved(tweetId)) return;

        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile, true))) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("tweet_id", tweetId);
            record.put("response", objectMapper.readValue(responseJson, Map.class));
            out.println(objectMapper.writeValueAsString(record));
            existingTweetIds.add(tweetId);
        } catch (IOException e) {
            System.err.println("Failed to save result for tweet " + tweetId + ": " + e.getMessage());
        }
    }
}