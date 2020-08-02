package com.google.launchpod.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "rss")
public class RSS {

  @JacksonXmlProperty(isAttribute = true)
  @JsonProperty("version")
  private double version = 2.0;

  @JacksonXmlProperty(isAttribute = true, namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  @JsonProperty("itunes")
  private String itunes = "http://www.itunes.com/dtds/podcast-1.0.dtd";

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty
  @JsonProperty("channel")
  private Channel channel;

  private static final XmlMapper xmlMapper = new XmlMapper();

  /**
  * Constructor generated for serialization/deserialization. Ensures that a
  * constructor is being read for object conversion.
  */
  public RSS() {
  }

  public RSS(String name, String email, String podcastTitle, String description, String category, String language) {
    this.channel = new Channel(name, email, podcastTitle, description, category, language);
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
