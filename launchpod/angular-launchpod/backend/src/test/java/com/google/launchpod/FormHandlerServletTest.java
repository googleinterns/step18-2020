package com.google.launchpod;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.interfaces.DSAKey;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.launchpod.servlets.FormHandlerServlet;
import com.google.launchpod.data.UserFeed;
import com.google.launchpod.data.RSS;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
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
 * Runs unit tests for the FormHandlerServlet that contains doPost(), doGet(),
 * and xmlString() methods.
 */
@RunWith(JUnit4.class)
public class FormHandlerServletTest extends Mockito {

  @InjectMocks
  private FormHandlerServlet servlet = new FormHandlerServlet();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Rule // JUnit 4 uses Rules for testing specific messages
  public ExpectedException thrown = ExpectedException.none();

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  // keys
  private static final String USER_FEED = "UserFeed";
  private static final String PODCAST_TITLE = "title";
  private static final String XML_STRING = "xmlString";
  private static final String DESCRIPTION = "description";
  private static final String LANGUAGE = "language";
  private static final String TIMESTAMP = "timestamp";
  private static final String EMAIL = "email";
  private static final String MP3 = "mp3";
  private static final String MP3_LINK = "mp3Link";
  private static final String ID = "id";
  private static final String ACTION = "action";

  private static final String TEST_PODCAST_TITLE = "TEST_PODCAST_TITLE";
  private static final String TEST_DESCRIPTION= "TEST_DESCRIPTION";
  private static final String TEST_LANGUAGE= "en";
  private static final long TEST_TIMESTAMP = System.currentTimeMillis();
  private static final String TEST_ID = "123456";
  private static final String TEST_ID_TWO = "789012";
  private static final String TEST_MP3_LINK = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
  private static final String TEST_PUBDATE = "2020/06/26 01:32:06";
  private static final String TEST_EMAIL = "123@abc.com";
  private static final String TEST_INCORRECT_EMAIL = "123@cde.com";
  private static final String TEST_EMAIL_TWO = "456@abc.com";
  private static final String EMPTY_STRING = "";
  private static final String TEST_XML_STRING = "test";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final RSS TEST_RSS_FEED = new RSS(TEST_PODCAST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL, TEST_MP3_LINK);
  private static final String GENERATE_XML_ACTION = "generateXml";

  private static final String PROJECT_ID = "launchpod-step18-2020"; // The ID of your GCP project
  private static final String BUCKET_NAME = "launchpod-mp3-files"; // The ID of the GCS bucket to upload to

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // TO-DO: googleapis.dev/java/google-cloud-storage/latest/index.html for test helper

  /**
   * Creates a test user feed entity with an embedded entity for the MP3 object.
   */
  private Entity makeEntity(String title, String description, String language, String email, String entityId, String mp3Link, String xmlString) {
    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, title);
    userFeedEntity.setProperty(DESCRIPTION, description);
    userFeedEntity.setProperty(LANGUAGE, language);
    userFeedEntity.setProperty(EMAIL, email);

    EmbeddedEntity mp3 = makeEmbeddedEntity(entityId, mp3Link, xmlString);
    mp3.setProperty(ID, entityId);
    mp3.setProperty(MP3_LINK, mp3Link);
    mp3.setProperty(EMAIL, email);

    userFeedEntity.setProperty(MP3, mp3);
    userFeedEntity.setProperty(XML_STRING, xmlString);
    return userFeedEntity;
  }

  /**
   * Creates a test user feed entity.
   */
  private EmbeddedEntity makeEmbeddedEntity(String entityId, String mp3Link, String email) {
    EmbeddedEntity mp3Entity = new EmbeddedEntity();
    mp3.setProperty(ENTITY_ID, entityId);
    mp3.setProperty(MP3_LINK, mp3Link);
    mp3.setProperty(EMAIL, email);
    return mp3Entity;
  }

  /**
   * Asserts that doPost() takes in form inputs from client and successfully
   * stores that information in a Datastore entity.
   */
  @Test
  public void doPost_StoresCorrectFormInput() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
    when(request.getParameter(DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(EMAIL)).thenReturn(TEST_EMAIL);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Asserts that doPost() takes in form inputs from client, successfully
   * stores that information in a Datastore entity, and correctly creates and stores xml string.
   */
  @Test
  public void doPost_StoresCorrectFormInput_StoresCorrectXmlString() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
    when(request.getParameter(DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(EMAIL)).thenReturn(TEST_EMAIL);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    // verify xml string generation
    String testXmlString = RSS.toXmlString(TEST_RSS_FEED);
    assertEquals(testXmlString, desiredEntity.getProperty(XML_STRING).toString());
  }

  // TO-DO: check for the property MP3, mp3 where mp3 is an object with ENTITY_ID, MP3_LINK, and EMAIL fields
    /**
   * Asserts that doPost() takes in form inputs from client and successfully
   * stores that information + MP3 information as an embedded entity in Datastore.
   */
  @Test
  public void doPost_StoresCorrectFormInput_StoresCorrectEmbeddedEntity() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
    when(request.getParameter(DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(EMAIL)).thenReturn(TEST_EMAIL);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    String id = KeyFactory.keyToString(desiredEntity.getKey());

    // verify xml string generation
    String testXmlString = RSS.toXmlString(TEST_RSS_FEED);
    assertEquals(testXmlString, desiredEntity.getProperty(XML_STRING).toString());
  }

  /**
   * Asserts that doPost() takes in form inputs from client and returns an HTML form.
   */
  @Test
  public void doPost_ReturnsHtmlForm() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
    when(request.getParameter(DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);
    when(request.getParameter(EMAIL)).thenReturn(TEST_EMAIL);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    assertEquals(1, ds.prepare(query).countEntities(withLimit(10)));

    String id = KeyFactory.keyToString(desiredEntity.getKey());

    // verify that generatePostPolicyV4() was called once
    verify(servlet, times(1)).generatePostPolicyV4(PROJECT_ID, BUCKET_NAME, id);
    verify(response, times(1)).setContentType("text/html");
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the title field is
   * empty.
   */
  @Test
  public void doPost_FormInputEmptyTitle_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(PODCAST_TITLE)).thenReturn("");

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No Title inputted, please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the title field is
   * null.
   */
  @Test
  public void doPost_FormInputNullTitle_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(PODCAST_TITLE)).thenReturn(null);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No Title inputted, please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the description field
   * is empty.
   */
  @Test
  public void doPost_FormInputEmptyDescription_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(DESCRIPTION)).thenReturn("");

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No description inputted, please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the description field
   * is null.
   */
  @Test
  public void doPost_FormInputNullDescription_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(DESCRIPTION)).thenReturn(null);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No description inputted, please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the language field
   * is empty.
   */
  @Test
  public void doPost_FormInputEmptyLanguage_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(LANGUAGE)).thenReturn("");

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No language inputted, please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the language field
   * is null.
   */
  @Test
  public void doPost_FormInputNullLanguage_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(LANGUAGE)).thenReturn(null);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("No language inputted, please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the email field
   * is empty.
   */
  @Test
  public void doPost_FormInputEmptyEmail_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(EMAIL)).thenReturn("");

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("You are not logged in. Please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  /**
   * Expects doPost() to throw an IllegalArgumentException when the email field
   * is null.
   */
  @Test
  public void doPost_FormInputNullEmail_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(EMAIL)).thenReturn(null);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("You are not logged in. Please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  // Note: Post-MVP, we will change the language input into a dropdown to prevent invalid inputs.

  /**
   * Asserts that doGet() successfully creates link to the RSS feed when given an action and entity ID,
   * with one entity in Datastore.
   */
  @Test
  public void doGet_ReturnsRSSLink() throws IOException {

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL, TEST_ID, TEST_MP3_LINK, TEST_XML_STRING);
    ds.put(entity);

    String action = "generateRSSLink";
    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(ACTION)).thenReturn(action);
    when(request.getParameter(ID)).thenReturn(id);

    String testRSSLink = "https://launchpod-step18-2020.appspot.com/rss-feed?action=generateXml&id=" + id;

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals(0, testRSSLink.compareTo(stringWriter.toString()));
  }

  /**
   * Asserts that doGet() successfully creates an xml string when given an action and entity ID,
   * with one entity in Datastore.
   */
  @Test
  public void doGet_SingleEntity_ReturnsCorrectXmlString() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL, TEST_ID, TEST_MP3_LINK, TEST_XML_STRING);
    String testXmlString = RSS.toXmlString(rss);
    ds.put(entity);

    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(ACTION)).thenReturn(GENERATE_XML_ACTION);
    when(request.getParameter(ID)).thenReturn(id);

    Entity desiredFeedEntity = datastore.get(id);
    assertEquals(TEST_PODCAST_TITLE, desiredFeedEntity.getProperty(PODCAST_TITLE).toString());
    assertEquals(TEST_DESCRIPTION, desiredFeedEntity.getProperty(DESCRIPTION).toString());
    assertEquals(TEST_LANGUAGE, desiredFeedEntity.getProperty(LANGUAGE).toString());
    assertEquals(TEST_EMAIL, desiredFeedEntity.getProperty(EMAIL).toString());

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
    Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL, TEST_ID, TEST_MP3_LINK, TEST_XML_STRING);
    Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL_TWO, TEST_ID_TWO, TEST_MP3_LINK, TEST_XML_STRING);
    String testXmlString = RSS.toXmlString(rss);
    ds.put(entity);

    String id = KeyFactory.keyToString(entity.getKey());

    when(request.getParameter(ACTION)).thenReturn(GENERATE_XML_ACTION);
    when(request.getParameter(ID)).thenReturn(id);

    Entity desiredFeedEntity = datastore.get(id);
    assertEquals(TEST_PODCAST_TITLE, desiredFeedEntity.getProperty(PODCAST_TITLE).toString());
    assertEquals(TEST_DESCRIPTION, desiredFeedEntity.getProperty(DESCRIPTION).toString());
    assertEquals(TEST_LANGUAGE, desiredFeedEntity.getProperty(LANGUAGE).toString());
    assertEquals(TEST_EMAIL, desiredFeedEntity.getProperty(EMAIL).toString());

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/xml");
    writer.flush();
    assertEquals(testXmlString, stringWriter.toString());
  }

  /**
   * Expects that doGet() returns an error message when an entity with request id
   * does not exist in Datastore.
   */
  @Test
  public void doGet_EntityNotFound() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL, TEST_ID, TEST_MP3_LINK, TEST_XML_STRING);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());
    ds.delete(entity.getKey());

    when(request.getParameter(ACTION)).thenReturn(GENERATE_XML_ACTION);
    when(request.getParameter(ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Your entity could not be found.", stringWriter.toString());
  }

  /**
   * Expects doGet() to throw an error message when there are no entities in
   * Datastore period. TO-DO: add this test to testing file for LoginServlet (MVP)
   */
  @Test
  public void doGet_NoEntitiesInDatastore_ThrowsErrorMessage() throws IOException {

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // TO-DO: change this to EntityNotFoundException?
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Sorry, no matching Id was found in Datastore.");
    servlet.doGet(request, response);
  }

  /**
   * Expects doGet() return message asking for action if it is null.
   */
  @Test
  public void doGet_NullAction_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(ACTION)).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Please specify action and/or id.", stringWriter.toString());
  }

  /**
   * Expects doGet() to return message asking for id if it is null.
   */
  @Test
  public void doGet_NullId_SendsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(ID)).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("Please specify action and/or id.", stringWriter.toString());
  }


}

