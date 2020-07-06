package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "item")
public class Item {
  
  @JacksonXmlProperty
  private String title;

  @JacksonXmlProperty
  private String link;

  @JacksonXmlProperty
  private String description;

  @JacksonXmlProperty
  private String pubDate;

  public Item(String title, String link, String pubDate){
    this.title = title;
    this.link = link;
    this.description = "Episode 1";
    this.pubDate = pubDate;
  }
}
