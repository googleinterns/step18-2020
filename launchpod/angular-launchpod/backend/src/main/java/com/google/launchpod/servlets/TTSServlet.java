package com.google.launchpod.servlets;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

@WebServlet("/tts-feed")
public class TTSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String title = "title";
    private static final String language = "language";
    private static final String description = "description";
    private static final String text = "text";
    private static final String USER_FEED = "UserFeed";

    // Variables required for cloud storage
    private static final String PROJECT_ID = "launchpod-step18-2020"; // ID of GCP Project
    private static final String BUCKET_NAME = "launchpod-mp3-files"; // ID of GCS bucket to upload to

    public void doPost(HttpServletRequest request, HttpServletResponse res) throws IOException {
        String podcastTitle = request.getParameter(title);
        String podcastLanguage = request.getParameter(language);
        String podcastDescription = request.getParameter(description);
        String podcastText = request.getParameter(text);

        Entity userFeedEntity = new Entity(USER_FEED);
        String feedId = KeyFactory.keyToString(userFeedEntity.getKey());
        ByteString synthesizedMp3;
        try {
            synthesizedMp3 = synthesizeText(podcastText, feedId);
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_CONFLICT, "unable to create mp3 from request. Please try again.");
        }
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
        BlobId blobId = BlobId.of(BUCKET_NAME, feedId);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        // TODO: Implement mp3 upload to cloudstore
        // storage.create(blobInfo, synthesizedMp3 );
    }

    /**
     * Demonstrates using the Text to Speech client to synthesize text or ssml.
     *
     * @param text the raw text to be synthesized. (e.g., "Hello there!")
     * @throws Exception on TextToSpeechClient Errors.
     */
    public static ByteString synthesizeText(String text, String id) throws Exception {
        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Synthesize text that is inputted
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build voice and set the voice type
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("en-US")
                    .setSsmlGender(SsmlVoiceGender.FEMALE).build();

            // Select audio to be MP3
            AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Write the response to the output file.
            try (OutputStream out = new FileOutputStream(id + ".mp3")) {
                out.write(audioContents.toByteArray());
                return audioContents;
            }
        }
    }
}
