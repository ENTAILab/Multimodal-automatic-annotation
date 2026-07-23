package videoanalyzer;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.cas.FSArray;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.annotation.type.Image;
import org.texttechnologylab.annotation.type.Video;
import org.texttechnologylab.annotation.type.Audio;
import org.texttechnologylab.type.llm.prompt.Prompt;
import org.texttechnologylab.type.llm.prompt.Message;
import org.texttechnologylab.type.llm.prompt.Result;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.xml.sax.SAXException;

import org.apache.uima.jcas.cas.FSList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Ali Abusaleh
 */
public class DUUIRunner {
    static DUUIComposer composer;
    static JCas cas;
    int iWorkers = 2;

    public DUUIRunner(String url, String model,String mode) throws URISyntaxException, IOException, UIMAException, SAXException, CompressorException {
        composer = new DUUIComposer().withSkipVerification(true)
                .withLuaContext(new DUUILuaContext().withJsonLibrary());

//        composer.withWorkers(iWorkers);

        DUUIUIMADriver uima_driver = new DUUIUIMADriver();
        DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver();
        composer.addDriver(remoteDriver, uima_driver);

        composer.resetPipeline();


        composer.add(new DUUIRemoteDriver.Component(url)
                .withParameter("model_name", model)
                .withParameter("mode", mode)
//                .withScale(iWorkers)
                .build().withTimeout(500));

        cas = JCasFactory.createJCas();
    }

    public void createCasWithVideo(String language, List<String> prompts, String videoBase64) throws UIMAException {
        cas.setDocumentLanguage(language);


        // Add prompt
        for (String messageText : prompts) {
            Prompt prompt = new Prompt(cas);
            prompt.setArgs("{}");

            Message message = new Message(cas);
            message.setRole("user");
            message.setContent(messageText);
            message.addToIndexes();

            FSArray messages = new FSArray(cas, 1);
            messages.set(0, message);
            prompt.setMessages(messages);
            prompt.addToIndexes();
        }

        // Add video
        Video videoWrapper = new Video(cas);
        videoWrapper.setMimetype("video/mp4");
        videoWrapper.setSrc(videoBase64);
        videoWrapper.addToIndexes();


    }

    public void createCasWithImage(String language, List<String> prompts, String imageBase64) throws UIMAException {
        cas.setDocumentLanguage(language);


        // Add prompt
        for (String messageText : prompts) {
            Prompt prompt = new Prompt(cas);
            prompt.setArgs("{}");

            Message message = new Message(cas);
            message.setRole("user");
            message.setContent(messageText);
            message.addToIndexes();

            FSArray messages = new FSArray(cas, 1);
            messages.set(0, message);
            prompt.setMessages(messages);
            prompt.addToIndexes();
        }

        // Add video
        Image imageWrapper = new Image(cas);
        imageWrapper.setMimetype("image/jpeg");
        imageWrapper.setSrc(imageBase64);
        imageWrapper.addToIndexes();


    }

    public void createCasWithText(String language, List<String> prompts, String text) throws UIMAException {
        cas.setDocumentLanguage(language);


        // Add prompt
        for (String messageText : prompts) {
            Prompt prompt = new Prompt(cas);
            prompt.setArgs("{}");


            // add text to message text

            messageText = messageText + text ;


            Message message = new Message(cas);
            message.setRole("user");
            message.setContent(messageText);
            message.addToIndexes();

            FSArray messages = new FSArray(cas, 1);
            messages.set(0, message);
            prompt.setMessages(messages);
            prompt.addToIndexes();
        }


    }

    public String analyzeVideo(String promptText, String base64Video) throws Exception {

        try {
            createCasWithVideo("en", Collections.singletonList(promptText), base64Video);

            composer.run(cas);

            composer.resetPipeline();
            String results = "";
            for (Result result : JCasUtil.select(cas, Result.class)) {
                results = result.getMeta();
            }


            cas.reset();

            return results;
        }
        catch (Exception e) {
            cas.reset();
            return("Error in processing video ");
//            throw e;
        }
    }

    public String analyzeImage(String promptText, String base64Image) throws Exception {

        try {
            createCasWithImage("en", Collections.singletonList(promptText), base64Image);

            composer.run(cas);

            composer.resetPipeline();
            String results = "";
            for (Result result : JCasUtil.select(cas, Result.class)) {
                results = result.getMeta();
            }


            cas.reset();

            return results;
        }
        catch (Exception e) {
            cas.reset();
            return("Error in processing image ");
//            throw e;
        }
    }

    public String analyzeText(String promptText, String text) throws Exception {

        try {
            createCasWithText("en", Collections.singletonList(promptText), text);

            composer.run(cas);

            composer.resetPipeline();
            String results = "";
            for (Result result : JCasUtil.select(cas, Result.class)) {
                results = result.getMeta();
            }


            cas.reset();

            return results;
        }
        catch (Exception e) {
            cas.reset();
            return("Error in processing text ");
//            throw e;
        }
    }
}