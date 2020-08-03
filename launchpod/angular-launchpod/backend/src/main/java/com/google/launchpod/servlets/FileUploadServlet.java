package com.google.launchpod.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.lang.SecurityException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.auth.appengine.AppEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.UserFeed;
import com.google.launchpod.data.MP3;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.PostPolicyV4;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Strings;

@WebServlet("/create-by-upload")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

  // TO-DO after merging: move these to common place
  public static final String PROJECT_ID = "launchpod-step18-2020"; // The ID of your GCP project
  public static final String BUCKET_NAME = "launchpod-mp3-files"; // The ID of the GCS bucket to upload to

  public static final String USER_FEED = "UserFeed";
  public static final String PODCAST_TITLE = "title";
  public static final String DESCRIPTION = "description";
  public static final String LANGUAGE = "language";
  public static final String EMAIL = "email";
  public static final String TIMESTAMP = "timestamp";
  public static final String MP3 = "mp3";
  public static final String MP3_LINK = "mp3Link";
  public static final String XML_STRING = "xmlString";
  public static final String PUB_DATE = "pubDate";
  public static final String GENERATE_RSS_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?action=generateRSSLink&id=";
  public static final String HTML_FORM_END = "  <input type='file' name='file'/><br />\n"
                                             + "<input type='submit' value='Upload File' name='submit'/><br />\n"
                                             + "</form>\n";

  private static final String ID = "id";
  private static final String ACTION = "action";

  // TO-DO after merging: move this to common place
  /*
  * Outlines actions for doGet() method.
  * GENERATE_RSS_LINK: action for creating the URL to an RSS feed.
  * GENERATE_XML: action for retrieving the XML string for an RSS feed from Datastore.
  */
  private enum Action {
    GENERATE_RSS_LINK("generateRSSLink"),
    GENERATE_XML("generateXml"),
    OTHER_ACTION("otherAction"); // for testing purposes

    private String action;
 
    Action(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
      return action;
    }
  }

  /**
  * Creates an MP3 link from GCS bucket name and the object name (entity id).
  */
  private String makeMp3Link(String entityId) {
    String link = "https://storage.googleapis.com/" + BUCKET_NAME + "/" + entityId;
    return link;
  }

  /**
  * Helper method for repeated code in catching exceptions.
  */
  private void writeResponse(HttpServletResponse res, String message, int statusCode) throws IOException {
    res.setContentType("text/html");
    res.getWriter().println(message);
    res.setStatus(statusCode);
  }

  /**
   * Requests user inputs in form fields, then creates Entity and places in Datastore.
   * @throws ServletException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException, IOException {
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String description = req.getParameter(DESCRIPTION);
    String language = req.getParameter(LANGUAGE);
    String email = req.getParameter(EMAIL);

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      email = userService.getCurrentUser().getEmail();
    }

    // TO-DO after merging: move validation to common place 
    if (Strings.isNullOrEmpty(podcastTitle)) {
      throw new IllegalArgumentException("No Title inputted, please try again.");
    } else if (Strings.isNullOrEmpty(description)) {
      throw new IllegalArgumentException("No description inputted, please try again.");
    } else if (Strings.isNullOrEmpty(language)) {
      throw new IllegalArgumentException("No language inputted, please try again.");
    } else if (Strings.isNullOrEmpty(email)) {
      throw new IllegalArgumentException("You are not logged in. Please try again.");
    }

    // Create entity with all desired attributes
    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    userFeedEntity.setProperty(DESCRIPTION, description);
    userFeedEntity.setProperty(LANGUAGE, language);
    userFeedEntity.setProperty(EMAIL, email);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key entityKey = datastore.put(userFeedEntity);

    String entityId = KeyFactory.keyToString(entityKey);
    String mp3Link = makeMp3Link(entityId);

    // Create embedded entity to store MP3 data
    EmbeddedEntity mp3 = new EmbeddedEntity();
    mp3.setProperty(ID, entityId);
    mp3.setProperty(MP3_LINK, mp3Link);
    mp3.setProperty(EMAIL, email);

    // Retrieve entity from Datastore
    Entity savedEntity;
    try {
      savedEntity = datastore.get(entityKey);
    } catch(EntityNotFoundException e) {
      throw new IOException("Entity not found in Datastore.");
    }

    // Generate xml string
    // TO-DO in mp3 episode PR: name, email, podcastTitle, description, category, language
    RSS rssFeed = new RSS(podcastTitle, description, language, email, mp3Link);
    String xmlString;
    try {
      xmlString = RSS.toXmlString(rssFeed);
      savedEntity.setProperty(XML_STRING ,xmlString);
    } catch(IOException e){
      throw new IOException("Unable to create XML string.");
    }

    // Update entity by adding embedded mp3 entity as a property
    savedEntity.setProperty(MP3, mp3);
    datastore.put(savedEntity);

    //write the file upload form
    String formHtml = generateSignedPostPolicyV4(PROJECT_ID, BUCKET_NAME, entityId);
    res.setContentType("text/html");
    res.getWriter().println(formHtml);
  }

  /**
   * Generate policy for directly uploading a file to Cloud Storage via HTML form.
   * @return HTML form (as a String) for uploading MP3
   */
  public String generateSignedPostPolicyV4 (String projectId, String bucketName, String blobName) throws IOException {
    // String blobName = entity ID from Datastore, as the name of object uploaded to GCS

    String errorMessage = "Policy is null.";
    Storage storage;
    PostPolicyV4.PostFieldsV4 fields;
    PostPolicyV4 policy = null;
    String myRedirectUrl = "";

    try {
      // Prepare credentials and API key
      String keyFileName = "launchpod-step18-2020-47434aafba88.json";
      ClassLoader classLoader = getClass().getClassLoader();
      File file = new File(classLoader.getResource(keyFileName).getFile());
      GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(file));

      storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

      myRedirectUrl = GENERATE_RSS_URL + blobName;
      fields =
          PostPolicyV4.PostFieldsV4.newBuilder().setSuccessActionRedirect(myRedirectUrl).build();

      policy =
        storage.generateSignedPostPolicyV4(
            BlobInfo.newBuilder(bucketName, blobName).build(), 10, TimeUnit.MINUTES, fields);
    } catch(SecurityException | IOException e) {
      throw e;
    }

    StringBuilder htmlForm = new StringBuilder();
    if (policy!=null) {
      String formPostHtml = String.format("<form name='mp3-upload' action='%s' method='POST' enctype='multipart/form-data'>\n", policy.getUrl());
      htmlForm.append(formPostHtml);

      for (Map.Entry<String, String> entry : policy.getFields().entrySet()) {
        String hiddenPolicyInput = String.format("<input name='%s' value='%s' type='hidden' />\n", entry.getKey(), entry.getValue());
        htmlForm.append(hiddenPolicyInput);
      }
      htmlForm.append(HTML_FORM_END);
    } else {
      htmlForm.append(errorMessage);
    }
    return htmlForm.toString();
  }

  /**
  * Display RSS feed xml string that user tries recalling with the given ID.
  */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String actionString = req.getParameter(ACTION);
    String id = req.getParameter(ID);

    if (actionString == null || id == null) {
      writeResponse(res, "Please specify action and/or id.", HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Action action;
    try {
      action = Action.valueOf(actionString);
    } catch (IllegalArgumentException e) {
      writeResponse(res, "Illegal argument for action.", HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    switch (action) {
      case GENERATE_RSS_LINK:
        String rssLink = "https://launchpod-step18-2020.appspot.com/rss-feed?action=generateXml&id=" + id;
        res.setContentType("text/html");
        res.getWriter().println(rssLink);
        break;
      case GENERATE_XML:
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key entityKey = null;
        Entity desiredFeedEntity = null; 
        try {
          // Use key to retrieve entity from Datastore
          entityKey = KeyFactory.stringToKey(id);
          desiredFeedEntity = datastore.get(entityKey);
        } catch (IllegalArgumentException e) {
          // If entityId cannot be converted into a key
          writeResponse(res, "Sorry, this is not a valid id.", HttpServletResponse.SC_BAD_REQUEST);
          return;
        } catch (EntityNotFoundException e) {
          // No matching entity in Datastore
          writeResponse(res, "Your entity could not be found.", HttpServletResponse.SC_NOT_FOUND);
          return;
        }

        String podcastTitle = desiredFeedEntity.getProperty(PODCAST_TITLE).toString();
        String description = desiredFeedEntity.getProperty(DESCRIPTION).toString();
        String language = desiredFeedEntity.getProperty(LANGUAGE).toString();
        String email = desiredFeedEntity.getProperty(EMAIL).toString();

        EmbeddedEntity mp3Entity = (EmbeddedEntity) desiredFeedEntity.getProperty(MP3);
        String mp3Link = mp3Entity.getProperty(MP3_LINK).toString();

        RSS rssFeed = new RSS(podcastTitle, description, language, email, mp3Link);

        String xmlString = RSS.toXmlString(rssFeed);
        res.setContentType("text/xml");
        res.getWriter().println(xmlString);
        break;
      default: 
        writeResponse(res, "Sorry, this is not a valid action.", HttpServletResponse.SC_BAD_REQUEST);
    }
  }
}
