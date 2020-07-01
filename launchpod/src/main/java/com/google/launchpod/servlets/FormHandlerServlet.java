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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
  public static final String PUB_DATE= "pubDate";

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
    String pubDate = dateFormatter.format(publishTime);

    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    userFeedEntity.setProperty(EMAIL, "123@example.com");
    userFeedEntity.setProperty(MP3LINK, mp3Link);
    userFeedEntity.setProperty(TIMESTAMP, timestamp);
    userFeedEntity.setProperty(PUB_DATE, pubDate);

    // Generate xml string
    String xmlString = "";
    try{
      xmlString = xmlString(userFeedEntity);
      userFeedEntity.setProperty(XML_STRING ,xmlString);
    }catch(IOException e){
      e.printStackTrace();
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);

    //return accessible link to user 
    String ID = Long.toString(userFeedEntity.getKey().getId());
    String rssLink = "https://launchpod-step18-2020.appspot.com?id=" + ID;
    res.setContentType("text/html");
    res.getWriter().println("<a href=\"" + rssLink + "\">" + rssLink + "</a>");
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
