package com.google.launchpod.servlets;

import com.google.appengine.api.datastore.Entity;

public final class UserFeed{

  private static final String name;
  private static final String mp3Link;
  private static final long timestamp;
  private static final String xmlString;

  public UserFeed(String name, String mp3Link, long timestamp, String xmlString){
    this.name = name; 
    this.mp3Link = mp3Link;
    this.timestamp = timestamp;
    this.xmlString = xmlString;
  }
}
