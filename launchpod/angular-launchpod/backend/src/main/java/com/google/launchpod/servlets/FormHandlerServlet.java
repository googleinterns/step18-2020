package com.google.launchpod.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.UserFeed;
import com.google.launchpod.data.MP3;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.PostPolicyV4;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@WebServlet("/rss-feed")
@MultipartConfig
public class FormHandlerServlet extends HttpServlet {

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
  public static final String ENTITY_ID = "entityId";
  public static final Gson GSON = new Gson();

  private static final long serialVersionUID = 1L;
  private static final String ID = "id";

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

    if (podcastTitle == null || podcastTitle.isEmpty()) {
      throw new IllegalArgumentException("No Title inputted, please try again.");
    } else if (description == null || description.isEmpty()) {
      throw new IllegalArgumentException("No description inputted, please try again.");
    } else if (language == null || language.isEmpty()) {
      throw new IllegalArgumentException("No language inputted, please try again.");
    } else if (email == null || email.isEmpty()) {
      throw new IllegalArgumentException("You are not logged in. Please try again.");
    }

    //Create entity with all desired attributes
    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    // set other properties

    // Generate xml string
    String xmlString = "";
    try {
      xmlString = xmlString(userFeedEntity);
      userFeedEntity.setProperty(XML_STRING ,xmlString);
    } catch(IOException e){
      throw new IOException("Unable to create XML string.");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);

    Key entityKey = userFeedEntity.getKey();
    String entityId = KeyFactory.keyToString(entityKey);
    String mp3Link = "https://storage.googleapis.com/" + BUCKET_NAME + "/" + entityId;
    // MP3 mp3 = new MP3(entityId, email);
    EmbeddedEntity mp3 = new EmbeddedEntity();
    mp3.setProperty(ENTITY_ID, entityId);
    mp3.setProperty(MP3_LINK, mp3Link);
    mp3.setProperty(EMAIL, email);

    // update entity by adding embedded MP3 entity as a property
    userFeedEntity.setProperty(MP3, mp3);

    // userFeedEntity = Entity.newBuilder(datastore.get(entityKey)).set(MP3, mp3);
    // userFeedEntity = Entity.newBuilder(datastore.get(entityKey)).set(MP3_LINK, mp3Link);
    datastore.put(userFeedEntity);

    //write the file upload form
    String formHtml = generateSignedPostPolicyV4(PROJECT_ID, BUCKET_NAME, entityId);
    res.getWriter().println(formHtml);
  }

  public  String generateSignedPostPolicyV4(String projectId, String bucketName, String blobName) {
    // The name to give the object uploaded to GCS
    // String blobName = "your-object-name", the Datastore entity ID of that MP3 object

    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

    PostPolicyV4.PostFieldsV4 fields =
        PostPolicyV4.PostFieldsV4.newBuilder().AddCustomMetadataField("test", "data").build();

    PostPolicyV4 policy =
        storage.generateSignedPostPolicyV4(
            BlobInfo.newBuilder(bucketName, blobName).build(), 10, TimeUnit.MINUTES, fields);

    StringBuilder htmlForm =
        new StringBuilder(
            "<form action='"
                + policy.getUrl()
                + "' method='POST' enctype='multipart/form-data'>\n");
    for (Map.Entry<String, String> entry : policy.getFields().entrySet()) {
      htmlForm.append(
          "  <input name='"
              + entry.getKey()
              + "' value='"
              + entry.getValue()
              + "' type='hidden' />\n");
    }
    htmlForm.append("  <input type='file' name='file'/><br />\n");
    String myRedirectUrl="https://launchpod-step18-2020.appspot.com/rss-feed/?action=generateRssLink&id=" + blobName;
    htmlForm.append(" <input name='success-action-redirect' value=" + myRedirectUrl + "/>\n");
    htmlForm.append("  <input type='submit' value='Upload File' name='submit'/><br />\n");
    htmlForm.append("</form>\n");

    return htmlForm.toString();
  }

  /**
  * Display RSS feed xml string that user tries recalling with the given ID
  */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String action = req.getParameter("action");
    String id = req.getParameter(ID);
    Key urlID = KeyFactory.stringToKey(id);

    if (action==null || action.isEmpty() || action.equals("generateXml")) {
      // Search key in datastore
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      // create entity that contains id from datastore
      try {
        Entity desiredFeedEntity = datastore.get(urlID);

        String podcastTitle = desiredFeedEntity.getProperty(PODCAST_TITLE).toString();
        String description = desiredFeedEntity.getProperty(DESCRIPTION).toString();
        String language = desiredFeedEntity.getProperty(LANGUAGE).toString();
        String email = desiredFeedEntity.getProperty(EMAIL).toString();
        String mp3Link = desiredFeedEntity.getProperty(MP3_LINK).toString();

        RSS rssFeed = new RSS(podcastTitle, description, language, email, mp3Link);
        
        //Create user feed object to create XML String
        UserFeed desiredUserFeed = UserFeed.fromEntity(desiredFeedEntity);

        XmlMapper xmlMapper = new XmlMapper();
        String xmlString = xmlMapper.writeValueAsString(rssFeed);
        res.setContentType("text/xml");
        res.getWriter().println(xmlString);

      // If there is no entity that matches the key
      } catch (EntityNotFoundException e) {
        e.printStackTrace();
        res.setContentType("text/html");
        res.getWriter().println("<p>Sorry. This is not a valid link.</p>");
        return;
      }
   } else if (action.equals("generateRSSLink")) {
      String rssLink = "https://launchpod-step18-2020.appspot.com/rss-feed?action=generateXml&id=" + urlID;
      res.setContentType("text/html");
      res.getWriter().println(rssLink);
   }
  }

  /**
   * Create RSS XML string from given fields
   *
   * @return xml String
   * @throws IOException
   * @throws Exception
   */
  private static String xmlString(Entity userFeedEntity) throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    String xmlString = xmlMapper.writeValueAsString(UserFeed.fromEntity(userFeedEntity));
    return xmlString;
  }
}
