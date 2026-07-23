
import models.MediaProcessingStatus;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.xml.sax.SAXException;
import videoanalyzer.DUUIRunner;
import videoanalyzer.ResultSaver;
import videoanalyzer.TweetDataReader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final List<MediaProcessingStatus> mediaProcessingResults = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Map<String, String> models = Map.of(
//                "Qwen/Qwen2.5-VL-7B-Instruct", "http://anduin.hucompute.org:9992"
//                "Qwen/Qwen2.5-VL-32B-Instruct", "http://anduin.hucompute.org:9992",
//                "Qwen/Qwen2.5-VL-72B-Instruct", "http://anduin.hucompute.org:9992"
//                "Qwen/Qwen2.5-VL-3B-Instruct", "http://anduin.hucompute.org:9992"
//                "Qwen/Qwen2.5-VL-3B-Instruct", "http://gondor.hucompute.org:9992"
//                "Qwen/Qwen2.5-VL-3B-Instruct", "http://127.0.0.1:9992"
//                "Qwen/Qwen2.5-VL-3B-Instruct", "http://anduin.hucompute.org:9992",
                "vllm/Qwen/Qwen3-Omni-30B-A3B-Instruct", "http://127.0.0.1:9999"



        );

        String[] dataDirs = {
                "/storage/projects/bagci/data/X/Bundestag/Tweets_with_media/",
                "/storage/projects/bagci/data/X/Queries_with_media_stats"
        };

        String[] datasetNames = {
                "tweets_politics",
                "tweet_queries"
        };


        for (Map.Entry<String, String> modelEntry : models.entrySet()) {
            String model = modelEntry.getKey();
            String duuiUrl = modelEntry.getValue();

            for (int i = 0; i < dataDirs.length; i++) {
                String dataDir = dataDirs[i];
                String dataset = datasetNames[i];

                TweetDataReader reader = new TweetDataReader(dataDir);

               processImage(reader, duuiUrl, model, dataset);
               processVideo(reader, duuiUrl, model, dataset);
               processText(reader, duuiUrl, model, dataset);
               // counter of broken and existing, uncomment when needed
                // CheckImage(reader);
                // CheckVideo(reader);

            }
            // for counter and summery, uncomment
            // saveProcessingSummary("tweet_all_unique_summary_with_include.csv");
            System.out.println("All tweets processed.");

        }

        System.out.println("All tweets processed.");
    }

    private static void processVideo(TweetDataReader reader, String duuiUrl, String model, String dataset) throws CompressorException, URISyntaxException, IOException, UIMAException, SAXException {
        String outputPath = "results/twitter_lrec/results_" + dataset + "_video_" + model.replace("/", "_") + ".csv";
        ResultSaver saver = new ResultSaver(outputPath);
        DUUIRunner duuivideo = new DUUIRunner(duuiUrl, model, "video");

        AtomicInteger totalVideos = new AtomicInteger(0);
        AtomicInteger processedVideos = new AtomicInteger(0);

        AtomicInteger failedVideos = new AtomicInteger(0);


        String prompt = "You are an AI system tasked with performing multimodal analysis on a media input (video and accompanying audio if exists). Your responsibilities include:\n" +
                "\n" +
                "1. **Emotion Classification**  \n" +
                "Identify one or more emotional states expressed visually in the image. Use the following emotion labels:  \n" +
                "- anger (AN)  \n" +
                "- sadness (SD)  \n" +
                "- apprehension (AP)  \n" +
                "- confusion (CO)  \n" +
                "- happiness (HA)  \n" +
                "\n" +
                "2. **Sentiment Analysis**  \n" +
                "Assess the overall sentiment conveyed by the image. Choose from:  \n" +
                "- positive  \n" +
                "- neutral  \n" +
                "- negative  \n" +
                "\n" +
                "3. **Topic Classification (Based on DDC Level 1)**  \n" +
                "Determine the most relevant topic of the image using one of the following Dewey Decimal classes:  \n" +
                "- 000: Computer science, information, and general works  \n" +
                "- 100: Philosophy and psychology  \n" +
                "- 200: Religion  \n" +
                "- 300: Social sciences  \n" +
                "- 400: Language  \n" +
                "- 500: Science  \n" +
                "- 600: Technology  \n" +
                "- 700: Arts and recreation  \n" +
                "- 800: Literature  \n" +
                "- 900: History and geography  \n" +
                "\n" +
                "You must return the result in **valid JSON format**, with the following components for each of the three tasks:\n" +
                "- The **predicted label**  \n" +
                "- A **confidence distribution** over all possible labels (values between 0 and 1, summing to 1)  \n" +
                "- A **brief explanation**, grounded in specific visual or contextual cues observed in the image  \n" +
                "\n" +
                "The output must follow this structure:\n" +
                "{\n" +
                "  \"emotion\": {\n" +
                "    \"predicted\": \"<label>\",\n" +
                "    \"confidence_distribution\": {\n" +
                "      \"anger\": <float>,\n" +
                "      \"sadness\": <float>,\n" +
                "      \"apprehension\": <float>,\n" +
                "      \"confusion\": <float>,\n" +
                "      \"happiness\": <float>\n" +
                "    },\n" +
                "    \"explanation\": \"<evidence-based explanation>\"\n" +
                "  },\n" +
                "  \"sentiment\": {\n" +
                "    \"predicted\": \"<label>\",\n" +
                "    \"confidence_distribution\": {\n" +
                "      \"positive\": <float>,\n" +
                "      \"neutral\": <float>,\n" +
                "      \"negative\": <float>\n" +
                "    },\n" +
                "    \"explanation\": \"<evidence-based explanation>\"\n" +
                "  },\n" +
                "  \"topic\": {\n" +
                "    \"predicted\": \"<label>\",\n" +
                "    \"confidence_distribution\": {\n" +
                "      \"000\": <float>,\n" +
                "      \"100\": <float>,\n" +
                "      \"200\": <float>,\n" +
                "      \"300\": <float>,\n" +
                "      \"400\": <float>,\n" +
                "      \"500\": <float>,\n" +
                "      \"600\": <float>,\n" +
                "      \"700\": <float>,\n" +
                "      \"800\": <float>,\n" +
                "      \"900\": <float>\n" +
                "    },\n" +
                "    \"explanation\": \"<evidence-based explanation>\"\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "Important Constraints:\n" +
                "\n" +
                "    All outputs must be grounded in the specific content of the image provided.\n" +
                "\n" +
                "    You must not copy or reuse example values.\n" +
                "\n" +
                "    The confidence values must reflect the actual visual cues in the image.\n" +
                "\n" +
                "    If the image contains multiple people or elements, base the classification on the dominant or most salient features, but take all visual evidence into account when calculating confidence.\n" +
                "\n" +
                "Return only the JSON object. Do not include commentary or template examples.";

        System.out.println("Start processing video");

//        System.out.println(prompt);

        try {

            reader.forEachTweetVideo(tweet -> {
                // Replace the %s in the prompt with the actual text content
                String finalPrompt = String.format(prompt, tweet.text);

                finalPrompt = finalPrompt + "Video: ";
                totalVideos.incrementAndGet();

                try {

                    if (saver.isAlreadySaved(tweet.tweetId)) return;
                    processedVideos.incrementAndGet();

                    String result = duuivideo.analyzeVideo(finalPrompt, tweet.base64Video);
                    saver.save(tweet.tweetId, result);
                    mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "video", "success", ""));
//                System.out.println("Processed tweet: " + tweet.tweetId);
                } catch (Exception e) {
                    failedVideos.incrementAndGet();
                    mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "video", "failed", e.getMessage()));
//                System.err.println("Failed to process video " + tweet.tweetId + ": " + e.getMessage());
                }
            });

        } catch (Exception e) {
            failedVideos.incrementAndGet();
            mediaProcessingResults.add(new MediaProcessingStatus("","", "video", "failed", e.getMessage()));
//                System.err.println("Failed to process video " + tweet.tweetId + ": " + e.getMessage());
        }

        System.out.format("Videos - total: %d, processed %d,  broken: %d%n", totalVideos.get(), processedVideos.get(),failedVideos.get());

        System.out.println("Finished processing video");
    }

    private static void processImage(TweetDataReader reader, String duuiUrl, String model, String dataset) throws CompressorException, URISyntaxException, IOException, UIMAException, SAXException {
        String outputPath = "results/twitter_lrec/results_" + dataset + "_image_" + model.replace("/", "_") + ".csv";
        ResultSaver saver = new ResultSaver(outputPath);
        DUUIRunner duuivideo = new DUUIRunner(duuiUrl, model, "image");

        AtomicInteger totalImages = new AtomicInteger(0);
        AtomicInteger processedImages = new AtomicInteger(0);

        AtomicInteger failedImages = new AtomicInteger(0);


        String prompt = "You are an AI system tasked with performing multimodal analysis on a media input (image). Your responsibilities include:\n" +
                "\n" +
                "1. **Emotion Classification**  \n" +
                "Identify one or more emotional states expressed visually in the image. Use the following emotion labels:  \n" +
                "- anger (AN)  \n" +
                "- sadness (SD)  \n" +
                "- apprehension (AP)  \n" +
                "- confusion (CO)  \n" +
                "- happiness (HA)  \n" +
                "\n" +
                "2. **Sentiment Analysis**  \n" +
                "Assess the overall sentiment conveyed by the image. Choose from:  \n" +
                "- positive  \n" +
                "- neutral  \n" +
                "- negative  \n" +
                "\n" +
                "3. **Topic Classification (Based on DDC Level 1)**  \n" +
                "Determine the most relevant topic of the image using one of the following Dewey Decimal classes:  \n" +
                "- 000: Computer science, information, and general works  \n" +
                "- 100: Philosophy and psychology  \n" +
                "- 200: Religion  \n" +
                "- 300: Social sciences  \n" +
                "- 400: Language  \n" +
                "- 500: Science  \n" +
                "- 600: Technology  \n" +
                "- 700: Arts and recreation  \n" +
                "- 800: Literature  \n" +
                "- 900: History and geography  \n" +
                "\n" +
                "You must return the result in **valid JSON format**, with the following components for each of the three tasks:\n" +
                "- The **predicted label**  \n" +
                "- A **confidence distribution** over all possible labels (values between 0 and 1, summing to 1)  \n" +
                "- A **brief explanation**, grounded in specific visual or contextual cues observed in the image  \n" +
                "\n" +
                "The output must follow this structure:\n" +
                "{\n" +
                "  \"emotion\": {\n" +
                "    \"predicted\": \"<label>\",\n" +
                "    \"confidence_distribution\": {\n" +
                "      \"anger\": <float>,\n" +
                "      \"sadness\": <float>,\n" +
                "      \"apprehension\": <float>,\n" +
                "      \"confusion\": <float>,\n" +
                "      \"happiness\": <float>\n" +
                "    },\n" +
                "    \"explanation\": \"<evidence-based explanation>\"\n" +
                "  },\n" +
                "  \"sentiment\": {\n" +
                "    \"predicted\": \"<label>\",\n" +
                "    \"confidence_distribution\": {\n" +
                "      \"positive\": <float>,\n" +
                "      \"neutral\": <float>,\n" +
                "      \"negative\": <float>\n" +
                "    },\n" +
                "    \"explanation\": \"<evidence-based explanation>\"\n" +
                "  },\n" +
                "  \"topic\": {\n" +
                "    \"predicted\": \"<label>\",\n" +
                "    \"confidence_distribution\": {\n" +
                "      \"000\": <float>,\n" +
                "      \"100\": <float>,\n" +
                "      \"200\": <float>,\n" +
                "      \"300\": <float>,\n" +
                "      \"400\": <float>,\n" +
                "      \"500\": <float>,\n" +
                "      \"600\": <float>,\n" +
                "      \"700\": <float>,\n" +
                "      \"800\": <float>,\n" +
                "      \"900\": <float>\n" +
                "    },\n" +
                "    \"explanation\": \"<evidence-based explanation>\"\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "Important Constraints:\n" +
                "\n" +
                "    All outputs must be grounded in the specific content of the image provided.\n" +
                "\n" +
                "    You must not copy or reuse example values.\n" +
                "\n" +
                "    The confidence values must reflect the actual visual cues in the image.\n" +
                "\n" +
                "    If the image contains multiple people or elements, base the classification on the dominant or most salient features, but take all visual evidence into account when calculating confidence.\n" +
                "\n" +
                "Return only the JSON object. Do not include commentary or template examples.";

        System.out.println("Start processing image");

//        System.out.println(prompt);


        reader.forEachTweetImaage(tweet -> {
            try {
                totalImages.incrementAndGet();

                if (saver.isAlreadySaved(tweet.tweetId)) return;
                processedImages.incrementAndGet();
                String result = duuivideo.analyzeImage(prompt, tweet.base64Image);
                saver.save(tweet.tweetId, result);
                //                System.out.println("Processed tweet: " + tweet.tweetId);
                mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "image", "success", ""));
            } catch (Exception e) {
                failedImages.incrementAndGet();
//                System.err.println("Failed to process image " + tweet.tweetId + ": " + e.getMessage());
                mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "image", "failed", e.getMessage()));
            }
        });

        System.out.format("images - total %d, processed %d,  total failed %d", totalImages.get(),processedImages.get(), failedImages.get());
        System.out.println("Finished processing images");
    }

    private static void CheckImage(TweetDataReader reader) throws CompressorException, URISyntaxException, IOException, UIMAException, SAXException {
        System.out.println("Start processing image");
        AtomicInteger totalImages = new AtomicInteger(0);
        AtomicInteger failedImages = new AtomicInteger(0);
        AtomicInteger processedImages = new AtomicInteger(0);
        Set<String> brokenImageIds = new HashSet<>(); // Use Set for uniqueness
        Set<String> processedTweetIds = new HashSet<>(); // Track processed tweet IDs

        reader.forEachTweetImaage(tweet -> {
            totalImages.incrementAndGet();
            try {
                // Skip if this tweet ID has already been processed
                if (processedTweetIds.contains(tweet.tweetId)) {
                    return;
                }
                processedTweetIds.add(tweet.tweetId); // Mark as processed
                mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "image", "success", ""));

//                if (tweet.base64Image == null || tweet.base64Image.isEmpty()) {
//                    failedImages.incrementAndGet();
//                    brokenImageIds.add(tweet.tweetId); // Add to broken set
//                    throw new Exception("Empty image");
//                }
//                Base64.getDecoder().decode(tweet.base64Image);
//                processedImages.incrementAndGet();
            } catch (Exception e) {
                mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "image", "failed", ""));
                failedImages.incrementAndGet();
                brokenImageIds.add(tweet.tweetId); // Add to broken set
                System.err.println("Broken image (base64) at tweet " + tweet.tweetId + ": " + e.getMessage());
            }
        });

        System.out.format("Images; total: %d, processed: %d, broken: %d%n", totalImages.get(), processedImages.get(), failedImages.get());
        System.out.println("Broken image tweet IDs (unique): " + brokenImageIds);
    }


    private static void CheckVideo(TweetDataReader reader) throws CompressorException, URISyntaxException, IOException, UIMAException, SAXException {
        System.out.println("Start processing video");
        AtomicInteger totalVideos = new AtomicInteger(0);
        AtomicInteger failedVideos = new AtomicInteger(0);
        AtomicInteger processedVideos = new AtomicInteger(0);
        Set<String> brokenVideoIds = new HashSet<>(); // Use Set for uniqueness
        Set<String> processedTweetIds = new HashSet<>(); // Track processed tweet IDs

        reader.forEachTweetVideo(tweet -> {
            totalVideos.incrementAndGet();
            try {
                // Skip if this tweet ID has already been processed
                if (processedTweetIds.contains(tweet.tweetId)) {
                    return;
                }
                processedTweetIds.add(tweet.tweetId); // Mark as processed
                mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "video", "success", ""));

//                if (tweet.base64Video == null || tweet.base64Video.isEmpty()) {
//                    failedVideos.incrementAndGet();
//                    brokenVideoIds.add(tweet.tweetId); // Add to broken set
//                    throw new Exception("Empty video");
//                }
//                // Attempt to decode base64
//                Base64.getDecoder().decode(tweet.base64Video);
//                processedVideos.incrementAndGet();
            } catch (Exception e) {
                failedVideos.incrementAndGet();
                mediaProcessingResults.add(new MediaProcessingStatus(tweet.text, tweet.tweetId, "video", "failed", ""));

                brokenVideoIds.add(tweet.tweetId); // Add to broken set
                System.err.println("Broken video (base64) at tweet " + tweet.tweetId + ": " + e.getMessage());
            }
        });

        System.out.format("Videos - total: %d, processed: %d, broken: %d%n", totalVideos.get(), processedVideos.get(), failedVideos.get());
        System.out.println("Broken video tweet IDs (unique): " + brokenVideoIds);
    }




    private static void saveProcessingSummary(String filepath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            writer.println("tweetId,mediaType,status");
            for (MediaProcessingStatus status : mediaProcessingResults) {
                writer.printf("%s,%s,%s%n", status.tweetId, status.mediaType, status.status);
            }
            System.out.println("Saved processing summary to " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to write summary file: " + e.getMessage());
        }
    }

}
