package com.google.launchpod.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import com.google.common.base.Strings;
import com.google.launchpod.data.Keys;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.Channel;

/*
 * Store helper methods used in servlets and test files.
 */
public class Helper {
  /**
  * Helper method for repeated code in catching exceptions.
  */
  public static void writeResponse(HttpServletResponse res, String message, int statusCode) throws IOException {
    res.setContentType("text/html");
    res.getWriter().println(message);
    res.setStatus(statusCode);
  }

  /**
  * Given and RSS feed and episode details, adds that episode to the RSS Feed and returns the XML of that modified feed.
  */
  public static String createModifiedXml(RSS rssFeed, String episodeTitle, String episodeDescription, String episodeLanguage, String email, String mp3Link) throws JsonProcessingException {
    Channel channel = rssFeed.getChannel();
    channel.addItem(episodeTitle, episodeDescription, episodeLanguage, email, mp3Link);
    String modifiedXmlString = RSS.toXmlString(rssFeed);
    return modifiedXmlString;
  }

  /**
  * Creates an MP3 link based on GCS documentation.
  */
  public static String makeMp3Link(String entityId) {
    String link = "https://storage.googleapis.com/" + Keys.BUCKET_NAME + "/" + entityId;
    return link;
  }
}