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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;
import com.google.launchpod.data.LoginStatus;
import com.google.launchpod.data.UserFeed;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login-status")
public class LoginServlet extends HttpServlet {
  private static final Gson GSON = new Gson();
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    UserService userService = UserServiceFactory.getUserService();
    String urlToRedirectTo = "/index.html";
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String logoutUrl = userService.createLogoutURL(urlToRedirectTo);
      String loginMessage = "<p>Logged in as " + userEmail + ". <a href=\"" + logoutUrl + "\">Logout</a>.</p>";

      Query query =
        new Query(LoginStatus.USER_FEED_KEY).setFilter(new FilterPredicate("email", FilterOperator.EQUAL, userEmail)).addSort(LoginStatus.TIMESTAMP_KEY, SortDirection.DESCENDING);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      ArrayList<UserFeed> userFeeds = new ArrayList<UserFeed>();
      for (Entity entity : results.asIterable()) {
        String userFeedEmail = String.valueOf(entity.getProperty(LoginStatus.EMAIL_KEY));
        String title = (String) entity.getProperty(LoginStatus.TITLE_KEY);
        String name = (String) entity.getProperty(LoginStatus.NAME_KEY);
        String description = (String) entity.getProperty(LoginStatus.DESCRIPTION_KEY);
        String email = (String) entity.getProperty(LoginStatus.EMAIL_KEY);
        long timestamp = (long) entity.getProperty(LoginStatus.TIMESTAMP_KEY);
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss Z", Locale.getDefault());
        String postTime = dateFormat.format(date);
        Key key = entity.getKey();
        
        String urlID = KeyFactory.keyToString(entity.getKey()); // the key string associated with the entity, not the numeric ID.
        String rssLink = BASE_URL + urlID;

        userFeeds.add(new UserFeed(title, name, rssLink, description, email, postTime, key));
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
}
