package models;


import com.fasterxml.jackson.databind.JsonNode;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class Tweet {
    private final String tweetId;
    private final String mediaMimeType;
    private final List<String> media;
    private final boolean hasMedia;
    private final String text;

    private Tweet(String tweetId, String mediaMimeType, List<String> media, boolean hasMedia, String text) {
        this.tweetId = tweetId;
        this.mediaMimeType = mediaMimeType;
        this.media = media;
        this.hasMedia = hasMedia;
        byte ptext[] = null;
        try {
            ptext = text.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            this.text = new String(ptext, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("text is: " + text);
    }



    public static Tweet fromJsonNode(JsonNode node) {
        String tweetId = node.path("id").asText();
        boolean hasMedia = false;
        String mediaMimeType = null;
        List<String> mediaList = new ArrayList<>();
        String text = node.path("text").asText();

        if (node.has("entities") && node.get("entities").has("urls")) {
            for (JsonNode urlNode : node.get("entities").get("urls")) {
                String expandedUrl = urlNode.path("expanded_url").asText("");
                String base64 = urlNode.has("base64") ? urlNode.get("base64").asText(null) : null;
                if (base64 != null && (expandedUrl.contains("video") || expandedUrl.contains("photo"))) {
                    hasMedia = true;
                    mediaMimeType = expandedUrl.contains("video") ? "video" : "photo";
                    mediaList.add(base64);
                    break; // only take one media per tweet
                }
            }
        }

        return new Tweet(tweetId, mediaMimeType, mediaList, hasMedia, text);
    }

    public String getTweetId() {
        return tweetId;
    }

    public String getMediaMimeType() {
        return mediaMimeType;
    }

    public List<String> getMedia() {
        return media;
    }

    public boolean hasMedia() {
        return hasMedia;
    }

    public String getText() {
        return text;
    }

}