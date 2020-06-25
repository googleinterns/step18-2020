package com.google.launchpod.servlets;

import com.google.appengine.api.datastore.Entity;

public final class UserFeed{

  private static final String name;
  private static final String mp3Link;
  private static final long timestamp;

  public UserFeed(String name, String mp3Link){
    this.name = name; 
    this.mp3Link = mp3Link;
    this.timestamp = timestamp;
  }
}
