package com.google.launchpod.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.launchpod.data.RSS;

@WebServlet("/rss-feed")
public class FormHandlerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public static final String USER_FEED = "UserFeed";
  public static final String PODCAST_TITLE = "title";
  public static final String EMAIL = "email";
  public static final String MP3LINK = "mp3Link";
  public static final String XML_STRING = "xmlString";


  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final String ID = "id";

  public static final Gson GSON = new Gson();

  /**
   * request user inputs in form fields then create Entity and place in datastore
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String mp3Link = req.getParameter(MP3LINK);
    if(podcastTitle.isEmpty() || podcastTitle == null){
      throw new IOException("No Title inputted, please try again.");
    } else if (mp3Link.isEmpty() || mp3Link == null){
      throw new IOException("No Mp3 inputted, please try again.");
    }

    //Create entity with all desired attributes
    Entity userFeedEntity = new Entity(USER_FEED);

    // Generate xml string
    RSS rssFeed = new RSS(podcastTitle, mp3Link);
    try{
      String xmlString = xmlString(rssFeed);
      userFeedEntity.setProperty(XML_STRING ,xmlString);
    }catch(IOException e){
      throw new IOException("Unable to create XML string.");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);

    //return accessible link to user 
    String urlID = KeyFactory.keyToString(userFeedEntity.getKey());
    String rssLink = BASE_URL + urlID;
    res.setContentType("text/html");
    res.getWriter().println(rssLink);
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
      
      // generate xml string
      String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);
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
  private static String xmlString(RSS rssFeed) throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    String xmlString = xmlMapper.writeValueAsString(rssFeed);
    return xmlString;
  }
}
