package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "rss")
public class RSS {
    
  @JacksonXmlProperty
  private final Channel channel;

  public RSS(String podcastTitle, String mp3Link, String pubDate){
    this.channel = new Channel(podcastTitle, mp3Link, pubDate);
  }  
}