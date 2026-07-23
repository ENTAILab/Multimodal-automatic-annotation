package videoanalyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.StreamReadConstraints;
import java.io.IOException;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.function.Consumer;

import models.Tweet;

/**
 * @author Ali Abusaleh
 */
public class TweetDataReader {
    private final String dataDir;

    public TweetDataReader(String dataDir) {
        this.dataDir = dataDir;
    }



    public void forEachTweetVideo(Consumer<TweetVideo> processor) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxStringLength(Integer.MAX_VALUE)
                        .build()
        );

        Files.walk(Paths.get(dataDir))
                .filter(path -> path.toString().endsWith(".gz"))
                .forEach(path -> {
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(path.toFile()))) {
                        JsonNode root = mapper.readTree(gzipInputStream);
                        if (!root.has("data") || !root.get("data").isArray()) return;

                        for (JsonNode tweetNode : root.get("data")) {
                            Tweet tweet = Tweet.fromJsonNode(tweetNode);
                            if (tweet.hasMedia() && tweet.getMediaMimeType().equals("video")) {
                                for (String base64 : tweet.getMedia()) {
                                    processor.accept(new TweetVideo(tweet.getTweetId(), base64, tweet.getText()));
                                }
                            }
                            if (root.get("includes") != null) {
                                try {
                                    for (JsonNode tweetNode2 : root.get("includes").get("tweets")) {
                                        Tweet tweet2 = Tweet.fromJsonNode(tweetNode2);
                                        if (tweet2.hasMedia() && tweet2.getMediaMimeType().equals("video")) {
                                            for (String base64 : tweet2.getMedia()) {
                                                processor.accept(new TweetVideo(tweet2.getTweetId(), base64, tweet2.getText()));
                                            }
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void forEachTweetImaage(Consumer<TweetImage> processor) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxStringLength(Integer.MAX_VALUE)
                        .build()
        );

        Files.walk(Paths.get(dataDir))
                .filter(path -> path.toString().endsWith(".gz"))
                .forEach(path -> {
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(path.toFile()))) {
                        JsonNode root = mapper.readTree(gzipInputStream);
                        if (!root.has("data") || !root.get("data").isArray()) return;

                        for (JsonNode tweetNode : root.get("data")) {
                            Tweet tweet = Tweet.fromJsonNode(tweetNode);
                            if (tweet.hasMedia() && tweet.getMediaMimeType().equals("photo")) {
                                for (String base64 : tweet.getMedia()) {
                                    processor.accept(new TweetImage(tweet.getTweetId(), base64, tweet.getText()));
                                }
                            }
                            if (root.get("includes") != null) {
                                try {
                                    for (JsonNode tweetNode2 : root.get("includes").get("tweets")) {
                                        Tweet tweet2 = Tweet.fromJsonNode(tweetNode2);
                                        if (tweet2.hasMedia() && tweet2.getMediaMimeType().equals("photo")) {
                                            for (String base64 : tweet2.getMedia()) {
                                                processor.accept(new TweetImage(tweet2.getTweetId(), base64, tweet2.getText()));
                                            }
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void forEachTweetText(Consumer<TweetText> processor) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxStringLength(Integer.MAX_VALUE)
                        .build()
        );

        Files.walk(Paths.get(dataDir))
                .filter(path -> path.toString().endsWith(".gz"))
                .forEach(path -> {
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(path.toFile()))) {
                        JsonNode root = mapper.readTree(gzipInputStream);
                        if (!root.has("data") || !root.get("data").isArray()) return;

                        for (JsonNode tweetNode : root.get("data")) {
                            Tweet tweet = Tweet.fromJsonNode(tweetNode);
                            if (tweet.hasMedia()){
                                processor.accept(new TweetText(tweet.getTweetId(), tweet.getText()));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }



    public void forEachTweet(Consumer<TweetText> processor) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxStringLength(Integer.MAX_VALUE)
                        .build()
        );

        Files.walk(Paths.get(dataDir))
                .filter(path -> path.toString().endsWith(".gz"))
                .forEach(path -> {
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(path.toFile()))) {
                        JsonNode root = mapper.readTree(gzipInputStream);
                        if (!root.has("data") || !root.get("data").isArray()) return;

                        for (JsonNode tweetNode : root.get("data")) {
                            Tweet tweet = Tweet.fromJsonNode(tweetNode);
                            if (tweet.hasMedia()){
                                processor.accept(new TweetText(tweet.getTweetId(), tweet.getText()));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static class TweetImage{
        public final String tweetId;
        public final String text;
        public final String base64Image;

        public TweetImage(String tweetId, String base64Image, String text) {
            this.tweetId = tweetId;
            this.base64Image = base64Image;
            this.text = text;

        }
    }

    public static class TweetVideo {
        public final String tweetId;
        public final String base64Video;
        public final String text;

        public TweetVideo(String tweetId, String base64Video, String text) {
            this.tweetId = tweetId;
            this.base64Video = base64Video;
            this.text = text;
        }
    }

    public static class TweetText {
        public final String tweetId;
        public final String text;

        public TweetText(String tweetId, String text) {
            this.tweetId = tweetId;
            this.text = text;
        }
    }
}