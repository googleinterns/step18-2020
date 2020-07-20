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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.launchpod.servlets.TranslationServlet;

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

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalTaskQueueTestConfig(),
      new LocalDatastoreServiceTestConfig());

  // keys
  private static final String USER_FEED = "UserFeed";
  private static final String LANGUAGE = "language";
  private static final String RSS_FEED_LINK = "rssFeedLink";
  private static final String XML_STRING = "xmlString";
  private static final String EMAIL = "email";
  private static final String TEST_LANGUAGE = "es";
  private static final String TEST_ID = "123456";
  private static final String TEST_EMAIL = "123@abc.com";
  private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com?id=";
  private static final String TEST_RSS_FEED_LINK = BASE_URL + TEST_ID;

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
    when(request.getParameter(LANGUAGE)).thenReturn(TEST_LANGUAGE);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    Query query = new Query(USER_FEED);
    assertEquals(1, datastore.prepare(query).countEntities(withLimit(10)));

    Entity testEntity = datastore.prepare(query).asSingleEntity();

    assertEquals(TEST_LANGUAGE, (String) testEntity.getProperty(LANGUAGE));
  }

  /**
   * Assert that doPost() obtains the correct input language from the form
   */
  @Test
  public void doPost_GeneratesCorrectIdFromForm() throws IOException {
    when(request.getParameter(RSS_FEED_LINK)).thenReturn(TEST_RSS_FEED_LINK);

    String testId = TranslationServlet.getIdFromUrl(TEST_RSS_FEED_LINK);

    assertEquals(TEST_ID, testId);
  }
}