package com.google.launchpod;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.Transient;
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
import com.google.launchpod.servlets.FileUploadServlet;
import com.google.launchpod.data.Keys;
import com.google.launchpod.data.TestKeys;
import com.google.launchpod.data.Helper;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.Channel;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.cloud.storage.testing.RemoteStorageHelper;

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
 * Runs unit tests for the FileUploadServlet that contains doPost() and doGet() methods.
 */
@RunWith(JUnit4.class)
public class FileUploadServletTest extends Mockito {

  @InjectMocks
  private FileUploadServlet servlet = new FileUploadServlet();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Rule // JUnit 4 uses Rules for testing specific messages
  public ExpectedException thrown = ExpectedException.none();

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig());
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

  private enum Action {
    GENERATE_RSS_LINK("generateRSSLink"), GENERATE_XML("generateXml");

    private String action;
 
    Action(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
      return action;
    }
  }

  /**
   * Creates a test user feed entity with an embedded entity for the MP3 object.
   */
  private static Entity makeEntity(String title, String description, String language, String email, String xmlString) {
    Entity userFeedEntity = new Entity(Keys.USER_FEED);
    userFeedEntity.setProperty(Keys.PODCAST_TITLE, title);
    userFeedEntity.setProperty(Keys.DESCRIPTION, description);
    userFeedEntity.setProperty(Keys.LANGUAGE, language);
    userFeedEntity.setProperty(Keys.USER_EMAIL, email);
    userFeedEntity.setProperty(Keys.XML_STRING, xmlString);
    return userFeedEntity;
  }

  /**
   * Creates a test user feed entity.
   */
  private static EmbeddedEntity makeEmbeddedEntity(String entityId, String email) {
    EmbeddedEntity mp3 = new EmbeddedEntity();
    mp3.setProperty(Keys.ID, entityId);
    mp3.setProperty(Keys.MP3_LINK, Helper.makeMp3Link(entityId));
    mp3.setProperty(Keys.USER_EMAIL, email);
    return mp3;
  }

  /**
  * Creates an entity with an XML string in Datastore.
  */
  private static Entity setUpEntityinDatastore() throws JsonProcessingException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_CATEGORY, TestKeys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);

    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, testXmlString);
    ds.put(entity);
    return entity;
  }

  /**
   * Asserts that doPost() takes in form inputs from client, successfully
   * stores that information in the Datastore entity associated with the id,
   * and correctly modifies the entity's XML string.
   */
  @Test
  public void doPost_StoresCorrectFormInput_CorrectlyModifiesXmlString() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_CATEGORY, TestKeys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(Keys.USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    // Verify xml string modification
    String expectedXmlString = Helper.createModifiedXml(rss, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, Helper.makeMp3Link(id));
    assertEquals(expectedXmlString, desiredEntity.getProperty(Keys.XML_STRING).toString());
  }

  /**
   * Asserts that doPost() takes in form inputs from client and successfully
   * stores that information + MP3 information as an embedded entity in Datastore.
   */
  @Test
  public void doPost_StoresCorrectFormInput_StoresCorrectMp3Info() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_CATEGORY, TestKeys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(Keys.USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    // Verify embedded entity
    EmbeddedEntity testEmbeddedEntity = makeEmbeddedEntity(id, TestKeys.TEST_EMAIL);
    assertEquals(testEmbeddedEntity, desiredEntity.getProperty(Keys.MP3));
  }

  /**
  * Expects doPost() to throw an IOException when a user tries to modify another user's feed.
  */
  @Test
  public void doPost_CorrectlyVerifiesUser() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL_TWO).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL_TWO, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_CATEGORY, TestKeys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.MP3_LINK)).thenReturn(TestKeys.TEST_MP3_LINK);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    assertEquals(1, ds.prepare(new Query(Keys.USER_FEED)).countEntities(withLimit(10)));
    thrown.expect(IOException.class);
    thrown.expectMessage("You are trying to edit a feed that's not yours!");
    servlet.doPost(request, response);
  }

  /**
   * Asserts that doPost() takes in form inputs from client and returns an HTML form.
   */
  @Test
  public void doPost_ReturnsHtmlForm() throws IOException, Exception {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_CATEGORY, TestKeys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(Keys.USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    assertEquals(1, ds.prepare(query).countEntities(withLimit(10)));

    // Verify that the HTML returned contains the form name
    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertTrue(stringWriter.toString().contains("mp3-upload"));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the episode title field is
   * empty.
   */
  @Test
  public void doPost_FormInputEmptyEpisodeTitle_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(Keys.EMPTY_STRING);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(null);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(Keys.EMPTY_STRING);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());
    
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(null);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No episode description inputted, please try again.");
    servlet.doPost(request, response);
  }

  /**
  * Expects doPost() to throw an IllegalArgumentException when the language field
  * is empty.
  */
  @Test
  public void doPost_FormInputEmptyLanguage_ThrowsErrorMessage() throws IOException {
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(Keys.EMPTY_STRING);
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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();

    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
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
    helper.setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");
    Entity entity = setUpEntityinDatastore();
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
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
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);  
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
    when(request.getParameter(Keys.EPISODE_TITLE)).thenReturn(TestKeys.TEST_TITLE);
    when(request.getParameter(Keys.EPISODE_DESCRIPTION)).thenReturn(TestKeys.TEST_DESCRIPTION);
    when(request.getParameter(Keys.EPISODE_LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.ID)).thenReturn(id); 

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("You are not logged in. Please try again.");
    servlet.doPost(request, response);
  }

  /**
   * Asserts that doGet() successfully creates link to the RSS feed when given an action and entity Keys.ID,
   * with one entity in Datastore.
   */
  @Test
  public void doGet_ReturnsRSSLink() throws IOException {

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, TestKeys.TEST_XML_STRING);
    ds.put(entity);

    String action = Keys.GENERATE_RSS_LINK;
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.ACTION)).thenReturn(action);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    String testRSSLink = "https://launchpod-step18-2020.appspot.com/rss-feed?action=generateXml&id=" + id;

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals(testRSSLink.trim(), stringWriter.toString().trim());
  }

  /**
  * Asserts that doGet() successfully returns an XML string from an entity
  * in Datastore when given an action and entity ID.
  */
  @Test
  public void doGet_SingleEntity_ReturnsCorrectXmlString() throws IOException, EntityNotFoundException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_CATEGORY, TestKeys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, testXmlString);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    Entity desiredEntity = ds.get(entity.getKey());
    EmbeddedEntity mp3 = makeEmbeddedEntity(id, TestKeys.TEST_EMAIL);
    desiredEntity.setProperty(Keys.MP3, mp3);
    ds.put(desiredEntity);

    when(request.getParameter(Keys.ACTION)).thenReturn(Keys.GENERATE_XML);
    when(request.getParameter(Keys.ID)).thenReturn(id);
    
    assertEquals(TestKeys.TEST_TITLE, desiredEntity.getProperty(Keys.PODCAST_TITLE).toString());
    assertEquals(TestKeys.TEST_DESCRIPTION, desiredEntity.getProperty(Keys.DESCRIPTION).toString());
    assertEquals(TestKeys.TEST_LANGUAGE, desiredEntity.getProperty(Keys.LANGUAGE).toString());
    assertEquals(TestKeys.TEST_EMAIL, desiredEntity.getProperty(Keys.USER_EMAIL).toString());

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/xml");
    writer.flush();
    assertEquals(testXmlString.trim(), stringWriter.toString().trim());
  }

  /**
  * Asserts that doGet() successfully returns an XML string from an entity
  * in Datastore when given an action and entity , with multiple entities in Datastore.
  */
  @Test
  public void doGet_MultipleEntities_ReturnsCorrectXmlString() throws IOException, EntityNotFoundException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_CATEGORY, TestKeys.TEST_LANGUAGE);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, testXmlString);
    Entity entityTwo = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL_TWO, testXmlString);
    ds.put(entity);
    ds.put(entityTwo);
    String id = KeyFactory.keyToString(entity.getKey());

    EmbeddedEntity mp3 = makeEmbeddedEntity(id, TestKeys.TEST_EMAIL);
    Entity desiredEntity = ds.get(entity.getKey());
    desiredEntity.setProperty(Keys.MP3, mp3);
    ds.put(desiredEntity);

    when(request.getParameter(Keys.ACTION)).thenReturn(Keys.GENERATE_XML);
    when(request.getParameter(Keys.ID)).thenReturn(id);

    assertEquals(TestKeys.TEST_TITLE, desiredEntity.getProperty(Keys.PODCAST_TITLE).toString());
    assertEquals(TestKeys.TEST_DESCRIPTION, desiredEntity.getProperty(Keys.DESCRIPTION).toString());
    assertEquals(TestKeys.TEST_LANGUAGE, desiredEntity.getProperty(Keys.LANGUAGE).toString());
    assertEquals(TestKeys.TEST_EMAIL, desiredEntity.getProperty(Keys.USER_EMAIL).toString());

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/xml");
    writer.flush();
    assertEquals(testXmlString.trim(), stringWriter.toString().trim());
  }

  /**
   * Asserts that doGet() returns an error message when an entity with request id
   * does not exist in Datastore.
   */
  @Test
  public void doGet_EntityNotFound_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, TestKeys.TEST_XML_STRING);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());
    ds.delete(entity.getKey());

    when(request.getParameter(Keys.ACTION)).thenReturn(Keys.GENERATE_XML);
    when(request.getParameter(Keys.ID)).thenReturn(id);

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
   * Asserts that doGet() returns an error message by catching an IllegalArgumentException
   * when an entity with request id cannot be converted to a key.
   */
  @Test
  public void doGet_InvalidId_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, TestKeys.TEST_XML_STRING);
    ds.put(entity);
    String id = "1234"; // incorrect id

    when(request.getParameter(Keys.ACTION)).thenReturn(Keys.GENERATE_XML);
    when(request.getParameter(Keys.ID)).thenReturn(id);

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
   * Asserts that doGet() returns an error message when it catches an
   * IllegalArgumentException pertaining to an unexpected action.
   */
  @Test
  public void doGet_NonexistentAction_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, TestKeys.TEST_XML_STRING);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.ACTION)).thenReturn("generateFake");
    when(request.getParameter(Keys.ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Illegal argument for action.".trim(), stringWriter.toString().trim());
    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * Asserts that doGet() returns an error message when the action parameter is not
   * generateRSSLink or generateXml. (Goes to else branch in servlet) 
   */
  @Test
  public void doGet_OtherAction_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_EMAIL, TestKeys.TEST_XML_STRING);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(Keys.ACTION)).thenReturn("otherAction");
    when(request.getParameter(Keys.ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Illegal argument for action.".trim(), stringWriter.toString().trim());
    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * Asserts that doGet() returns a message asking for action if action is null.
   */
  @Test
  public void doGet_NullAction_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(Keys.ACTION)).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Please specify action and/or id.".trim(), stringWriter.toString().trim());
    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * Asserts that doGet() returns a message asking for id if the id is null.
   */
  @Test
  public void doGet_NullId_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(Keys.ID)).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Please specify action and/or id.".trim(), stringWriter.toString().trim());
    String expectedMessage = "Please specify action and/or id.";
    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }
}
