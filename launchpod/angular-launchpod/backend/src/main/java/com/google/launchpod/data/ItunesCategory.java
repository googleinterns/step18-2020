package com.google.launchpod.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
public class ItunesCategory {

  @JacksonXmlProperty(isAttribute = true)
  @JsonProperty("text")
  private String text;
  
  /**
   * constructor generated for serialization/deserialization. Ensures that a
   * constructor is being read for object conversion
   */
  public ItunesCategory(){
  }

  public ItunesCategory(String category) {
    this.text = category;
  }
}
