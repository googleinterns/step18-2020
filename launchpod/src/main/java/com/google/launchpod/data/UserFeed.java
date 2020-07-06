package com.google.launchpod.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

@JacksonXmlRootElement(localName = "rss")
public final class UserFeed{

  @JsonPropertyOrder({"channelTitle", "channelLink", "language", "channelDescription", "item"})
  @JacksonXmlProperty(isAttribute = true)
  private final double version=2.0;

  @JacksonXmlProperty
  @JacksonXmlElementWrapper
  private final List item ;

  @JacksonXmlProperty
  private final String channelTitle = "Launchpod";

  @JacksonXmlProperty
  private final String channelLink = "https://launchpod-step18-2020.appspot.com";

  @JacksonXmlProperty
  private final String channelDescription = "Feed generated in the launchpod website";

  @JacksonXmlProperty
  private final String language = "en";

  private UserFeed(String podcastTitle, String mp3Link, String pubDate) {
    this.item = new ArrayList<>(Arrays.asList(new Item(podcastTitle, mp3Link, pubDate)));
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
