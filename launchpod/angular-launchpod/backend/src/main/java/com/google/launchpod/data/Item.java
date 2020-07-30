package com.google.launchpod.data;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import org.joda.time.DateTimeUtils;

@JacksonXmlRootElement(localName = "item")
public class Item {

  @JacksonXmlProperty
  private String title;

  @JacksonXmlProperty
  private String link;

  @JacksonXmlProperty
  private String description;

  @JacksonXmlProperty
  private String language;

  @JacksonXmlProperty
  private String email;

  @JacksonXmlProperty
  private String pubDate;

  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

  public Item(String podcastTitle, String description, String language, String email, String mp3Link) {
    this.title = podcastTitle;
    this.description = description;
    this.language = language; 
    this.email = email;
    this.link = mp3Link;
    this.pubDate = dateFormatter.format(DateTimeUtils.currentTimeMillis());
  }
}
