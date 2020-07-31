package com.google.launchpod.data;

import java.util.ArrayList;

/** An item on a comment list. */
public final class LoginStatus {
  public boolean isLoggedIn;
  public String message;
  public ArrayList<UserFeed> feeds;

  public static final String USER_FEED_KEY = "UserFeed";
  public static final String TITLE_KEY = "title";
  public static final String DESCRIPTION_KEY = "description";
  public static final String NAME_KEY = "name";
  public static final String EMAIL_KEY = "email";
  public static final String XML_STRING_KEY = "xmlString";
  public static final String POST_TIME_KEY = "postTime";
  public static final String TIMESTAMP_KEY = "timestamp";
  public static final String LANGUAGE_KEY = "language";

  private LoginStatus(boolean isLoggedIn, String message, ArrayList<UserFeed> feeds) {
    this.isLoggedIn = isLoggedIn;
    this.message = message;
    this.feeds = feeds;
  }

  public static LoginStatus forSuccessfulLogin(String message, ArrayList<UserFeed> feeds) {
    return new LoginStatus(true, message, feeds);
  }

  public static LoginStatus forFailedLogin(String message) {
    return new LoginStatus(false, message, new ArrayList<UserFeed>());
  }
}