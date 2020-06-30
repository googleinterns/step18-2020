package com.google.launchpod.data;

import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed{

  public final String podcastTitle;
  private final String email;
  private final String mp3Link;
  private final long timestamp;
  public final String xmlString;

  private UserFeed(String podcastTitle, String email, String mp3Link, long timestamp, String xmlString) {
    this.podcastTitle = podcastTitle;
    this.email = email;
    this.mp3Link = mp3Link;
    this.timestamp = timestamp;
    this.xmlString = xmlString;
  }
  /** Create UserFeed Object from Entity
   *  @param entity : entity that is being used to create user feed object
   *  @return UserFeed object
   */
  public static UserFeed fromEntity(Entity entity){
    String podcastTitle = (String) entity.getProperty(FormHandlerServlet.PODCAST_TITLE);
    String email = (String) entity.getProperty(FormHandlerServlet.EMAIL);
    String mp3Link = (String) entity.getProperty(FormHandlerServlet.MP3LINK);
    long timestamp = (long) entity.getProperty(FormHandlerServlet.TIMESTAMP);
    String xmlString = (String) entity.getProperty(FormHandlerServlet.XML_STRING);

    return new UserFeed(podcastTitle, email, mp3Link, timestamp, xmlString);
  }
}
