package com.google.launchpod.data;

/*
 * Store commonly used keys and test variables used in servlets.
 */
public final class Keys {
  public static final String USER_FEED = "UserFeed";
  public static final String TITLE = "title";
  public static final String PODCAST_TITLE = "podcastTitle";
  public static final String DESCRIPTION = "description";
  public static final String TIMESTAMP = "timestamp";
  public static final String CATEGORY = "category";
  public static final String LANGUAGE = "language";
  public static final String EPISODE_TITLE = "episodeTitle";
  public static final String EPISODE_DESCRIPTION = "episodeDescription";
  public static final String EPISODE_LANGUAGE = "episodeLanguage";
  public static final String USER_NAME = "name";
  public static final String USER_EMAIL = "email";
  public static final String ID = "id";
  public static final String XML_STRING = "xmlString";
  public static final String MP3 = "mp3";
  public static final String MP3_LINK = "mp3Link";
  public static final String RSS_FEED_LINK = "rssFeedLink";
  public static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  public static final String EMPTY_STRING = "";
  public static final String ACTION = "action";
  public static final String GENERATE_XML = "GENERATE_XML";
  public static final String GENERATE_RSS_LINK = "GENERATE_RSS_LINK";
  public static final String OTHER_ACTION = "OTHER_ACTION";
  // Variables required for cloud storage
  public static final String PROJECT_ID = "launchpod-step18-2020"; // ID of GCP Project
  public static final String BUCKET_NAME = "launchpod-mp3-files"; // ID of GCS bucket to upload to
}