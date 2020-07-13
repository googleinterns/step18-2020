package com.google.launchpod.data;

import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed{

  private final String podcastTitle;
  private final String description;
  private final String language;
  private final String email; 
  private final String mp3Link;
  private final String pubDate;

  private UserFeed(String podcastTitle, String description, String language, String email, String mp3Link, String pubDate) {
    this.podcastTitle = podcastTitle;
    this.description = description;
    this.language = language;
    this.email = email; 
    this.mp3Link = mp3Link;
    this.pubDate = pubDate;
  }
  /** Create UserFeed Object from Entity
   *  @param entity : entity that is being used to create user feed object
   *  @return UserFeed object
   */
  public static UserFeed fromEntity(Entity entity){
    String podcastTitle = (String) entity.getProperty(FormHandlerServlet.PODCAST_TITLE);
    String description = (String) entity.getProperty(FormHandlerServlet.DESCRIPTION);
    String language = (String) entity.getProperty(FormHandlerServlet.LANGUAGE);
    String email = (String) entity.getProperty(FormHandlerServlet.EMAIL);
    String mp3Link = (String) entity.getProperty(FormHandlerServlet.MP3LINK);
    String pubDate = (String) entity.getProperty(FormHandlerServlet.PUB_DATE);

    return new UserFeed(podcastTitle, description, language, email, mp3Link, pubDate);
  }

  public String getTitle(){
    return this.podcastTitle;
  }

  public String getDescription(){
    return this.description;
  }

  public String getLanguage(){
    return this.language;
  }

  public String getEmail(){
    return this.email;
  }

  public String getMp3Link(){
    return this.mp3Link;
  }

  public String getPubDate(){
    return this.pubDate;
  }
}
