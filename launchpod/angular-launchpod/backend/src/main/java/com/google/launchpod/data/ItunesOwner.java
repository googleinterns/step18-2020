package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JacksonXmlRootElement(localName = "owner", namespace = "itunes")
public class ItunesOwner {

  @JacksonXmlProperty(localName = "name", namespace = "itunes")
  private String name;

  @JacksonXmlProperty(localName = "email", namespace = "itunes")
  private String email;

  public ItunesOwner(String name, String email) {
    this.name = name;
    this.email = email;
  }
}
