package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
public class ItunesOwner {

  @JacksonXmlProperty(localName = "name", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  private String name;

  @JacksonXmlProperty(localName = "email", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  private String email;

  public ItunesOwner(String name, String email) {
    this.name = name;
    this.email = email;
  }
}
