package com.google.launchpod.data;

import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed {

  private String xmlString;
  private String email;
  private String language;

  private UserFeed(String xmlString, String email, String language) {
    this.xmlString = xmlString;
    this.email = email;
    this.language = language;
  }

  /**
   * Create UserFeed Object from Entity
   * 
   * @param entity : entity that is being used to create user feed object
   * @return UserFeed object
   */
  public static UserFeed fromEntity(Entity entity) {
    String xmlString = (String) entity.getProperty(FormHandlerServlet.XML_STRING);
    String email = (String) entity.getProperty(FormHandlerServlet.EMAIL);
    String language = (String) entity.getProperty(FormHandlerServlet.LANGUAGE);
    return new UserFeed(xmlString, email, language);
  }

  // getter for xml string
  public String getXmlString() {
    return this.xmlString;
  }

  // getter for email
  public String getEmail() {
    return this.email;
  }

  public String getLanguage(){
    return this.language;
  }
}
