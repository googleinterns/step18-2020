package com.google.launchpod.data;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS z");

  public Item(String podcastTitle, String podcastDescription, String mp3Link) {
    this.title = podcastTitle;
    this.description = podcastDescription;
    this.language = language; 
    this.email = email;
    this.link = mp3Link;
    this.pubDate = DATE_FORMATTER.format(DateTimeUtils.currentTimeMillis());
  }
}
