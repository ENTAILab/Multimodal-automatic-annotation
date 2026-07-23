package models;

public class MediaProcessingStatus {
    public String tweetId;
    public String mediaType; // "image" or "video"
    public String status;    // "success" or "failed"
    public String exception;
    public String tweetText;

    public MediaProcessingStatus(String tweetText, String tweetId, String mediaType, String status, String exception) {
        this.tweetText = tweetText;
        this.tweetId = tweetId;
        this.mediaType = mediaType;
        this.status = status;
        this.exception = exception;
    }
}