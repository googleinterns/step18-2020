package com.google.launchpod.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JacksonXmlRootElement(localName = "channel")
@JsonPropertyOrder({ "title", "link", "language", "description", "author", "owner", "category", "item" })
public class Channel {

  @JacksonXmlProperty
  @JsonProperty("title")
  private String title = "Launchpod";

  @JacksonXmlProperty
  @JsonProperty("link")
  private String link = "https://launchpod-step18-2020.appspot.com";

  @JacksonXmlProperty
  @JsonProperty("language")
  private String language;

  @JacksonXmlProperty
  @JsonProperty("description")
  private String description = "Launchpod generated RSS";

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "owner", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  private List<ItunesOwner> itunesOwner;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "author", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  @JsonProperty("author")
  private String author;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "category", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  @JsonProperty("itunesCategory")
  private List<ItunesCategory> itunesCategory;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("item")
  private List<Item> items;

  /**
   * constructor generated for serialization/deserialization. Ensures that a
   * constructor is being read for object conversion
   */
  public Channel() {
  }

  public Channel(String name, String email, String podcastTitle, String mp3Link, String category) {
    this.itunesOwner = new ArrayList<>(Arrays.asList(new ItunesOwner(name, email)));
    this.itunesCategory = new ArrayList<>(Arrays.asList(new ItunesCategory(category)));
    this.items = new ArrayList<>(Arrays.asList(new Item(podcastTitle, mp3Link)));
    this.author = name;
  }

  public void setLanguage(String newLanguage) {
    this.language = newLanguage;
  }

  public void setDescription(String newDescription) {
    this.description = newDescription;
  }

  public String getLanguage() {
    return this.language;
  }

  public String getDescription() {
    return this.description;
  }

  public List<Item> getItems() {
    return this.items;
  }
}
