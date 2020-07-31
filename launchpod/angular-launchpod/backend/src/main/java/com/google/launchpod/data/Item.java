package com.google.launchpod.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

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
  private String description = "Episode 1";

  @JacksonXmlProperty
  @JsonProperty("pubDate")
  private String pubDate;

  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
  private static final LocalDateTime publishTime = LocalDateTime.now();

  /**
   * constructor generated for serialization/deserialization. Ensures that a
   * constructor is being read for object conversion
   */
  public Item() {
  }

  public Item(String podcastTitle, String mp3Link) {
    this.title = podcastTitle;
    this.link = mp3Link;
    this.pubDate = dateFormatter.format(publishTime);
  }

  public void setTitle(String newTitle) {
    this.title = newTitle;
  }

  public void setDescription(String newDescription) {
    this.description = newDescription;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDescription() {
    return this.description;
  }
}
