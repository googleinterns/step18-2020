package com.google.launchpod.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.launchpod.data.UserFeed;
import com.google.appengine.api.datastore.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
   *  request user inputs in form fields then create Entity and place in datastore
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String mp3Link = req.getParameter(MP3LINK);
    long timestamp = System.currentTimeMillis();
    //Create time
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime publishTime = LocalDateTime.now();
    //Generate xml string
    String xmlString = xmlString(podcastTitle, mp3Link, dateFormatter.format(publishTime));

    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    userFeedEntity.setProperty(EMAIL, "123@example.com");
    userFeedEntity.setProperty(MP3LINK, mp3Link);
    userFeedEntity.setProperty(TIMESTAMP, timestamp);
    userFeedEntity.setProperty(XML_STRING ,xmlString);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);
  }

  /** Create XML string from given fields
   *  @return 
   */
  private static String xmlString(String title, String mp3Link, String pubDate){
    //Set Default Values to title and mp3 if they are event null or empty
    if (title.isEmpty() || title == null){
      title = "Podcast";
    }
    if (mp3Link.isEmpty() || mp3Link== null){
      mp3Link = "None Listed";
    }
    String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  +
                       "<rss version=\"2.0\">" + 
                       "  <channel>" +
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
    Query query = new Query("UserFeed").addSort(TIMESTAMP, SortDirection.ASCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<UserFeed> userFeeds = new ArrayList<>();
    for(Entity entity: results.asIterable()){
      if(entity.getProperty(EMAIL) == "123@example.com"){ // user log in info here
        userFeeds.add(UserFeed.fromEntity(entity));
      }
    }

    res.setContentType("text/html");
    //display all xml strings related to user logged in
    for (UserFeed userfeed: userFeeds){
      res.getWriter().println("<div><p>+" + userfeed.xmlString + "</p></div>");
    }
  }
}
