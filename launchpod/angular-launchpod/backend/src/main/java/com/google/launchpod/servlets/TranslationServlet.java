package com.google.launchpod.servlets;

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
import com.google.launchpod.data.RSS;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/translate-feed")
public class TranslationServlet {

  private static final String ID = "id";
  private static final String XML_STRING = "xmlString";
  private static final XmlMapper XML_MAPPER = new XmlMapper();

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws JsonParseException, JsonMappingException, IOException {
    String id = req.getParameter(ID);
    Key desiredFeedKey = KeyFactory.stringToKey(id);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity desiredFeedEntity = datastore.get(desiredFeedKey);
      String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);
      RSS rssFeed = XML_MAPPER.readValue(xmlString, RSS.class);
    } catch (EntityNotFoundException e) {
      //TODO: add code to run when there is an exception
      return;
    }

    String projectId = "launchpod-step18-2020";
    // Supported Languages: https://cloud.google.com/translate/docs/languages
    String targetLanguage = "es";
    String text = "your-text";
    translateText(projectId, targetLanguage, text);
  }

  // Translating Text
  public static void translateText(String projectId, String targetLanguage, String text)
      throws IOException {
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      // Supported Locations: `global`, [glossary location], or [model location]
      // Glossaries must be hosted in `us-central1`
      // Custom Models must use the same location as your model. (us-central1)
      LocationName parent = LocationName.of(projectId, "global");

      // Supported Mime Types: https://cloud.google.com/translate/docs/supported-formats
      TranslateTextRequest request =
          TranslateTextRequest.newBuilder()
              .setParent(parent.toString())
              .setMimeType("text/plain")
              .setTargetLanguageCode(targetLanguage)
              .addContents(text)
              .build();

      TranslateTextResponse response = client.translateText(request);

      // Display the translation for each input text provided
      for (Translation translation : response.getTranslationsList()) {
        System.out.printf("Translated text: %s\n", translation.getTranslatedText());
      }
    }
  }
}