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
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.launchpod.servlets.CreateByLinkServlet;
import com.google.launchpod.data.Keys;
import com.google.launchpod.data.UserFeed;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.Channel;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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
import org.joda.time.DateTimeUtils;

/**
 * Runs unit tests for the FormHandlerServlet that contains doPost(), doGet(),
 * and xmlString() methods.
 */
@RunWith(JUnit4.class)
public class CreateByLinkServletTest extends Mockito {

  @InjectMocks
  private CreateByLinkServlet servlet = new CreateByLinkServlet();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Rule // JUnit 4 uses Rules for testing specific messages
  public ExpectedException thrown = ExpectedException.none();

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig());

  // keys
  private static final String TEST_MP3_LINK = "http://www.gstatic.com/podcasts/test-podcast/audio/test-episode-4.mp3";
  private static final long TEST_TIMESTAMP = System.currentTimeMillis();
  private static final String TEST_ID = "123456";
  private static final String TEST_ID_TWO = "789012";
  private static final String TEST_EMAIL_TWO = "456@abc.com";
  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

  @Before
  public void setUp() throws ParseException {
    MockitoAnnotations.initMocks(this);
    Date fixedDateTime = dateFormatter.parse("07/29/2019 21:39:00:000");
    DateTimeUtils.setCurrentMillisFixed(fixedDateTime.getTime());
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
  * Creates an entity with an XML string in Datastore.
  */
  private static Entity setUpEntityinDatastore() throws JsonProcessingException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(Keys.TEST_NAME, Keys.TEST_EMAIL, Keys.TEST_TITLE, Keys.TEST_DESCRIPTION, Keys.TEST_CATEGORY, Keys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);

    Entity entity = makeEntity(Keys.TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    return entity;
  }

  /**
  * Creates a test user feed entity.
  */
  public static Entity makeEntity(String title, String mp3Link, String xmlString) {
    Entity userFeedEntity = new Entity(Keys.USER_FEED);
    userFeedEntity.setProperty(Keys.PODCAST_TITLE, title);
    userFeedEntity.setProperty(Keys.MP3_LINK, mp3Link);
    userFeedEntity.setProperty(Keys.XML_STRING, xmlString);
    userFeedEntity.setProperty(Keys.USER_EMAIL, Keys.TEST_EMAIL);
    return userFeedEntity;
  }

  /**
   * Asserts that doPost() takes in form inputs from client and successfully
   * stores that information in a Datastore entity.
   */
  @Test
  public void doPost_CorrectlyModifiesXml() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(Keys.TEST_NAME, Keys.TEST_EMAIL, Keys.TEST_TITLE, Keys.TEST_DESCRIPTION, Keys.TEST_CATEGORY, Keys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(Keys.TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(Keys.USER_FEED)).countEntities(withLimit(10)));

    Query query = new Query(Keys.USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    String expectedXmlString = Helper.createModifiedXml(rss, Keys.TEST_TITLE, Keys.TEST_DESCRIPTION, Keys.TEST_LANGUAGE, Keys.TEST_EMAIL, TEST_MP3_LINK);

    assertEquals(expectedXmlString, desiredEntity.getProperty(Keys.XML_STRING).toString());
  }

  /**
  * Expects doPost() to throw an IOException when a user tries to modify another user's feed.
  */
  @Test
  public void doPost_CorrectlyVerifiesUser() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL_TWO).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(Keys.TEST_NAME, TEST_EMAIL_TWO, Keys.TEST_TITLE, Keys.TEST_DESCRIPTION, Keys.TEST_CATEGORY, Keys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(Keys.TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    assertEquals(1, ds.prepare(new Query(Keys.USER_FEED)).countEntities(withLimit(10)));
    thrown.expect(IOException.class);
    thrown.expectMessage("You are trying to edit a feed that's not yours!");
    servlet.doPost(request, response);
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the episode title field is
   * empty.
   */
  @Test
  public void doPost_FormInputEmptyEpisodeTitle_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.EMPTY_STRING);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(id);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode title inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the episode title field is
   * null.
   */
  @Test
  public void doPost_FormInputNullTitle_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(null);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode title inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the episode description field is
  * empty.
  */
  @Test
  public void doPost_FormInputEmptyEpisodeDescription_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.EMPTY_STRING);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode description inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the episode description field is
  * null.
  */
  @Test
  public void doPost_FormInputNullEpisodeDescription_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());
    
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(null);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode description inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the MP3 link field
   * is empty.
   */
  @Test
  public void doPost_FormInputEmptyMp3Link_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(Keys.EMPTY_STRING);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No mp3 link inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the MP3 link field
   * is null.
   */
  @Test
  public void doPost_FormInputNullMp3Link_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(null);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No mp3 link inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the language field
  * is empty.
  */
  @Test
  public void doPost_FormInputEmptyLanguage_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(Keys.EMPTY_STRING);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode language inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the language field
  * is null.
  */
  @Test
  public void doPost_FormInputNullLanguage_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(null);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode language inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the id is empty.
  */
  @Test
  public void doPost_EmptyId_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(Keys.EMPTY_STRING);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Sorry, no entity Id could be found.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the id is null.
  */
  @Test
  public void doPost_NullId_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(Keys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Sorry, no entity Id could be found.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the email field
  * is empty.
  */
  @Test
  public void doPost_FormInputEmptyEmail_ThrowsErrorMessage() throws IOException {
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);  
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("You are not logged in. Please try again.");
    servlet.doPost(request, response);
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the email field
   * is null.
   */
  @Test
  public void doPost_FormInputNullEmail_ThrowsErrorMessage() throws IOException {
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.TEST_TITLE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id); 

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("You are not logged in. Please try again.");
    servlet.doPost(request, response);
  }
}
