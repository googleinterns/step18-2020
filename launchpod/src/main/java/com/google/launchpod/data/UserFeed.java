package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed{

  @JacksonXmlProperty
  private final RSS rss;

  private UserFeed(String podcastTitle, String mp3Link, String pubDate) {
    this.rss = new RSS(podcastTitle, mp3Link, pubDate);
  }
  /** Create UserFeed Object from Entity
   *  @param entity : entity that is being used to create user feed object
   *  @return UserFeed object
   */
  public static UserFeed fromEntity(Entity entity){
    String podcastTitle = (String) entity.getProperty(FormHandlerServlet.PODCAST_TITLE);
    String mp3Link = (String) entity.getProperty(FormHandlerServlet.MP3LINK);
    String pubDate = (String) entity.getProperty(FormHandlerServlet.PUB_DATE);

    return new UserFeed(podcastTitle, mp3Link, pubDate);
  }
}
