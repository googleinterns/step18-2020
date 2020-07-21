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
  private static final String ID = "id";

  private static final String TEST_PODCAST_TITLE = "TEST_PODCAST_TITLE";
  private static final String TEST_DESCRIPTION= "TEST_DESCRIPTION";
  private static final String TEST_LANGUAGE= "en";
  private static final long TEST_TIMESTAMP = System.currentTimeMillis();
  private static final String TEST_ID = "123456";
  private static final String TEST_ID_TWO = "789012";
  private static final String TEST_PUBDATE = "2020/06/26 01:32:06";
  private static final String TEST_EMAIL = "123@abc.com";
  private static final String TEST_INCORRECT_EMAIL = "123@cde.com";
  private static final String TEST_EMAIL_TWO = "456@abc.com";
  private static final String EMPTY_STRING = "";
  private static final String TEST_XML_STRING = "test";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";
  private static final RSS TEST_RSS_FEED = new RSS(TEST_PODCAST_TITLE, TEST_MP3_LINK);

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

    Query query = new Query(USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    String expectedXmlString = RSS.toXmlString(TEST_RSS_FEED);

    assertEquals(expectedXmlString, desiredEntity.getProperty(XML_STRING).toString());
  }

  /**
   * Asserts that doPost() takes in form inputs from client, successfully stores
   * that information in a Datastore entity, and returns an HTML form.
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

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

    Query query = new Query(USER_FEED);
    PreparedQuery preparedQuery = ds.prepare(query);
    Entity desiredEntity = preparedQuery.asSingleEntity();

    // verify xml string generation
    String expectedXmlString = RSS.toXmlString(TEST_RSS_FEED);
    assertEquals(expectedXmlString, desiredEntity.getProperty(XML_STRING).toString());

    String testXmlString = RSS.toXmlString(TEST_RSS_FEED);
    assertEquals(testXmlString, desiredEntity.getProperty(XML_STRING).toString());

    // String id = KeyFactory.keyToString(desiredEntity.getKey());
    // String rssLink = BASE_URL + id;

    // verify(response, times(1)).setContentType("text/html");
    // assertEquals(0, rssLink.compareTo(stringWriter.toString()));

    //TO-DO: verify that generatePostPolicyV4() was called once
    verify(response, times(1)).setContentType("text/html");
  }

  // TO-DO: check for the property MP3, mp3 where mp3 is an object with ENTITY_ID, MP3_LINK, and EMAIL fields

  /**
   * Expects doPost() to throw an IllegalArgumentException when the title field is
   * empty.
   */
  @Test
  public void doPost_FormInputEmptyTitle_ThrowsErrorMessage() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter(PODCAST_TITLE)).thenReturn("");
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

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
    when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

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
    when(request.getParameter(DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
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
    when(request.getParameter(DESCRIPTION)).thenReturn(TEST_DESCRIPTION);
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
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);
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
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);
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
    when(request.getParameter(EMAIL)).thenReturn(TEST_EMAIL);
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
    when(request.getParameter(EMAIL)).thenReturn(TEST_EMAIL);
    when(request.getParameter(EMAIL)).thenReturn(null);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("You are not logged in. Please try again.");
    servlet.doPost(request, response);

    assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
  }

  // TO-DO: add test that checks language is actually valid

  /**
   * Asserts that doGet() returns correct XML string when given an entity ID, with
   * one entity in Datstore.
   */
  @Test
  public void doGet_SingleEntity_ReturnsCorrectXmlString() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    RSS rss = new RSS(TEST_PODCAST_TITLE, TEST_MP3_LINK);
    String testXmlString = RSS.toXmlString(rss);
    Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, testXmlString);
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
    RSS rss = new RSS(TEST_PODCAST_TITLE, TEST_MP3_LINK);
    String testXmlString = RSS.toXmlString(rss);

    Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, testXmlString);
    Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, testXmlString);
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
   * Expects that doGet() returns an error message when an entity with request id
   * does not exist in Datastore.
   */
  @Test
  public void doGet_EntityNotFound() throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_XML_STRING);
    ds.put(entity);
    String id = KeyFactory.keyToString(entity.getKey());
    ds.delete(entity.getKey());

    when(request.getParameter(ID)).thenReturn(id);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response, times(1)).setContentType("text/html");
    writer.flush();
    assertEquals("<p>Sorry. This is not a valid link.</p>", stringWriter.toString());
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

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Sorry, no matching Id was found in Datastore.");
    servlet.doGet(request, response);
  }
}

