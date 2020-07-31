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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.launchpod.data.Item;
import com.google.launchpod.data.RSS;

@WebServlet("/translate-feed")
public class TranslationServlet extends HttpServlet {

    //https://launchpod-step18-2020.appspot.com/rss-feed?id=ahdzfmxhdW5jaHBvZC1zdGVwMTgtMjAyMHIVCxIIVXNlckZlZWQYgICAmKXQhgoM

  private static final long serialVersionUID = 1L;
  private static final String USER_FEED = "UserFeed";
  private static final String RSS_FEED_LINK = "rssFeedLink";
  private static final String LANGUAGE = "language";
  private static final String XML_STRING = "xmlString";
  private static final XmlMapper XML_MAPPER = new XmlMapper();
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws JsonParseException, JsonMappingException, IOException {
    String link = req.getParameter(RSS_FEED_LINK);
    String targetLanguage = req.getParameter(LANGUAGE);
    if (link == null || link == "") {
      throw new IOException("Please give valid link.");
    }
    if (targetLanguage == null || targetLanguage == "") {
      throw new IOException("Please give valid language.");
    }

    //get ID from parameter and turn into key
    String id = getIdFromUrl(link);
    Key desiredFeedKey = KeyFactory.stringToKey(id);

    //Search for key from given Link
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity desiredFeedEntity = null;
    try {
      desiredFeedEntity = datastore.get(desiredFeedKey);
    } catch (EntityNotFoundException e){
      e.printStackTrace();
      res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to find given URL key, Please try again");
    }
    String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);
    
    RSS rssFeed = null;
    try {
      XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      rssFeed = XML_MAPPER.readValue(xmlString, RSS.class);

    } catch (Exception e) {
      res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to translate. Try again");
    }
    //Translate all the fields
    //Channel title
    rssFeed.getChannel().setTitle(translateText(targetLanguage, rssFeed.getChannel().getTitle()));

    // Channel description
    rssFeed.getChannel().setDescription(translateText(targetLanguage, rssFeed.getChannel().getDescription()));

    // Language
    rssFeed.getChannel().setLanguage(targetLanguage);

      // Episodes
    for (Item item : rssFeed.getChannel().getItems()) {
      // Episode title
      item.setTitle(translateText(targetLanguage, item.getTitle()));

      // Episode description
      item.setDescription(translateText(targetLanguage, item.getDescription()));
    }

      // Generate Translated XML string then place it into datastore
      String translatedXmlString = RSS.toXmlString(rssFeed);
      Entity translatedUserFeedEntity = new Entity(USER_FEED);
      translatedUserFeedEntity.setProperty(XML_STRING, translatedXmlString);
      datastore.put(translatedUserFeedEntity);
      String translatedFeedId = KeyFactory.keyToString(translatedUserFeedEntity.getKey());
      
      // display new translated string to the user
      res.setContentType("text/html");
      res.getWriter().println(BASE_URL + translatedFeedId);
  }

  /**
   * Translate the text using the translation API request and response client
   */
  public static String translateText(String targetLanguage, String text)throws IOException {

    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      LocationName parent = LocationName.of("launchpod-step18-2020", "global");

      TranslateTextRequest request =
          TranslateTextRequest.newBuilder()
              .setParent(parent.toString())
              .setMimeType("text/plain")
              .setTargetLanguageCode(targetLanguage)
              .addContents(text)
              .build();

      TranslateTextResponse response = client.translateText(request);
      return response.getTranslations(0).getTranslatedText();
    }
  }

  public static String getIdFromUrl(String rssLink) throws MalformedURLException {
    // Get the ID from the link that is being pasted
    URL feedUrl = new URL(rssLink);
    String queryString = feedUrl.getQuery();
    String[] querySplit = queryString.split("=");
    String id = querySplit[1];
    return id;
  }
}
