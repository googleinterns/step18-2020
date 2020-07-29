package com.google.launchpod.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "rss")
public class RSS {

  @JacksonXmlProperty(isAttribute = true)
  private double version = 2.0;

  @JacksonXmlProperty
  @JsonProperty("channel")
  private Channel channel;

  private static final XmlMapper xmlMapper = new XmlMapper();

  /**
   * constructor generated for serialization/deserialization. Ensures that a
   * constructor is being read for object conversion
   */
  public RSS() {
  }

  public RSS(String podcastTitle, String description, String language, String email, String mp3Link) {
    this.channel = new Channel(podcastTitle, description, language, email, mp3Link);
  }

  public Channel getChannel() {
    return this.channel;
  }

  /**
   * Create RSS XML string from given fields
   * 
   * @return xml String
   * @throws JsonProcessingException
   */
  public static String toXmlString(RSS rssFeed) throws JsonProcessingException {
    return xmlMapper.writeValueAsString(rssFeed);
  }
}
