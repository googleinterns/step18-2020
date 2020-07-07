package com.google.launchpod.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "item")
public class Item {
  
  @JacksonXmlProperty
  private String title;

  @JacksonXmlProperty
  private String link;

  @JacksonXmlProperty
  private String description = "Episode 1";

  @JacksonXmlProperty
  private String pubDate;

  public Item(String podcastTitle, String mp3Link){
    this.title = podcastTitle;
    this.link = mp3Link;
    this.pubDate = createDate();
  }
  public String createDate(){
    // Create time
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime publishTime = LocalDateTime.now();
    String pubDate = dateFormatter.format(publishTime);

    return pubDate;
  }
}
