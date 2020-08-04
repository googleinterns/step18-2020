package com.google.launchpod.servlets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.api.client.http.FileContent;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Blob.BlobSourceOption;
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
import com.google.common.base.Strings;
import com.google.launchpod.data.LoginStatus;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.UserFeed;
import com.google.protobuf.ByteString;

@WebServlet("/create-by-tts")
public class TTSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TITLE = "episodeTitle";
    private static final String LANGUAGE = "episodeLanguage";
    private static final String DESCRIPTION = "episodeDescription";
    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String CATEGORY = "category";
    private static final String FEED_KEY = "id";
    private static final String TEXT = "text";
    private static final String XML_STRING = "xmlString";
    private static final String ID = "id";
    private static final String USER_FEED = "UserFeed";
    private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
    private static final String TTS_BASE_URL = "https://launchpod-step18-2020.appspot.com/create-by-tts?id=";
    private static final Gson GSON = new Gson();

    // Variables required for cloud storage
    private static final String PROJECT_ID = "launchpod-step18-2020"; // ID of GCP Project
    private static final String BUCKET_NAME = "launchpod-mp3-files"; // ID of GCS bucket to upload to
    private static final String CLOUD_BASE_URL = "https://storage.googleapis.com/" + BUCKET_NAME + "/";

    static {
        TranslationServlet.XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Requests user inputs from the form field to add item to existing channel, and
     * synthesizes the podcast Text. Then returns original RSS feed with the updated
     * item added by the user0
     * 
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse res) throws IOException {
        UserService userService = UserServiceFactory.getUserService();

        String userEmail = userService.getCurrentUser().getEmail();
        String feedKey = request.getParameter(FEED_KEY);
        String podcastTitle = request.getParameter(TITLE);
        String podcastDescription = request.getParameter(DESCRIPTION);
        String podcastLanguage = request.getParameter(LANGUAGE);
        String podcastText = request.getParameter(TEXT);

        // throw exception when parameters are empty or null
        if (Strings.isNullOrEmpty(feedKey)) {
            throw new IllegalArgumentException("Unable to get key. Please try again");
        } else if (Strings.isNullOrEmpty(podcastTitle)) {
            throw new IllegalArgumentException("Unable to get Podcast Title. Please try again");
        } else if (Strings.isNullOrEmpty(podcastDescription)) {
            throw new IllegalArgumentException("Unable to get Podcast Description. Please try again");
        } else if (Strings.isNullOrEmpty(podcastText)) {
            throw new IllegalArgumentException("Unable to get Podcast Text. Please try again");
        }

        // Search for key from given feed id
        Key desiredFeedKey = KeyFactory.stringToKey(feedKey);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity desiredFeedEntity = null;
        try {
            desiredFeedEntity = datastore.get(desiredFeedKey);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to find given URL key, Please try again");
            return;
        }

        // Turn the xml string back into an object
        String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);
        RSS rssFeed = null;
        try {
            rssFeed = TranslationServlet.XML_MAPPER.readValue(xmlString, RSS.class);
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to translate. Try again");
            return;
        }

        // Synthesize The podcast Text
        String ttsFeedId = KeyFactory.keyToString(desiredFeedEntity.getKey()); // key for the entity that contains the
                                                                               // channel to be modified
        ByteString synthesizedMp3 = null;
        try {
            synthesizedMp3 = synthesizeText(podcastText);
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_CONFLICT, "unable to create mp3 from request. Please try again.");
            return;
        }
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
        String itemCount = String.valueOf(rssFeed.getChannel().getItems().size());
        BlobId blobId = BlobId.of(BUCKET_NAME, ttsFeedId + itemCount);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        // create an uploadble array of bytes for blobstore
        byte[] mp3Bytes = synthesizedMp3.toByteArray();
        storage.create(blobInfo, mp3Bytes);

        // Generate mp3 link
        String mp3Link = TTS_BASE_URL + ttsFeedId + itemCount;

        rssFeed.getChannel().addItem(podcastTitle, podcastDescription, podcastLanguage,userEmail, mp3Link);

        desiredFeedEntity.setProperty(XML_STRING, RSS.toXmlString(rssFeed));

        datastore.put(desiredFeedEntity);

        Query query = new Query(LoginStatus.USER_FEED_KEY).addSort(LoginStatus.TIMESTAMP_KEY, SortDirection.DESCENDING);

        PreparedQuery results = datastore.prepare(query);

        ArrayList<UserFeed> userFeeds = new ArrayList<UserFeed>();
        for (Entity entity : results.asIterable()) {
            if (userEmail.equals(entity.getProperty(EMAIL).toString())) {
                String userFeedTitle = (String) entity.getProperty(LoginStatus.TITLE_KEY);
                String userFeedName = (String) entity.getProperty(LoginStatus.NAME_KEY);
                String userFeedDescription = (String) entity.getProperty(LoginStatus.DESCRIPTION_KEY);
                String userFeedLanguage = (String) entity.getProperty(LoginStatus.LANGUAGE_KEY);
                String userFeedEmail = (String) entity.getProperty(LoginStatus.EMAIL_KEY);
                long userFeedTimestamp = (long) entity.getProperty(LoginStatus.TIMESTAMP_KEY);
                Date date = new Date(userFeedTimestamp);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss Z", Locale.getDefault());
                String postTime = dateFormat.format(date);
                Key key = entity.getKey();

                String urlID = KeyFactory.keyToString(entity.getKey()); // the key string associated with the entity,
                                                                        // not the
                                                                        // numeric ID.
                String rssLink = BASE_URL + urlID;

                userFeeds.add(new UserFeed(userFeedTitle, userFeedName, rssLink, userFeedDescription, userFeedEmail,
                        postTime, urlID, userFeedLanguage));
            }
        }

        res.setContentType("application/json");
        res.getWriter().println(GSON.toJson(userFeeds));
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("audio/mpeg");
        String id = req.getParameter(ID);
        String URI = "gs://launchpod-mp3-files/";

        if (Strings.isNullOrEmpty(id)) {
            throw new IllegalArgumentException("Invalid URL. Please try again");
        }

        // Search for id in cloud storage from parameter ID
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
        Blob desiredFeedBlob = storage.get(BUCKET_NAME, id);
        byte[] blobBytes = desiredFeedBlob.getContent(BlobSourceOption.generationMatch());
        
        //Write File to servlet output stream
        res.getOutputStream().write(blobBytes);
        res.setContentType("audio/mpeg");
        res.getWriter().print(blobBytes.toString());
    }
    
    /**
     * Demonstrates using the Text to Speech client to synthesize text or ssml.
     *
     * @param text the raw text to be synthesized. (e.g., "Hello there!")
     * @throws Exception on TextToSpeechClient Errors.
     */
    public static ByteString synthesizeText(String text) throws Exception {
        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Synthesize text that is inputted
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build voice and set the voice type
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("en-US")
                    .setSsmlGender(SsmlVoiceGender.FEMALE).build();

            // Select audio to be MP3
            AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            return audioContents;
        }
    }
}
