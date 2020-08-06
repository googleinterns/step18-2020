package com.google.launchpod.servlets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.launchpod.data.Keys;
import com.google.launchpod.data.LoginStatus;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.UserFeed;

@WebServlet("/rss-feed")
public class FormHandlerServlet extends HttpServlet {

  private static final Gson GSON = new Gson();

  /**
   * Requests user inputs in form fields, then creates Entity and places in
   * Datastore.
   *
   * @throws IOException,IllegalArgumentException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException, IOException {
    UserService userService = UserServiceFactory.getUserService();

    String title = req.getParameter(Keys.TITLE);
    String name = req.getParameter(Keys.USER_NAME);
    String category = req.getParameter(Keys.CATEGORY);
    String description = req.getParameter(Keys.DESCRIPTION);
    String language = req.getParameter(Keys.LANGUAGE);
    String email = userService.getCurrentUser().getEmail();

    long timestamp = System.currentTimeMillis();

    if (title == null || title.isEmpty()) {
      throw new IllegalArgumentException("No Title inputted, please try again.");
    } else if (language == null || language.isEmpty()) {
      throw new IllegalArgumentException("No Language inputted, please try again.");
    } else if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("No Name inputted, please try again.");
    } else if (description == null || description.isEmpty()) {
      throw new IllegalArgumentException("No Description inputted, please try again.");
    } else if (category == null || category.isEmpty()) {
      throw new IllegalArgumentException("No Category inputted, please try again.");
    }

    // Creates entity with all desired attributes
    Entity userFeedEntity = new Entity(Keys.USER_FEED);

    userFeedEntity.setProperty(Keys.TITLE, title);
    userFeedEntity.setProperty(Keys.USER_NAME, name);
    userFeedEntity.setProperty(Keys.USER_EMAIL, email);
    userFeedEntity.setProperty(Keys.TIMESTAMP, timestamp);
    userFeedEntity.setProperty(Keys.DESCRIPTION, description);
    userFeedEntity.setProperty(Keys.LANGUAGE, language);

    // Generate xml string
    RSS rssFeed = new RSS(name, email, title, description, category, language);
    try {
      String xmlString = RSS.toXmlString(rssFeed);
      userFeedEntity.setProperty(Keys.XML_STRING, xmlString);
    } catch (IOException e) {
      throw new IOException("Unable to create XML string.");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);

    Query query = new Query(LoginStatus.USER_FEED_KEY).addSort(LoginStatus.TIMESTAMP_KEY, SortDirection.DESCENDING);

    PreparedQuery results = datastore.prepare(query);

    ArrayList<UserFeed> userFeeds = new ArrayList<UserFeed>();
    for (Entity entity : results.asIterable()) {
      if (email.equals(entity.getProperty(Keys.USER_EMAIL).toString())) {
        String userFeedTitle = (String) entity.getProperty(LoginStatus.TITLE_KEY);
        String userFeedName = (String) entity.getProperty(LoginStatus.NAME_KEY);
        String userFeedDescription = (String) entity.getProperty(LoginStatus.DESCRIPTION_KEY);
        String userFeedLanguage = (String) entity.getProperty(LoginStatus.LANGUAGE_KEY);
        long userFeedTimestamp = (long) entity.getProperty(LoginStatus.TIMESTAMP_KEY);
        Date date = new Date(userFeedTimestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss Z", Locale.getDefault());
        String postTime = dateFormat.format(date);
        Key key = entity.getKey();
        
        String urlID = KeyFactory.keyToString(entity.getKey()); // the key string associated with the entity, not the numeric ID.
        String rssLink = Keys.BASE_URL + urlID;

        userFeeds.add(new UserFeed(userFeedTitle, userFeedName, rssLink, userFeedDescription, email, postTime, urlID, userFeedLanguage));
      }
    }

    res.setContentType("application/json");
    res.getWriter().println(GSON.toJson(userFeeds));
  }

  /**
   * Display RSS feed xml string that user tries recalling with the given ID.
   * 
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // Get ID passed in request
    String id = req.getParameter(Keys.ID);
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
      String xmlString = (String) desiredFeedEntity.getProperty(Keys.XML_STRING);
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
