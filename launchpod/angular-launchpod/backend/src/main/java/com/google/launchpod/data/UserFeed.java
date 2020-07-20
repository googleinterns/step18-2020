package com.google.launchpod.data;

import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed {

  private String xmlString;
  private String email;

  private UserFeed(String xmlString, String email) {
    this.xmlString = xmlString;
    this.email = email;
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
    return new UserFeed(xmlString, email);
  }

  // getter for xml string
  public String getXmlString() {
    return this.xmlString;
  }

  // getter for email
  public String getEmail() {
    return this.email;
  }
}
