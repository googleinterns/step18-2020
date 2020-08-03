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

  /**
   * constructor generated for serialization/deserialization. Ensures that a
   * constructor is being read for object conversion
   */
  public Item() {
  }

  public Item(String podcastTitle, String podcastDescription, String mp3Link) {
    this.title = podcastTitle;
    this.description = podcastDescription;
    this.language = language; 
    this.email = email;
    this.link = mp3Link;
    this.pubDate = DATE_FORMATTER.format(DateTimeUtils.currentTimeMillis());
  }

  public void setTitle(String newTitle) {
    this.title = newTitle;
  }

  public void setDescription(String newDescription) {
    this.description = newDescription;
  }

  public void setLanguage(String newLanguage){
    this.language = newLanguage;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDescription() {
    return this.description;
  }

  public String getLanguage(){
    return this.language;
  }
}
