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
  private List<ItunesOwner> itunesOwner;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty
  private List<Item> item;

  public Channel(String name, String email, String podcastTitle, String mp3Link) {
    this.itunesOwner = new ArrayList<>(Arrays.asList(new ItunesOwner(name, email)));
    this.item = new ArrayList<>(Arrays.asList(new Item(podcastTitle, mp3Link)));
  }
}
