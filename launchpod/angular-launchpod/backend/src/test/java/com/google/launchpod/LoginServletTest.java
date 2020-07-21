
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
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.launchpod.data.LoginStatus;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.launchpod.servlets.LoginServlet;
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

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  private static final Gson GSON = new Gson();
  JsonParser parser = new JsonParser();

  private static final String EMAIL = "email";

  private static final String TEST_EMAIL = "123@abc.com";

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
   * Asserts that doPost() gets the user's correct status when logged in.
   */
  @Test
  public void doGet_GetsCorrectStatusLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String urlToRedirectToAfterUserLogsOut = "/index.html";
    String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
    String loginMessage = "<p>Logged in as " + TEST_EMAIL + ". <a href=\"" + logoutUrl + "\">Logout</a>.</p>";
    LoginStatus loginStatus = new LoginStatus(true, loginMessage);
    verify(response).setContentType("application/json");
    assertEquals(loginStatus.isLoggedIn, GSON.fromJson(stringWriter.toString(), LoginStatus.class).isLoggedIn);
  }

  /**
   * Asserts that doPost() gets the user's email and successfully sends
   * a corresponding loginStatus object as the response.
   */
  @Test
  public void doGet_GetsCorrectMessageLoggedIn() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String urlToRedirectToAfterUserLogsOut = "/index.html";
    String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
    String loginMessage = "<p>Logged in as " + TEST_EMAIL + ". <a href=\"" + logoutUrl + "\">Logout</a>.</p>";
    LoginStatus loginStatus = new LoginStatus(true, loginMessage);
    verify(response).setContentType("application/json");
    assertEquals(loginStatus.message, GSON.fromJson(stringWriter.toString(), LoginStatus.class).message);
  }

  /**
   * Asserts that doPost() gets the user's correct status when logged out.
   */
  @Test
  public void doGet_GetsCorrectStatusLoggedOut() throws IOException {
    helper.setEnvIsLoggedIn(false);
    UserService userService = UserServiceFactory.getUserService();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String urlToRedirectToAfterUserLogsOut = "/index.html";
    String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsOut);
    String loginMessage = loginUrl;
    LoginStatus loginStatus = new LoginStatus(false, loginMessage);
    verify(response).setContentType("application/json");
    assertEquals(loginStatus.isLoggedIn, GSON.fromJson(stringWriter.toString(), LoginStatus.class).isLoggedIn);
  }

  /**
   * Asserts that doPost() successfully sends a login url as the
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
    LoginStatus loginStatus = new LoginStatus(false, loginMessage);
    verify(response, atLeast(1)).setContentType("application/json");
    assertEquals(loginStatus.message, GSON.fromJson(stringWriter.toString(), LoginStatus.class).message);
  }
}
