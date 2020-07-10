package com.google.launchpod.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/transcribe-feed")
public class TranscriptionServlet extends HttpServlet {

    private static String GCS_URI = "gcs_uri";

  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    String gcs_uri = req.getParameter(GCS_URI);
    String transcribedFile = asyncRecognizeGcs(gcs_uri);
  }

  /**
   * Performs non-blocking speech recognition on remote FLAC file and prints the transcription.
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
        // There can be several alternative transcripts for a given chunk of speech. Just use the
        // first (most likely) one here.
        SpeechRecognitionAlternative alternative = result
          .getAlternativesList()
          .get(0);
        return alternative.getTranscript();
      }
    }
  }
}
