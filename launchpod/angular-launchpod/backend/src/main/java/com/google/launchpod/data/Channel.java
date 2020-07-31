package com.google.launchpod.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "channel")
public class Channel {

  @JacksonXmlProperty
  private String title = "Launchpod";

  @JacksonXmlProperty
  private String link = "https://launchpod-step18-2020.appspot.com";

  @JacksonXmlProperty
  private String language = "en";

  @JacksonXmlProperty
  private String description = "Launchpod generated RSS";

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty
  private List<Item> item;

  public Channel(String podcastTitle, String mp3Link) {
    this.item = new ArrayList<>(Arrays.asList(new Item(podcastTitle, mp3Link)));
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
    channel.getItems().add(item);
  }
}
