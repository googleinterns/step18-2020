package com.google.launchpod.data;

import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed{

  private final String podcastTitle;
  private final String mp3Link;

  private UserFeed(String podcastTitle, String mp3Link) {
    this.podcastTitle = podcastTitle;
    this.mp3Link = mp3Link;
  }
  /** Create UserFeed Object from Entity
   *  @param entity : entity that is being used to create user feed object
   *  @return UserFeed object
   */
  public static UserFeed fromEntity(Entity entity){
    String podcastTitle = (String) entity.getProperty(FormHandlerServlet.PODCAST_TITLE);
    String mp3Link = (String) entity.getProperty(FormHandlerServlet.MP3LINK);

    return new UserFeed(podcastTitle, mp3Link);
  }

  public String getTitle(){
    return this.podcastTitle;
  }

  public String getLink(){
    return this.mp3Link;
  }
}
