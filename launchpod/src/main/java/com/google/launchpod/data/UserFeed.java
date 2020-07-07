package com.google.launchpod.data;

import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed{

  private final String podcastTitle;
  private final String mp3Link;
  private final String pubDate;

  private UserFeed(String podcastTitle, String mp3Link, String pubDate) {
    this.podcastTitle = podcastTitle;
    this.mp3Link = mp3Link;
    this.pubDate = pubDate;
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

  public String getTitle(){
    return this.podcastTitle;
  }

  public String getLink(){
    return this.mp3Link;
  }

  public String getPubDate(){
    return this.pubDate;
  }
}
