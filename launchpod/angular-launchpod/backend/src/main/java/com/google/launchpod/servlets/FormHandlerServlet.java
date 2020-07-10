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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.UserFeed;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.PostPolicyV4;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;


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
    //String mp3Link = req.getParameter(MP3LINK); //storageapis.com/bucket_name/object_name

    //


    if((podcastTitle.isEmpty() || podcastTitle == null) || (mp3Link.isEmpty() || mp3Link == null)){
      throw new IOException("No Title or MP3 link inputted, please try again.");
    }

    //Create entity with all desired attributes
    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    userFeedEntity.setProperty(MP3LINK, mp3Link);
    // TO-DO: see if you can do .setProperty(String, Object)
    // set property to be 'mp3' to the mp3 object

    //

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
    String rssLink = "https://launchpod-step18-2020.appspot.com/rss-feed?action=generateXml&id=" + urlID;
    res.setContentType("text/html");
    res.getWriter().println(rssLink);

    //write the file upload form
    String formHtml = generateSignedPostPolicyV4(projectId, bucketName, blobName);
    res.getWriter().println(formHtml);

  }

  public  String generateSignedPostPolicyV4(String projectId, String bucketName, String blobName) {
    // The ID of your GCP project
    // String projectId = "launchpod-step18-2020";

    // The ID of the GCS bucket to upload to
    // String bucketName = "launchpod-mp3-files"

    // The name to give the object uploaded to GCS
    // String blobName = "your-object-name"
    // this should be the Datastore entity ID of that MP3 object

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
    String myRedirectUrl="xxxx/?action=generateRssLink";
    htmlForm.append(" <input name='successxxx-redirect-url' value=" + myRedirectUrl + "/>\n");
    htmlForm.append("  <input type='submit' value='Upload File' name='submit'/><br />\n");
    htmlForm.append("</form>\n");

    System.out.println(
        "You can use the following HTML form to upload an object to bucket "
            + bucketName
            + " for the next ten minutes:");
    System.out.println(htmlForm.toString());

    return htmlForm.toString();
  }

  /**
  * Display RSS feed xml string that user tries recalling  with the given ID
  */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String action = req.getParameter("action");

    if(action==null || action.isEmpty() || action.equals("generateXml")) {
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
   } else if (action.equals("generateRSSLink")) {
    // get desiredFeedEntity
    // generate RSS link like in doPost
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
