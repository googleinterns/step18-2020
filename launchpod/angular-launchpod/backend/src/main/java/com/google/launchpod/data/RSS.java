package com.google.launchpod.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "rss")
public class RSS {

  @JacksonXmlProperty(isAttribute = true)
  private double version = 2.0;

  @JacksonXmlProperty(isAttribute = true, namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  private String itunes = "http://www.itunes.com/dtds/podcast-1.0.dtd";

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty
  private Channel channel;

  private static final XmlMapper xmlMapper = new XmlMapper();

  public RSS(String name, String email, String podcastTitle, String mp3Link, String category) {
    this.channel = new Channel(name, email, podcastTitle, mp3Link, category);
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
