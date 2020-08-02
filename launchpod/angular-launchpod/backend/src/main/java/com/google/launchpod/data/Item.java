package com.google.launchpod.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import org.joda.time.DateTimeUtils;

@JacksonXmlRootElement(localName = "item")
public class Item {

  @JacksonXmlProperty
  @JsonProperty("title")
  private String title;

  @JacksonXmlProperty
  @JsonProperty("link")
  private String link;

  @JacksonXmlProperty
  @JsonProperty("description")
  private String description;

  @JacksonXmlProperty
  @JsonProperty("language")
  private String language;

  @JacksonXmlProperty
  @JsonProperty("email")
  private String email;

  @JacksonXmlProperty
  @JsonProperty("pubDate")
  private String pubDate;

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS z");

  public Item(String podcastTitle, String description, String language, String email, String mp3Link) {
    this.title = podcastTitle;
    this.description = description;
    this.language = language;
    this.email = email;
    this.link = mp3Link;
    this.pubDate = this.pubDate = DATE_FORMATTER.format(DateTimeUtils.currentTimeMillis());
  }
}
