package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "rss")
public class RSS {

  @JacksonXmlProperty(isAttribute = true)
  private double version = 2.0;
   
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty
  private Channel channel;

  public RSS(String podcastTitle, String mp3Link, String pubDate){
    this.channel = new Channel(podcastTitle, mp3Link, pubDate);
  }  
}
