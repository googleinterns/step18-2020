package com.google.launchpod.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.launchpod.data.RSS;

@WebServlet("/rss-feed")
public class FormHandlerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public static final String USER_FEED = "UserFeed";
  public static final String PODCAST_TITLE = "title";
  public static final String DESCRIPTION = "description";
  public static final String LANGUAGE = "language";
  public static final String EMAIL = "email";
  public static final String MP3_LINK = "mp3Link";
  public static final String XML_STRING = "xmlString";

  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final String ID = "id";

  /**
   * request user inputs in form fields then create Entity and place in datastore
   * 
   * @throws IOException,IllegalArgumentException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException, IOException {
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String description = req.getParameter(DESCRIPTION);
    String language = req.getParameter(LANGUAGE);
    String email = req.getParameter(EMAIL);
    String mp3Link = req.getParameter(MP3_LINK);

    if (podcastTitle == null || podcastTitle.isEmpty()) {
      throw new IllegalArgumentException("No Title inputted, please try again.");
    } else if (description == null || description.isEmpty()) {
      throw new IllegalArgumentException("No description inputted, please try again.");
    } else if (language == null || language.isEmpty()) {
      throw new IllegalArgumentException("No language inputted, please try again.");
    } else if (email == null || email.isEmpty()) {
      throw new IllegalArgumentException("You are not logged in. Please try again.");
    } else if (mp3Link == null || mp3Link.isEmpty()){
      throw new IllegalArgumentException("No MP3 link inputted, pllease try again");
    }

    // Create entity with all desired attributes
    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(EMAIL, email);

    // Generate xml string
    RSS rssFeed = new RSS(podcastTitle, description, language, email, mp3Link);
    try {
      String xmlString = RSS.toXmlString(rssFeed);
      userFeedEntity.setProperty(XML_STRING, xmlString);
    } catch (IOException e) {
      throw new IOException("Unable to create XML string.");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);

    // return accessible link to user
    String urlID = KeyFactory.keyToString(userFeedEntity.getKey());
    String rssLink = BASE_URL + urlID;
    res.setContentType("text/html");
    res.getWriter().println(rssLink);
  }

  /**
   * Display RSS feed xml string that user tries recalling with the given ID
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
      res.getWriter().print(xmlString);

      // If there is no entity that matches the key
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      res.setContentType("text/html");
      res.getWriter().println("<p>Sorry. This is not a valid link.</p>");
      return;
    }
  }
}
