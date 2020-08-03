package com.google.launchpod.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.common.base.Strings;
import com.google.launchpod.data.ItunesOwner;
import com.google.launchpod.data.ItunesCategory;
import com.google.launchpod.data.Item;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.LoginStatus;
import com.google.launchpod.data.UserFeed;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;

@WebServlet("/translate-feed")
public class TranslationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String USER_FEED = "UserFeed";
  private static final String RSS_FEED_LINK = "rssFeedLink";
  private static final String TITLE = "title";
  private static final String LANGUAGE = "language";
  private static final String USER_NAME = "name";
  private static final String USER_EMAIL = "email";
  private static final String TIMESTAMP = "timestamp";
  private static final String POST_TIME = "postTime";
  private static final String CATEGORY = "category";
  private static final String DESCRIPTION = "description";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final String ID = "id";
  private static final String XML_STRING = "xmlString";
  public static final XmlMapper XML_MAPPER = new XmlMapper();
  private static final Gson GSON = new Gson();

  static {
    XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws JsonParseException, JsonMappingException, IOException {
    UserService userService = UserServiceFactory.getUserService();

    String link = req.getParameter(RSS_FEED_LINK);
    String targetLanguage = req.getParameter(LANGUAGE);
    String email = userService.getCurrentUser().getEmail();
    long timestamp = System.currentTimeMillis();

    if (Strings.isNullOrEmpty(link)) {
      throw new IllegalArgumentException("Please give valid link.");
    } else if (Strings.isNullOrEmpty(targetLanguage)) {
      throw new IllegalArgumentException("Please give valid language.");
    } else if (Strings.isNullOrEmpty(email)) {
      throw new IllegalArgumentException("Please log in to access the feed.");
    }

    // get ID from parameter and turn into key
    String id = getIdFromUrl(link);
    Key desiredFeedKey = KeyFactory.stringToKey(id);

    // Search for key from given Link
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity desiredFeedEntity = null;
    try {
      desiredFeedEntity = datastore.get(desiredFeedKey);
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to find given URL key, Please try again");
    }
    String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);

    RSS rssFeed = null;
    try {
      rssFeed = XML_MAPPER.readValue(xmlString, RSS.class);
    } catch (Exception e) {
      res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to translate. Try again");
    }

    String sourceLanguage = rssFeed.getChannel().getLanguage();
    // Translate all the fields
    // Channel title
    rssFeed.getChannel().setTitle(translateText(sourceLanguage, targetLanguage, rssFeed.getChannel().getTitle()));

    // Channel description
    rssFeed.getChannel()
        .setDescription(translateText(sourceLanguage, targetLanguage, rssFeed.getChannel().getDescription()));

    // Language
    rssFeed.getChannel().setLanguage(targetLanguage);

    // Category
    for (ItunesCategory category : rssFeed.getChannel().getItunesCategory()) {
      category.setText(translateText(sourceLanguage, targetLanguage, category.getText()));
    }

    // Episodes
    if (rssFeed.getChannel().getItems() != null) {
      for (Item item : rssFeed.getChannel().getItems()) {
        // Episode title
        if (!Strings.isNullOrEmpty(item.getTitle())) {
          item.setTitle(translateText(sourceLanguage, targetLanguage, item.getTitle()));
        }

        // Episode description
        if (!Strings.isNullOrEmpty(item.getDescription())) {
          item.setDescription(translateText(sourceLanguage, targetLanguage, item.getDescription()));
        }

        // Episode Language
        if (!Strings.isNullOrEmpty(item.getLanguage())) {
          item.setLanguage(targetLanguage);
        }
      }
    }

    // Generate Translated XML string then place it into datastore
    String translatedXmlString = RSS.toXmlString(rssFeed);
    Entity translatedUserFeedEntity = new Entity(USER_FEED);
    translatedUserFeedEntity.setProperty(TITLE, rssFeed.getChannel().getTitle());
    translatedUserFeedEntity.setProperty(USER_NAME, rssFeed.getChannel().getAuthor());
    translatedUserFeedEntity.setProperty(USER_EMAIL, email);
    translatedUserFeedEntity.setProperty(TIMESTAMP, timestamp);
    translatedUserFeedEntity.setProperty(DESCRIPTION, rssFeed.getChannel().getDescription());
    translatedUserFeedEntity.setProperty(LANGUAGE, targetLanguage);
    translatedUserFeedEntity.setProperty(XML_STRING, translatedXmlString);
    datastore.put(translatedUserFeedEntity);

    Query query = new Query(LoginStatus.USER_FEED_KEY).addSort(LoginStatus.TIMESTAMP_KEY, SortDirection.DESCENDING);

    PreparedQuery results = datastore.prepare(query);

    ArrayList<UserFeed> userFeeds = new ArrayList<UserFeed>();
    for (Entity entity : results.asIterable()) {
      if (email.equals(entity.getProperty(USER_EMAIL).toString())) {
        String userFeedTitle = (String) entity.getProperty(LoginStatus.TITLE_KEY);
        String userFeedName = (String) entity.getProperty(LoginStatus.NAME_KEY);
        String userFeedDescription = (String) entity.getProperty(LoginStatus.DESCRIPTION_KEY);
        String userFeedLanguage = (String) entity.getProperty(LoginStatus.LANGUAGE_KEY);
        String userFeedEmail = (String) entity.getProperty(LoginStatus.EMAIL_KEY);
        long userFeedTimestamp = (long) entity.getProperty(LoginStatus.TIMESTAMP_KEY);
        Date date = new Date(userFeedTimestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss Z", Locale.getDefault());
        String postTime = dateFormat.format(date);
        Key key = entity.getKey();

        String urlID = KeyFactory.keyToString(entity.getKey()); // the key string associated with the entity, not the
                                                                // numeric ID.
        String rssLink = BASE_URL + urlID;

        userFeeds.add(new UserFeed(userFeedTitle, userFeedName, rssLink, userFeedDescription, userFeedEmail, postTime,
            urlID, userFeedLanguage));
      }
    }

    res.setContentType("application/json");
    res.getWriter().println(GSON.toJson(userFeeds));
  }

  /**
   * Translate the text using the translation API request and response client
   */
  public static String translateText(String sourceLanguage, String targetLanguage, String text) throws IOException {
    String projectId = "launchpod-step18-2020";
    String location = "global";
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      LocationName parent = LocationName.of(projectId, location);

      TranslateTextRequest request = TranslateTextRequest.newBuilder().setParent(parent.toString())
          .setMimeType("text/html").setTargetLanguageCode(targetLanguage).addContents(text).build();

      TranslateTextResponse response = client.translateText(request);
      request.toBuilder().clearContents();
      return response.getTranslations(0).getTranslatedText();
    }
  }

  public static String getIdFromUrl(String rssLink) throws MalformedURLException, IllegalArgumentException {
    // Get the ID from the link that is being pasted
    URL feedUrl = new URL(rssLink);
    String queryString = feedUrl.getQuery();
    String[] querySplit = queryString.split("=");
    if (querySplit.length <= 1) {
      throw new IllegalArgumentException("URL is not valid for translation");
    }
    String id = querySplit[1];
    return id;
  }
}
