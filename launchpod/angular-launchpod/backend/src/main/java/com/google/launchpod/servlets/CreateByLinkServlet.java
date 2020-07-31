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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.Channel;

@WebServlet("/create-by-link")
public class CreateByLinkServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String USER_FEED = "UserFeed";
  private static final String EPISODE_TITLE = "episodeTitle";
  private static final String EPISODE_DESCRIPTION = "episodeDescription";
  private static final String EPISODE_LANGUAGE = "episodeLanguage";
  private static final String MP3_LINK = "mp3Link";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final String ID = "id";
  private static final XmlMapper XML_MAPPER = new XmlMapper();
  // public variable to allow creation of UserFeed objects
  public static final String XML_STRING = "xmlString";

  /**
   * Requests user inputs in form fields, then creates Entity and places in Datastore.
   *
   * @throws IOException,IllegalArgumentException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException, IOException {
    String episodeTitle = req.getParameter(EPISODE_TITLE);
    String episodeDescription = req.getParameter(EPISODE_DESCRIPTION);
    String episodeLanguage = req.getParameter(EPISODE_LANGUAGE);
    String mp3Link = req.getParameter(MP3_LINK);
    String entityId = req.getParameter(ID);

    UserService userService = UserServiceFactory.getUserService();
    String email = "";
    if (userService.isUserLoggedIn()) {
      email = userService.getCurrentUser().getEmail();
    }

    if (episodeTitle == null || episodeTitle.isEmpty()) {
      throw new IllegalArgumentException("No episode title inputted, please try again.");
    } else if (episodeDescription == null || episodeDescription.isEmpty()) {
      throw new IllegalArgumentException("No episode description inputted, please try again.");
    } else if (episodeLanguage == null || episodeLanguage.isEmpty()) {
      throw new IllegalArgumentException("No episode language inputted, please try again.");
    } else if (mp3Link == null || mp3Link.isEmpty()) {
      throw new IllegalArgumentException("No mp3 link inputted, please try again.");
    }

    // // Creates entity with all desired attributes
    // Entity userFeedEntity = new Entity(USER_FEED);
    // // Generate xml string
    // RSS rssFeed = new RSS(podcastTitle, mp3Link);

    // try {
    //   String xmlString = RSS.toXmlString(rssFeed);
    //   userFeedEntity.setProperty(XML_STRING, xmlString);
    // } catch (IOException e) {
    //   throw new IOException("Unable to create XML string.");
    // }

    // retrieve entity associated with id
    Key entityKey = KeyFactory.stringToKey(entityId);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity desiredFeedEntity;
    try {
      desiredFeedEntity = datastore.get(entityKey);
    } catch (EntityNotFoundException e) {
      res.setContentType("text/html");
      res.getWriter().print("<p>Sorry, entity does not exist in Datastore.</p>"); // to-do: come back to this
      return;
    }

    String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);

    // modify the xml string
    RSS rssFeed = XML_MAPPER.readValue(xmlString, RSS.class);
    Channel channel = rssFeed.getChannel();
    channel.addItem(channel, episodeTitle, episodeDescription, episodeLanguage, email, mp3Link); // to-do: double check this
    String modifiedXmlString = RSS.toXmlString(rssFeed);
    desiredFeedEntity.setProperty(XML_STRING, modifiedXmlString);
    datastore.put(desiredFeedEntity);

    // return accessible link to client
    String urlID = KeyFactory.keyToString(desiredFeedEntity.getKey()); // the key string associated with the entity, not the numeric ID.
    String rssLink = BASE_URL + urlID;
    res.setContentType("text/html");
    res.getWriter().print(rssLink);
  }

  /**
   * Display RSS feed xml string that user tries recalling with the given ID.
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // Get ID passed in request
    String id = req.getParameter(ID);
    if (id == null) {
      throw new IllegalArgumentException("Sorry, no matching Id was found in Datastore.");
    }
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
      res.getWriter().print("<p>Sorry. This is not a valid link.</p>");
      return;
    }
  }
}
