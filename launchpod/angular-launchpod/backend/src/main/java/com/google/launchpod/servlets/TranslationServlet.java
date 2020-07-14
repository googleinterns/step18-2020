package com.google.launchpod.servlets;

import com.google.appengine.api.datastore.DatastoreService;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import java.io.IOException;

@WebServlet("/translate-feed")
public class TranslateText {

    private static final String ID = "id";

  public void doPost(HttpServletRequest req, HttpServletResponse res){
    String id = req.getParameter(ID);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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