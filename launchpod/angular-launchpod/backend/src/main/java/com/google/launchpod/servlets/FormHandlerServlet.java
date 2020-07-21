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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.launchpod.data.RSS;

@WebServlet("/rss-feed")
public class FormHandlerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String USER_FEED = "UserFeed";
  private static final String PODCAST_TITLE = "title";
  private static final String MP3_LINK = "mp3Link";
  private static final String USER_NAME = "name";
  private static final String USER_EMAIL = "email";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final String ID = "id";
  // public variable to allow creation of UserFeed objects
  public static final String XML_STRING = "xmlString";

  /**
   * Requests user inputs in form fields, then creates Entity and places in Datastore.
   *
   * @throws IOException,IllegalArgumentException
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String mp3Link = req.getParameter(MP3_LINK);
    String name = userService.getCurrentUser().getNickname();
    String email = userService.getCurrentUser().getEmail();

    if (podcastTitle == null || podcastTitle.isEmpty()) {
      throw new IllegalArgumentException("No Title inputted, please try again.");
    } else if (mp3Link == null || mp3Link.isEmpty()) {
      throw new IllegalArgumentException("No Mp3 inputted, please try again.");
    }

    // Creates entity with all desired attributes
    Entity userFeedEntity = new Entity(USER_FEED);
    
    userFeedEntity.setProperty(USER_NAME, name);
    userFeedEntity.setProperty(USER_EMAIL, email);
    
    // Generate xml string
    RSS rssFeed = new RSS(name, email, podcastTitle, mp3Link);
    try {
      String xmlString = RSS.toXmlString(rssFeed);
      userFeedEntity.setProperty(XML_STRING, xmlString);
    } catch (IOException e) {
      throw new IOException("Unable to create XML string.");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);

    // return accessible link to user
    String urlID = KeyFactory.keyToString(userFeedEntity.getKey()); // the key string associated with the entity, not the numeric ID.
    String rssLink = BASE_URL + urlID;
    res.setContentType("text/html");
    res.getWriter().print(rssLink);
  }

  /**
   * Display RSS feed xml string that user tries recalling with the given ID.
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // Get ID passed in request
    String id = req.getParameter(ID);
    if (id == null) {
      throw new IllegalArgumentException("Sorry, no matching Id was found in Datastore.");
    }
    Key urlID = KeyFactory.stringToKey(id);
    // Search key in datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // create entity that contains id from datastore
    try {
      Entity desiredFeedEntity = datastore.get(urlID);

      // generate xml string
      String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);
      res.setContentType("text/xml");
      res.getWriter().print(xmlString);

      // If there is no entity that matches the key
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      res.setContentType("text/html");
      res.getWriter().print("<p>Sorry. This is not a valid link.</p>");
      return;
    }
  }
}
