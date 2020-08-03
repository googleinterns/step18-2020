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
  private static final String USER_FEED = "UserFeed";
  private static final String PODCAST_TITLE = "podcastTitle";
  private static final String EPISODE_TITLE = "episodeTitle";
  private static final String EPISODE_DESCRIPTION = "episodeDescription";
  private static final String EPISODE_LANGUAGE = "episodeLanguage";
  private static final String XML_STRING = "xmlString";
  private static final String MP3_LINK = "mp3Link"; // URL to existing MP3 file
  private static final String TIMESTAMP = "timestamp";
  private static final String EMAIL = "email";
  private static final String ID = "id";

  private static final String TEST_TITLE = "TEST_TITLE";
  private static final String TEST_DESCRIPTION = "TEST_DESCRIPTION";
  private static final String TEST_LANGUAGE = "en";
  private static final String TEST_MP3_LINK = "http://www.gstatic.com/podcasts/test-podcast/audio/test-episode-4.mp3";
  private static final String TEST_NAME = "TEST_NAME";
  private static final String TEST_CATEGORY = "Business";
  private static final long TEST_TIMESTAMP = System.currentTimeMillis();
  private static final String TEST_ID = "123456";
  private static final String TEST_ID_TWO = "789012";
  private static final String TEST_PUBDATE = "2020/06/26 01:32:06";
  private static final String TEST_EMAIL = "123@abc.com";
  private static final String TEST_INCORRECT_EMAIL = "123@cde.com";
  private static final String TEST_EMAIL_TWO = "456@abc.com";
  private static final String EMPTY_STRING = "";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
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
   * Creates a test user feed entity.
   */
  private Entity makeEntity(String title, String mp3Link, String xmlString) {
    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, title);
    userFeedEntity.setProperty(MP3_LINK, mp3Link);
    userFeedEntity.setProperty(XML_STRING, xmlString);
    return userFeedEntity;
  }

  /**
  * Given and RSS feed and episode details, adds that episode to the RSS Feed and returns the XML of that modified feed.
  */
  private String createModifiedXml(RSS rssFeed, String episodeTitle, String episodeDescription, String episodeLanguage, String email, String mp3Link) throws JsonProcessingException {
    Channel channel = rssFeed.getChannel();
    channel.addItem(channel, episodeTitle, episodeDescription, episodeLanguage, email, mp3Link); // to-do: double check this
    String modifiedXmlString = RSS.toXmlString(rssFeed);
    return modifiedXmlString;
  }

  /**
  * Creates an entity with an XML string in Datastore.
  */
  private Entity setUpEntityinDatastore() throws JsonProcessingException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION, TEST_CATEGORY, TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);

    Entity entity = makeEntity(TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    return entity;
  }

  /**
   * Asserts that doPost() takes in form inputs from client and successfully
   * stores that information in a Datastore entity.
   */
  @Test
  public void doPost_CorrectlyModifiesXml() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION, TEST_CATEGORY, TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

    Query query = new Query(USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    String expectedXmlString = createModifiedXml(rss, TEST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL, TEST_MP3_LINK);

    assertEquals(expectedXmlString, desiredEntity.getProperty(XML_STRING).toString());
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the episode title field is
   * empty.
   */
  @Test
  public void doPost_FormInputEmptyEpisodeTitle_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn("");
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn(null);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(id);

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn("");
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(id);

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());
    
    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(null);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(id);

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn("");
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(id);

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(null);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(id);

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn("");
    when(request.getParameter(ID)).thenReturn(id);

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(null);
    when(request.getParameter(ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode language inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the id is empty.
  */
  @Test
  public void doPost_EmptyId_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();

    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn("");

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(null);

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
    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);  
    when(request.getParameter(ID)).thenReturn(id);

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
    when(request.getParameter(EPISODE_TITLE)).thenReturn(TEST_TITLE);
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);
    when(request.getParameter(EPISODE_DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(EPISODE_LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(ID)).thenReturn(id); 

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("You are not logged in. Please try again.");
    servlet.doPost(request, response);
  }

  /**
   * Asserts that doGet() returns correct XML string when given an entity ID, with
   * one entity in Datstore.
   */
  @Test
  public void doGet_SingleEntity_ReturnsCorrectXmlString() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION, TEST_CATEGORY, TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);

    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/xml");
    writer.flush();
    assertEquals(testXmlString, stringWriter.toString());
  }

  /**
   * Asserts that doGet() returns correct XML string when given an entity ID, with
   * multiple entities in Datastore.
   */
  @Test
  public void doGet_MultipleEntities_ReturnsCorrectXmlString() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION, TEST_CATEGORY, TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);

    Entity entity = makeEntity(TEST_TITLE, TEST_MP3_LINK, testXmlString);
    Entity entityTwo = makeEntity(TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    ds.put(entityTwo);

    String id = KeyFactory.keyToString(entity.getKey());
    String idTwo = KeyFactory.keyToString(entityTwo.getKey());

    when(request.getParameter(ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/xml");
    writer.flush();
    assertEquals(testXmlString, stringWriter.toString());
  }

  /**
  * Asserts that doGet() returns an error message by catching an IllegalArgumentException
  * when an entity with request id cannot be converted to a key.
  */
  @Test
  public void doGet_InvalidId_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION, TEST_CATEGORY, TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    String id = "1234"; // incorrect id

    when(request.getParameter(ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Sorry, this is not a valid id.".trim(), stringWriter.toString().trim());
    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * Expects that doGet() returns an error message when an entity with request id
   * does not exist in Datastore.
   */
  @Test
  public void doGet_EntityNotFound() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION, TEST_CATEGORY, TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TEST_TITLE, TEST_MP3_LINK, testXmlString);
    ds.put(entity);
    Key key = entity.getKey();
    String id = KeyFactory.keyToString(key);
    ds.delete(key);

    when(request.getParameter(ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Your entity could not be found.".trim(), stringWriter.toString().trim());
    verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  /**
   * Asserts that doGet() returns a message asking for id if the id is null.
   */
  @Test
  public void doGet_NullId_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(ID)).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Sorry, no matching Id was found in Datastore.");
    servlet.doGet(request, response);
  }
}
