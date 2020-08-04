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
import com.google.common.base.Strings;

@WebServlet("/create-by-link")
public class CreateByLinkServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String USER_FEED = "UserFeed";
  private static final String EPISODE_TITLE = "episodeTitle";
  private static final String EPISODE_DESCRIPTION = "episodeDescription";
  private static final String EPISODE_LANGUAGE = "episodeLanguage";
  private static final String MP3_LINK = "mp3Link";
  private static final String EMAIL = "email";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final String ID = "id";
  private static final XmlMapper XML_MAPPER = new XmlMapper();
  // public variable to allow creation of UserFeed objects
  public static final String XML_STRING = "xmlString";

  /**
  * Helper method for repeated code in catching exceptions.
  */
  private static void writeResponse(HttpServletResponse res, String message, int statusCode) throws IOException {
    res.setContentType("text/html");
    res.getWriter().println(message);
    res.setStatus(statusCode);
  }

  /**
   * Requests user inputs in form fields, then creates Entity and places in Datastore.
   * @throws IOException,IllegalArgumentException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException, IOException {
    String episodeTitle = req.getParameter(EPISODE_TITLE);
    String episodeDescription = req.getParameter(EPISODE_DESCRIPTION);
    String episodeLanguage = req.getParameter(EPISODE_LANGUAGE);
    String mp3Link = req.getParameter(MP3_LINK);
    String id = req.getParameter(ID);

    UserService userService = UserServiceFactory.getUserService();
    String email = "";
    if (userService.isUserLoggedIn()) {
      email = userService.getCurrentUser().getEmail();
    }

    if (Strings.isNullOrEmpty(episodeTitle)) {
      throw new IllegalArgumentException("No episode title inputted, please try again.");
    } else if (Strings.isNullOrEmpty(episodeDescription)) {
      throw new IllegalArgumentException("No episode description inputted, please try again.");
    } else if (Strings.isNullOrEmpty(episodeLanguage)) {
      throw new IllegalArgumentException("No episode language inputted, please try again.");
    } else if (Strings.isNullOrEmpty(mp3Link)) {
      throw new IllegalArgumentException("No mp3 link inputted, please try again.");
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

    // Return accessible link to client
    String urlID = KeyFactory.keyToString(desiredFeedEntity.getKey()); // the key string associated with the entity, not the numeric ID.
    String rssLink = BASE_URL + urlID;
    res.setContentType("text/html");
    res.getWriter().print(rssLink);
  }
}
