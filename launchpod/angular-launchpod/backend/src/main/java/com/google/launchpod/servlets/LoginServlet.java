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

package com.google.launchpod.servlets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.launchpod.data.Item;
import com.google.launchpod.data.LoginStatus;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.UserFeed;

@WebServlet("/login-status")
public class LoginServlet extends HttpServlet {

  private static final XmlMapper XML_MAPPER = new XmlMapper();
  private static final Gson GSON = new Gson();
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final String PROJECT_ID = "launchpod-step18-2020";
  private static final String BUCKET_NAME = "launchpod-mp3-files";
  private static final String CLOUD_BASE_URL = "https://storage.googleapis.com/" + BUCKET_NAME + "/";

  static {
    XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    UserService userService = UserServiceFactory.getUserService();
    String urlToRedirectTo = "/index.html";
    if (userService.isUserLoggedIn() && userService.getCurrentUser().getEmail().contains("@google.com")) {
      String userEmail = userService.getCurrentUser().getEmail();

      String logoutUrl = userService.createLogoutURL(urlToRedirectTo);
      String loginMessage = "<p>Logged in as " + userEmail + ". <a href=\"" + logoutUrl + "\">Logout</a>.</p>";

      Query query = new Query(LoginStatus.USER_FEED_KEY).addSort(LoginStatus.TIMESTAMP_KEY, SortDirection.DESCENDING);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      ArrayList<UserFeed> userFeeds = new ArrayList<UserFeed>();
      for (Entity entity : results.asIterable()) {
        if (userEmail.equals(entity.getProperty(FormHandlerServlet.USER_EMAIL).toString())) {
          String userFeedEmail = String.valueOf(entity.getProperty(LoginStatus.EMAIL_KEY));
          String title = (String) entity.getProperty(LoginStatus.TITLE_KEY);
          String name = (String) entity.getProperty(LoginStatus.NAME_KEY);
          String description = (String) entity.getProperty(LoginStatus.DESCRIPTION_KEY);
          String language = (String) entity.getProperty(LoginStatus.LANGUAGE_KEY);
          String email = (String) entity.getProperty(LoginStatus.EMAIL_KEY);
          long timestamp = (long) entity.getProperty(LoginStatus.TIMESTAMP_KEY);
          Date date = new Date(timestamp);
          SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss Z", Locale.getDefault());
          String postTime = dateFormat.format(date);
          Key key = entity.getKey();

          String urlID = KeyFactory.keyToString(entity.getKey()); // the key string associated with the entity, not the
                                                                  // numeric ID.
          String rssLink = BASE_URL + urlID;

          userFeeds.add(new UserFeed(title, name, rssLink, description, email, postTime, urlID, language));
        }
      }

      LoginStatus loginStatus = LoginStatus.forSuccessfulLogin(loginMessage, userFeeds);

      response.getWriter().println(GSON.toJson(loginStatus));
    } else {
      String loginUrl = userService.createLoginURL(urlToRedirectTo);
      String loginMessage = loginUrl;

      LoginStatus loginStatus = LoginStatus.forFailedLogin(loginMessage);

      response.getWriter().println(GSON.toJson(loginStatus));
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String id = req.getParameter("keyId");
    Key key = KeyFactory.stringToKey(id);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();

    //Deserialize Xml string back into an object
    RSS rssFeed = null;
    try {
      Entity desiredFeed = datastore.get(key);
      rssFeed = XML_MAPPER.readValue(desiredFeed.getProperty(FormHandlerServlet.XML_STRING).toString(), RSS.class);
    } catch (Exception e) {
      e.printStackTrace();
      res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to create RSS feed from given key");
      return;
    }

    //Delete the generated mp3s from cloud storage
    for(Item item: rssFeed.getChannel().getItems()){
      if(item.getLink().contains(TTSServlet.TTS_BASE_URL)){
        String itemId = TranslationServlet.getIdFromUrl(item.getLink());
        deleteObject(itemId);
      } else if(item.getLink().contains(CLOUD_BASE_URL)){
        String itemId = item.getLink().replace(CLOUD_BASE_URL, "");
        deleteObject(itemId);
      }
    }

    datastore.delete(key);

    Query query = new Query(LoginStatus.USER_FEED_KEY).addSort(LoginStatus.TIMESTAMP_KEY, SortDirection.DESCENDING);

    PreparedQuery results = datastore.prepare(query);

    ArrayList<UserFeed> userFeeds = new ArrayList<UserFeed>();
    for (Entity entity : results.asIterable()) {
      String userFeedEmail = String.valueOf(entity.getProperty(LoginStatus.EMAIL_KEY));
      if (userEmail.equals(userFeedEmail)) {
        String feedTitle = (String) entity.getProperty(LoginStatus.TITLE_KEY);
        String feedName = (String) entity.getProperty(LoginStatus.NAME_KEY);
        String feedDescription = (String) entity.getProperty(LoginStatus.DESCRIPTION_KEY);
        String feedLanguage = (String) entity.getProperty(LoginStatus.LANGUAGE_KEY);
        String feedEmail = (String) entity.getProperty(LoginStatus.EMAIL_KEY);
        long feedTimestamp = (long) entity.getProperty(LoginStatus.TIMESTAMP_KEY);
        Date date = new Date(feedTimestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss Z", Locale.getDefault());
        String feedPostTime = dateFormat.format(date);
        Key feedKey = (Key) entity.getKey();

        String feedUrlID = KeyFactory.keyToString(entity.getKey()); // the key string associated with the entity, not
                                                                    // the numeric ID.
        String feedRssLink = BASE_URL + feedUrlID;

        userFeeds.add(new UserFeed(feedTitle, feedName, feedRssLink, feedDescription, feedEmail, feedPostTime,
            feedUrlID, feedLanguage));
      }
    }

    res.setContentType("application/json");
    res.getWriter().println(GSON.toJson(userFeeds));
  }

  /**
   *  Delete objects from the desired buckets in cloud storage
   * @throws IOException if the given bucket was not found
   * @param projectId
   * @param bucketName
   * @param objectName
   */
  public static void deleteObject(String objectName) throws IOException{
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storage.delete(BUCKET_NAME, objectName);
  }
}
