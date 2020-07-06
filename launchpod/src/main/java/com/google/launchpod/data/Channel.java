package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "channel")
public class Channel {

  @JacksonXmlProperty
  private String title = "Launchpod";

  @JacksonXmlProperty
  private String link = "https://launchpod-step18-2020.appspot.com";

  @JacksonXmlProperty
  private String language = "en";

  @JacksonXmlProperty
  private String description = "Launchpod generated RSS";

  @JacksonXmlProperty
  private Item item;

  public Channel(String podcastTitle, String mp3Link, String pubDate){
    this.item = new Item(podcastTitle, mp3Link, pubDate);
  }    
}
