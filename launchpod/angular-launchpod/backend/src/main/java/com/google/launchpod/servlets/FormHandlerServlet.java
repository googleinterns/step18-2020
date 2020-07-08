package com.google.launchpod.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.UserFeed;

@WebServlet("/rss-feed")
@MultipartConfig
public class FormHandlerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public static final String USER_FEED = "UserFeed";
  public static final String PODCAST_TITLE = "title";
  public static final String EMAIL = "email";
  public static final String TIMESTAMP = "timestamp";
  public static final String MP3LINK = "mp3Link";
  public static final String MP3FILE = "mp3File";
  public static final String XML_STRING = "xmlString";
  public static final String PUB_DATE = "pubDate";

  private static final String ID = "id";

  public static final Gson GSON = new Gson();
  // public static boolean bucketCreated = false;
  // TO-DO: check in doPost if bucketCreated == true, if so upload object, if not
  // create bucket

  /**
   * request user inputs in form fields then create Entity and place in datastore
   * 
   * @throws ServletException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String mp3Link = req.getParameter(MP3LINK);
    Part mp3FilePart;
    try {
      mp3FilePart = req.getPart(MP3FILE);
    } catch (ServletException e1) {
      throw new ServletException("Unable to retrieve MP3 file from client.");
    } // Retrieves <input type="mp3File" name="mp3File">
    String mp3FileName = Paths.get(mp3FilePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
    InputStream fileContent = mp3FilePart.getInputStream();
    // ... (do your job here)

    if((podcastTitle.isEmpty() || podcastTitle == null) || (mp3Link.isEmpty() || mp3Link == null)){
      throw new IOException("No Title or MP3 link inputted, please try again.");
    }

    //Create entity with all desired attributes
    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    userFeedEntity.setProperty(MP3LINK, mp3Link);

    // Generate xml string
    String xmlString = "";
    try{
      xmlString = xmlString(userFeedEntity);
      userFeedEntity.setProperty(XML_STRING ,xmlString);
    }catch(IOException e){
      throw new IOException("Unable to create XML string.");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);

    //return accessible link to user 
    String urlID = KeyFactory.keyToString(userFeedEntity.getKey());
    String rssLink = "https://launchpod-step18-2020.appspot.com/rss-feed?id=" + urlID;
    res.setContentType("text/html");
    res.getWriter().println(rssLink);
  }

  /**
   * Uploads an object to Cloud Storage.
   * @throws IOException
   */
  public static void uploadObject(
      String projectId, String bucketName, String objectName, String filePath) throws IOException {
    // The ID of your GCP project
    // String projectId = "launchpod-step18-2020";

    // The ID of your GCS bucket
    // String bucketName = "launchpod-mp3-files";

    // The ID of your GCS object
    // String objectName = "your-object-name";

    // The path to your file to upload
    // String filePath = "path/to/your/file"

    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    BlobId blobId = BlobId.of(bucketName, objectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

    System.out.println(
        "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
  }

  /**
  * Display RSS feed xml string that user tries recalling  with the given ID
  */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // Get ID passed in request
    String id = req.getParameter(ID);
    Key urlID = KeyFactory.stringToKey(id);
    // Search key in datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // create entity that contains id from datastore
    try {
      Entity desiredFeedEntity = datastore.get(urlID);
      
      //Create user feed object to access rss feed attributes then create RSS feed
      UserFeed desiredUserFeed = UserFeed.fromEntity(desiredFeedEntity);
      RSS rssFeed = new RSS(desiredUserFeed.getTitle(), desiredUserFeed.getLink(), desiredUserFeed.getPubDate());

      // generate xml string
      XmlMapper xmlMapper = new XmlMapper();
      String xmlString = xmlMapper.writeValueAsString(rssFeed);
      res.setContentType("text/xml");
      res.getWriter().println(xmlString);

    //If there is no entity that matches the key
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      res.setContentType("text/html");
      res.getWriter().println("<p>Sorry. This is not a valid link.</p>");
      return;
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
