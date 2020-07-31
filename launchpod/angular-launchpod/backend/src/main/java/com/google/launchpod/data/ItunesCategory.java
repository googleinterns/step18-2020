package com.google.launchpod.data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
public class ItunesCategory {

  @JacksonXmlProperty(isAttribute = true)
  private String text;

  public ItunesCategory(String category) {
    this.text =  category;
  }
}