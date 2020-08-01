package com.google.launchpod.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
public class ItunesOwner {

  @JacksonXmlProperty(localName = "name", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  @JsonProperty("name")
  private String name;

  @JacksonXmlProperty(localName = "email", namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
  @JsonProperty("email")
  private String email;
  
  /**
  * constructor generated for serialization/deserialization. Ensures that a
  * constructor is being read for object conversion
  */
  public ItunesOwner(){   
  }
  
  public ItunesOwner(String name, String email) {
    this.name = name;
    this.email = email;
  }

  public String getName(){
    return this.name;
  }
}
