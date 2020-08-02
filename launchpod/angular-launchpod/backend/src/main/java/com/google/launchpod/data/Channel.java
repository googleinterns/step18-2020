package com.google.launchpod.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JacksonXmlRootElement(localName = "channel")
@JsonPropertyOrder({"title", "link", "language", "description", "author", "owner", "category", "item"})
public class Channel {

  @JacksonXmlProperty
  private String title = "Launchpod";

  @JacksonXmlProperty
  private String link = "https://launchpod-step18-2020.appspot.com";

  @JacksonXmlProperty
  private String language;

  @JacksonXmlProperty
  private String description;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "owner", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  @JsonProperty("itunesOwner")
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
  @JacksonXmlProperty
  @JsonProperty("item")
  private List<Item> items;

  /**
  * Constructor generated for serialization/deserialization. Ensures that a
  * constructor is being read for object conversion.
  */
  public Channel() {
  }

  public Channel(String name, String email, String podcastTitle, String description, String category, String language) {
    this.itunesOwner = new ArrayList<>(Arrays.asList(new ItunesOwner(name, email)));
    this.itunesCategory = new ArrayList<>(Arrays.asList(new ItunesCategory(category)));
    this.items = new ArrayList<>();
    this.author = name;
    this.language = language;
    this.description = description;
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

  /**
  * Add an item to a channel.
  */
  public static void addItem(Channel channel, String podcastTitle, String description, String language, String email, String mp3Link) {
    Item item = new Item(podcastTitle, description, language, email, mp3Link);
    if (channel.getItems() == null) {
      channel.items = new ArrayList<>();
    }
    channel.getItems().add(item);
  }
}
