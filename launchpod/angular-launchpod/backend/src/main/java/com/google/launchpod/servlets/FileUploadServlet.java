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
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
import com.google.launchpod.data.Channel;
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

  private static final String EPISODE_TITLE = "episodeTitle";
  private static final String EPISODE_DESCRIPTION = "episodeDescription";
  private static final String EPISODE_LANGUAGE = "episodeLanguage";
  public static final String EMAIL = "email";
  public static final String TIMESTAMP = "timestamp";
  public static final String MP3 = "mp3";
  public static final String MP3_LINK = "mp3Link";
  public static final String XML_STRING = "xmlString";
  public static final String PUB_DATE = "pubDate";
  public static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/";
  public static final String LINK_TO_XML_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?action=generateXml&id=";
  public static final String HTML_FORM_END = "  <input type='file' name='file'/><br />\n"
                                             + "<input type='submit' value='Upload File' name='submit'/><br />\n"
                                             + "</form>\n";

  private static final String ID = "id";
  private static final String ACTION = "action";
  private static final XmlMapper XML_MAPPER = new XmlMapper();

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
   * Requests user inputs in form fields, then updates XML associated with entity id in Datastore.
   * @throws ServletException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException, IOException {
    String episodeTitle = req.getParameter(EPISODE_TITLE);
    String episodeDescription = req.getParameter(EPISODE_DESCRIPTION);
    String episodeLanguage = req.getParameter(EPISODE_LANGUAGE);
    String id = req.getParameter(ID);

    UserService userService = UserServiceFactory.getUserService();
    String email = "";
    if (userService.isUserLoggedIn()) {
      email = userService.getCurrentUser().getEmail();
    }

    // TO-DO after merging: move validation to common place
    if (Strings.isNullOrEmpty(episodeTitle)) {
      throw new IllegalArgumentException("No episode title inputted, please try again.");
    } else if (Strings.isNullOrEmpty(episodeDescription)) {
      throw new IllegalArgumentException("No episode description inputted, please try again.");
    } else if (Strings.isNullOrEmpty(episodeLanguage)) {
      throw new IllegalArgumentException("No episode language inputted, please try again.");
    } else if (Strings.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Sorry, no entity Id could be found.");
    } else if (Strings.isNullOrEmpty(email)) {
      throw new IllegalArgumentException("You are not logged in. Please try again.");
    }

    Key entityKey = null;
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity desiredFeedEntity;
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
    String mp3Link = makeMp3Link(id);

    // Create embedded entity to store MP3 data in desired entity as a property
    EmbeddedEntity mp3 = new EmbeddedEntity();
    mp3.setProperty(ID, id);
    mp3.setProperty(MP3_LINK, mp3Link);
    mp3.setProperty(EMAIL, email);
    desiredFeedEntity.setProperty(MP3, mp3);

    String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);

    // Modify the xml string
    RSS rssFeed = XML_MAPPER.readValue(xmlString, RSS.class);
    Channel channel = rssFeed.getChannel();
      
    String entityEmail = (String) desiredFeedEntity.getProperty(EMAIL);

    // Verify that user is modifying a feed they created
    if (entityEmail.equals(email)) {
      channel.addItem(episodeTitle, episodeDescription, episodeLanguage, mp3Link);
    } else {
      throw new IOException("You are trying to edit a feed that's not yours!");
    }

    String modifiedXmlString = RSS.toXmlString(rssFeed);
    desiredFeedEntity.setProperty(XML_STRING, modifiedXmlString);
    datastore.put(desiredFeedEntity);

    // Write the file upload form
    String formHtml = generateSignedPostPolicyV4(PROJECT_ID, BUCKET_NAME, id);
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
      URL keyFile = classLoader.getResource(keyFileName);
      if (keyFile == null) {
        throw new IOException("API key was not found.");
      }
      File file = new File(keyFile.getFile());
      GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(file));

      storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

      myRedirectUrl = BASE_URL + "/my-feeds";
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
        String rssLink = LINK_TO_XML_URL + id;
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
        String xmlString = desiredFeedEntity.getProperty(XML_STRING).toString();
        res.setContentType("text/xml");
        res.getWriter().println(xmlString);
        break;
      default: 
        writeResponse(res, "Sorry, this is not a valid action.", HttpServletResponse.SC_BAD_REQUEST);
    }
  }
}
