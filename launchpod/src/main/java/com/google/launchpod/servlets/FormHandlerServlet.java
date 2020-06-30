package com.google.launchpod.servlets;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.launchpod.data.UserFeed;

@WebServlet("/rss-feed")
public class FormHandlerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public static final String USER_FEED = "UserFeed";
  public static final String PODCAST_TITLE = "title";
  public static final String EMAIL = "email";
  public static final String TIMESTAMP = "timestamp";
  public static final String MP3LINK = "mp3link";
  public static final String XML_STRING = "xmlString";

  public static final Gson GSON = new Gson();

  /**
   * request user inputs in form fields then create Entity and place in datastore
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String mp3Link = req.getParameter(MP3LINK);
    if((podcastTitle.isEmpty() || podcastTitle == null) || (mp3Link.isEmpty() || mp3Link == null)){
      throw new IOException("No Title or MP3 link inputted, please try again.");
    }
    long timestamp = System.currentTimeMillis();
    // Create time
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime publishTime = LocalDateTime.now();

    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    userFeedEntity.setProperty(EMAIL, "123@example.com");
    userFeedEntity.setProperty(MP3LINK, mp3Link);
    userFeedEntity.setProperty(TIMESTAMP, timestamp);
    String ID = KeyFactory.keyToString(userFeedEntity.getKey());

    // Generate xml string
    String xmlString = "";
    try{
      xmlString = xmlString(podcastTitle, mp3Link, dateFormatter.format(publishTime), ID);
      userFeedEntity.setProperty(XML_STRING ,xmlString);
    }catch(IOException e){
      e.printStackTrace();
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);
  }

  /**
   * Create RSS XML string from given fields
   * 
   * @return xml String
   * @throws IOException
   * @throws Exception
   */
  private static String xmlString(String title, String mp3Link, String pubDate, String ID) throws IOException {
    //Set Default Values to title and mp3 if they are event null or empty
    if (title.isEmpty() || title == null){
      throw new IOException("Title field is empty, try again.");
    }
    if (mp3Link.isEmpty() || mp3Link== null){
      throw new IOException("MP3 Link field is empty, try again.");
    }
    String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  +
                       "<rss version=\"2.0\">" + 
                       "  <channel>" +
                       "    <link>https://launchpod-step18-2020.appspot.com/?id=" + ID + "</link>" +
                       "    <language>en</language>"  +
                       "    <itunes:author>User</itunes:author>" + 
                       "    <title>" + title + "</title>" + 
                       "    <item>" +
                       "      <title>" + title + "</title>" + 
                       "      <summary>This is a Test</summary>" + 
                       "      <description>This is a Test</description>" + 
                       "      <link>" + mp3Link +"</link>" +
                       "      <enclosure url=\"" + mp3Link + "\" type=\"audio/mpeg\" length=\"185000\"/>" +
                       "      <pubDate>" + pubDate + "</pubDate>" +
                       "      <itunes:author/>" + 
                       "      <itunes:duration>03:05</itunes:duration>" + 
                       "      <itunes:explicit>No</itunes:explicit>" + 
                       "      <guid isPermaLink=\"false\">uhwefpoihEOUUHSFEOIwqkhdho-=</guid>" + 
                       "    </item>" + 
                       "  </channel>" + 
                       "</rss>";

    return xmlString;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setContentType("text/html");
    Query query = new Query("UserFeed").addSort(TIMESTAMP, SortDirection.ASCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<UserFeed> userFeeds = new ArrayList<>();
    List<String> userURLs = new ArrayList<>();
    for(Entity entity: results.asIterable()){
      if(entity.getProperty(EMAIL) == "123@example.com"){ // user log in info here
        userFeeds.add(UserFeed.fromEntity(entity));
        userURLs.add(KeyFactory.keyToString(entity.getKey()));
      }
    }
    //check edge case if there happens to be no RSS feeds belonging to that specific user then display error message.
    if(userFeeds.isEmpty()){
      res.getWriter().println("<p>You have not created any RSS feeds</p>");
      return;
    }
    //display all urls related to user logged in
    for (int i = 0; i < userURLs.size(); i++){
      res.getWriter().println("<div>");
      res.getWriter().println("<p>" + userFeeds.get(i).podcastTitle + 
      "URL: <a href=\"https://launchpod-step18-2020.appspot.com/?id=" + userURLs.get(i) + "\">" + userURLs.get(i) + "</a></p>");
      res.getWriter().println("</div>");
    }
  }
}
