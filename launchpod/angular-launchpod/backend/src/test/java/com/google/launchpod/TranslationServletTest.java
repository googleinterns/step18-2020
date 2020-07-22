package com.google.launchpod;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.launchpod.servlets.TranslationServlet;
import com.google.launchpod.data.RSS;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
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
public class TranslationServletTest extends Mockito{

  @InjectMocks
  private TranslationServlet servlet = new TranslationServlet();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  // keys
  private static final String USER_FEED = "UserFeed";
  private static final String LANGUAGE = "language";
  private static final String RSS_FEED_LINK = "rssFeedLink";
  private static final String XML_STRING = "xmlString";
  private static final String EMAIL = "email";
  private static final String TEST_PODCAST_TITLE = "American Life";
  private static final String TEST_LANGUAGE = "es";
  private static final String TEST_DESCRIPTION = "Episode one of the series";
  private static final String TEST_MP3_LINK = "this-is-a-test.mp3";
  private static final String TEST_ID = "123456";
  private static final String TEST_EMAIL = "123@abc.com";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com?id=";
  private static final RSS TEST_RSS_FEED = new RSS(TEST_PODCAST_TITLE, TEST_DESCRIPTION, TEST_LANGUAGE, TEST_EMAIL, TEST_MP3_LINK);

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
   * Assert that doPost() obtains the correct input language from the form
   */
  @Test
  public void doPost_StoresCorrectLanguageFromForm() throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String testXmlString = RSS.toXmlString(TEST_RSS_FEED);
    //Place item into local datastore
    Entity testEntity = new Entity(USER_FEED);
    testEntity.setProperty(EMAIL, TEST_EMAIL);;
    testEntity.setProperty(XML_STRING, testXmlString);
    datastore.put(testEntity);
    String testId = KeyFactory.keyToString(testEntity.getKey());

    when(request.getParameter(RSS_FEED_LINK)).thenReturn(BASE_URL + testId);
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(USER_FEED);

    Entity translatedTestEntity = datastore.prepare(query).asSingleEntity();
    System.out.println("test: " + TEST_LANGUAGE);
    System.out.println("test entity: " + translatedTestEntity);

    assertEquals(TEST_LANGUAGE, (String) translatedTestEntity.getProperty(LANGUAGE));
  }

  /**
   * Assert that doPost() obtains the correct input id from the rssFeedLink in the form
   */
  @Test
  public void doPost_GeneratesCorrectIdFromForm() throws IOException {
    when(request.getParameter(RSS_FEED_LINK)).thenReturn(BASE_URL + TEST_ID);

    String testId = TranslationServlet.getIdFromUrl(BASE_URL + TEST_ID);

    assertEquals(TEST_ID, testId);
  }
}