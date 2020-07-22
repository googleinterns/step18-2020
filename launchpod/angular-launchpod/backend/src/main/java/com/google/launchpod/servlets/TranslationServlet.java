package com.google.launchpod.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.launchpod.data.Item;
import com.google.launchpod.data.RSS;

@WebServlet("/translate-feed")
public class TranslationServlet extends HttpServlet {

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
    if (link == null) {
      throw new IOException("Please give valid link.");
    }
    
    if (targetLanguage == null) {
      throw new IOException("Please give valid language.");
    }
    try {
      XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      String id = getIdFromUrl(link);

      Key desiredFeedKey = KeyFactory.stringToKey(id);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity desiredFeedEntity = datastore.get(desiredFeedKey);
      if(desiredFeedEntity == null){
          throw new EntityNotFoundException(desiredFeedKey);
      }
      String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);
      RSS rssFeed = XML_MAPPER.readValue(xmlString, RSS.class);

      // Translate fields to new language
      Translate translate = TranslateOptions.getDefaultInstance().getService();

      // Channel description
      Translation translation = translate.translate(rssFeed.getChannel().getDescription());
      rssFeed.getChannel().setDescription(translation.getTranslatedText());

      // Language
      rssFeed.getChannel().setLanguage(targetLanguage);

      // Episodes
      for (Item item : rssFeed.getChannel().getItem()) {
        // Episode title
        translation = translate.translate(item.getTitle());
        item.setTitle(translation.getTranslatedText());

        // Episode description
        translation = translate.translate(item.getDescription());
        item.setDescription(translation.getTranslatedText());
      }

      // Generate Translated XML string then place it into datastore
      String translatedXmlString = RSS.toXmlString(rssFeed);
      Entity translatedUserFeedEntity = new Entity(USER_FEED);
      translatedUserFeedEntity.setProperty(XML_STRING, translatedXmlString);
      Key newFeedKey = datastore.put(translatedUserFeedEntity);
      String translatedFeedId = KeyFactory.keyToString(translatedUserFeedEntity.getKey());

      // display new translated string to the user
      res.setContentType("text/html");
      res.getWriter().println(BASE_URL + translatedFeedId);
      } catch (EntityNotFoundException e) {
        res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to obtain ID from given link");
        return;
    }
  }

  /**
   * Generate translated text from inputted string, then return the output of the
   * translated text
   *
   * @param projectId
   * @param targetLanguage language to be translated to
   * @param text           the text that is to be translated
   * @throws IOException
   * @return String in html form that contains the translated text
   */
  public static String translateText(String projectId, String targetLanguage, String text) throws IOException {
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      LocationName parent = LocationName.of(projectId, "global");

      TranslateTextRequest request = TranslateTextRequest.newBuilder().setParent(parent.toString())
          .setMimeType("text/html").setTargetLanguageCode(targetLanguage).addContents(text).build();

      TranslateTextResponse response = client.translateText(request);

      // Display the translation for each input text provided
      String translatedDiv = "<p>";
      for (com.google.cloud.translate.v3.Translation translation : response.getTranslationsList()) {
        translatedDiv += translation.getTranslatedText();
      }
      translatedDiv += "</p>";
      return translatedDiv;
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
