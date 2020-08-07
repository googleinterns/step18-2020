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

package com.google.launchpod;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.interfaces.DSAKey;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import com.google.gson.JsonParser;
import com.google.launchpod.data.Keys;
import com.google.launchpod.data.TestKeys;
import com.google.launchpod.data.LoginStatus;
import com.google.launchpod.data.UserFeed;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.launchpod.servlets.LoginServlet;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.After;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Runs unit tests for the LoginServlet that contains doGet() method.
 */
@RunWith(JUnit4.class)
public class LoginServletTest extends Mockito {

  @InjectMocks
  private LoginServlet servlet = new LoginServlet();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Rule // JUnit 4 uses Rules for testing specific messages
  public ExpectedException thrown = ExpectedException.none();

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig());
  private static final Gson GSON = new Gson();
  JsonParser parser = new JsonParser();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
   * Asserts that doGet() gets the user's correct status when logged in.
   */
  @Test
  public void doGet_GetsCorrectStatusLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    assertEquals(true, GSON.fromJson(stringWriter.toString(), LoginStatus.class).isLoggedIn);
  }

  /**
   * Asserts that doGet() gets the user's email and successfully sends
   * a corresponding loginStatus object as the response.
   */
  @Test
  public void doGet_GetsCorrectMessageLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String urlToRedirectToAfterUserLogsOut = "/index.html";
    String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
    String loginMessage = "<p>Logged in as " + TestKeys.TEST_EMAIL + ". <a href=\"" + logoutUrl + "\">Logout</a>.</p>";
    verify(response).setContentType("application/json");
    assertEquals(loginMessage, GSON.fromJson(stringWriter.toString(), LoginStatus.class).message);
  }

  /**
   * Asserts that doGet() gets the user's feeds and successfully sends
   * a corresponding loginStatus object as the response.
   */
  @Test
  public void doGet_GetsCorrectFeedsLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String urlToRedirectToAfterUserLogsOut = "/index.html";
    String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
    String loginMessage = "<p>Logged in as " + TestKeys.TEST_EMAIL + ". <a href=\"" + logoutUrl + "\">Logout</a>.</p>";

    Query query =
        new Query(LoginStatus.USER_FEED_KEY).setFilter(new FilterPredicate("email", FilterOperator.EQUAL, TestKeys.TEST_EMAIL)).addSort(LoginStatus.TIMESTAMP_KEY, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<UserFeed> userFeeds = new ArrayList<UserFeed>();
    for (Entity entity : results.asIterable()) {
      String userFeedEmail = String.valueOf(entity.getProperty(LoginStatus.EMAIL_KEY));
      String title = (String) entity.getProperty(LoginStatus.TITLE_KEY);
      String name = (String) entity.getProperty(LoginStatus.NAME_KEY);
      String description = (String) entity.getProperty(LoginStatus.DESCRIPTION_KEY);
      String email = (String) entity.getProperty(LoginStatus.EMAIL_KEY);
      String language = (String) entity.getProperty(LoginStatus.LANGUAGE_KEY);
      long timestamp = (long) entity.getProperty(LoginStatus.TIMESTAMP_KEY);
      Date date = new Date(timestamp);
      SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss");
      String postTime = dateFormat.format(date);
      Key key = entity.getKey();
      
      String urlID = KeyFactory.keyToString(entity.getKey()); // the key string associated with the entity, not the numeric ID.
      String rssLink = Keys.BASE_URL + urlID;

      userFeeds.add(new UserFeed(title, name, rssLink, description, email, postTime, urlID, language));
    }

    verify(response).setContentType("application/json");
    assertEquals(userFeeds, GSON.fromJson(stringWriter.toString(), LoginStatus.class).feeds);
  }

  /**
   * Asserts that doGet() gets the user's correct status when logged out.
   */
  @Test
  public void doGet_GetsCorrectStatusLoggedOut() throws IOException {
    helper.setEnvIsLoggedIn(false);
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    assertEquals(false, GSON.fromJson(stringWriter.toString(), LoginStatus.class).isLoggedIn);
  }

  /**
   * Asserts that doGet() successfully sends a login url as the
   * response when the user is not logged in.
   */
  @Test
  public void doGet_GetsCorrectMessageLoggedOut() throws IOException {
    helper.setEnvIsLoggedIn(false);
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String urlToRedirectToAfterUserLogsOut = "/index.html";
    String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsOut);
    String loginMessage = loginUrl;

    verify(response).setContentType("application/json");
    assertEquals(loginMessage, GSON.fromJson(stringWriter.toString(), LoginStatus.class).message);
  }
}
