// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.launchpod.data;

/*
 * Store keys used in servlets.
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
  public static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  public static final String EMPTY_STRING = "";
  // Variables required for cloud storage
  public static final String PROJECT_ID = "launchpod-step18-2020"; // ID of GCP Project
  public static final String BUCKET_NAME = "launchpod-mp3-files"; // ID of GCS bucket to upload to

  public static final String TEST_TITLE = "TEST_TITLE";
  public static final String TEST_DESCRIPTION = "TEST_DESCRIPTION";
  public static final String TEST_NAME = "TEST_NAME";
  
}