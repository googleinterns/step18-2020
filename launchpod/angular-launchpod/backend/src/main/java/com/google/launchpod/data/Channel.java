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
  private String title;

  @JacksonXmlProperty
  private String link;

  @JacksonXmlProperty
  private String language;

  @JacksonXmlProperty
  private String description;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty
  private List<Item> items;

  public Channel(String podcastTitle, String description, String language, String email, String mp3Link) {
    this.items = new ArrayList<>(Arrays.asList(new Item(podcastTitle, description, language, email, mp3Link)));
  }

  public List<Item> getItems() {
    return this.items;
  }

  /**
  * Add an item to a channel.
  */
  public static void addItem(Channel channel, String episodeTitle, String episodeDescription, String episodeLanguage, String email, String mp3Link) {
    Item item = new Item(episodeTitle, episodeDescription, episodeLanguage, email, mp3Link);
    channel.getItems().add(item);
  }
}
