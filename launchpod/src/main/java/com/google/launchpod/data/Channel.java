package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "channel")
public final class Channel {

  @JacksonXmlProperty
  private final String title = "Launchpod";

  @JacksonXmlProperty
  private final String link = "https://launchpod-step18-2020.appspot.com";

  @JacksonXmlProperty
  private final String language = "en";

  @JacksonXmlProperty
  private final String description = "Launchpod generated RSS";

  @JacksonXmlProperty
  private final Item item;

  public Channel(String podcastTitle, String mp3Link, String pubDate){
    this.item = new Item(podcastTitle, mp3Link, pubDate);
  }    
}
