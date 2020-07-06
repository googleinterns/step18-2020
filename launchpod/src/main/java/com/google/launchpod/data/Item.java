package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "item")
public class Item {
  
  @JacksonXmlProperty
  private final String title;

  @JacksonXmlProperty(localName = "link")
  private final String mp3Link;

  @JacksonXmlProperty
  private final String description;

  @JacksonXmlProperty
  private final String pubDate;

  public Item(String title, String mp3Link, String pubDate){
    this.title = title;
    this.mp3Link = mp3Link;
    this.description = "Episode 1";
    this.pubDate = pubDate;
  }
}
