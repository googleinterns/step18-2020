package com.google.launchpod.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/transcribe-feed")
public class TranscriptionServlet extends HttpServlet {

    private static final String ID = "id";
    private static String mp3Link = "https://storage.googleapis.com/launchpod-mp3-files/";

  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    //TODO: wait for billing to pass, then finish doPost to send transcribed file back to the user
    String id = req.getParameter(id);
    mp3Link += id;
    String transcribedFile = asyncRecognizeGcs(gcs_uri);
    res.setContentType("text/html");
    res.getWriter().println(transcribedFile);
  }

  /**
   * Performs speech recognition and saves the text
   * @param gcsUri the path to remote file to be transcribed
   */
  public static String asyncRecognizeGcs(String gcsUri) throws Exception {
      //create client credentials
    try (SpeechClient speech = SpeechClient.create()) {
      // Configure remote file request for FLAC
      RecognitionConfig config = RecognitionConfig
        .newBuilder()
        .setEncoding(AudioEncoding.FLAC)
        .setLanguageCode("en-US")
        .setSampleRateHertz(16000)
        .build();
      RecognitionAudio audio = RecognitionAudio
        .newBuilder()
        .setUri(gcsUri)
        .build();

      // Use non-blocking call for getting file transcription
      OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speech.longRunningRecognizeAsync(
        config,
        audio
      );
      while (!response.isDone()) {
        System.out.println("Waiting for response...");
        Thread.sleep(10000);
      }

      List<SpeechRecognitionResult> results = response.get().getResultsList();

      for (SpeechRecognitionResult result : results) {
        //get first transcript generated inside of the results
        SpeechRecognitionAlternative alternative = result
          .getAlternativesList()
          .get(0);
        return alternative.getTranscript();
      }
    }
  }
}
