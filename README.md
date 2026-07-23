# Multimodal Automatic Annotation

Java tool for running multimodal (image/video) LLM annotation over tweet datasets using [DUUI](https://github.com/texttechnologylab/DockerUnifiedUIMAInterface) (Docker Unified UIMA Interface) and the [duui-mm](https://github.com/texttechnologylab/duui-uima/tree/main/duui-mm) component.

For each tweet, the attached image or video is sent to a vision-language model (e.g. Qwen2.5-VL, Qwen3-Omni) which returns a structured JSON annotation covering:

- **Emotion classification** (anger, sadness, apprehension, confusion, happiness)
- **Sentiment analysis** (positive, neutral, negative)
- **Topic classification** (Dewey Decimal Classification, level 1)

Each label is returned with a confidence distribution and a short evidence-based explanation.

## Requirements

- Java 21
- Maven
- A running `duui-mm` DUUI component (Docker) reachable over HTTP
- Tweet data as gzip-compressed JSON files (Twitter API v2 shape) with base64-encoded media

## Project structure

```
src/main/java/
├── Main.java                        # Entry point: wires models, data dirs, and processing
├── models/
│   ├── Tweet.java                   # Parses a tweet from JSON (id, text, media)
│   └── MediaProcessingStatus.java   # Tracks per-tweet processing outcome
└── videoanalyzer/
    ├── DUUIRunner.java              # Builds/runs the DUUI pipeline against the LLM component
    ├── TweetDataReader.java         # Streams tweets with image/video/text media from .gz files
    └── ResultSaver.java             # Appends JSONL results, skipping already-processed tweets
```

## Configuration

Edit `src/main/java/Main.java` to set:

- `models`: map of model name → DUUI component URL
- `dataDirs` / `datasetNames`: input data directories and their dataset labels

Results are written as JSON Lines to `results/twitter_lrec/results_<dataset>_<mediaType>_<model>.csv`, one line per tweet, and are resumable — tweets already present in the output file are skipped.

## Build & run

```bash
mvn compile
mvn exec:java -Dexec.mainClass="Main"
```

## License

AGPL-3.0-or-later, see the `licenses` section of [pom.xml](pom.xml).
