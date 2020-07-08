package com.google.launchpod.data;

import com.google.appengine.api.datastore.Entity;
import com.google.launchpod.servlets.FormHandlerServlet;

public final class UserFeed{

  private String xmlString;

  private UserFeed(String xmlString) {
    this.xmlString = xmlString;
  }
  /** Create UserFeed Object from Entity
   *  @param entity : entity that is being used to create user feed object
   *  @return UserFeed object
   */
  public static UserFeed fromEntity(Entity entity){
    String xmlString = (String) entity.getProperty(FormHandlerServlet.XML_STRING);
    return new UserFeed(xmlString);
  }

  public String getXmlString(){
    return this.xmlString;
  }
}
