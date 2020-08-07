package com.google.launchpod;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.testing.RemoteTranslateHelper;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.launchpod.data.Keys;
import com.google.launchpod.data.TestKeys;
import com.google.launchpod.data.Item;
import com.google.launchpod.data.RSS;
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
public class TranslationServletTest extends Mockito {

  @InjectMocks
  private TranslationServlet servlet = new TranslationServlet();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String TEST_TARGET_LANGUAGE = "es";
  private static final RSS TEST_RSS_FEED = new RSS(TestKeys.TEST_NAME, TestKeys.TEST_EMAIL, TestKeys.TEST_TITLE, TestKeys.TEST_DESCRIPTION, TestKeys.TEST_LANGUAGE, TestKeys.TEST_CATEGORY);
  private RemoteTranslateHelper translateHelper = RemoteTranslateHelper
      .create("AIzaSyBlu9s7xFLjlHAdvlVuUISq_MbyELuCRZo");
  private Translate translateMock = translateHelper.getOptions().getService();

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
  .setEnvIsLoggedIn(true).setEnvEmail(TestKeys.TEST_EMAIL).setEnvAuthDomain("localhost");

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
   * Expect doPost to throw IOException when language field is empty
   * 
   * @throws IOException
   */
  @Test
  public void doPost_ThrowsException_WhenLanguageInputEmpty() throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter(Keys.LANGUAGE)).thenReturn("");
    when(request.getParameter(Keys.RSS_FEED_LINK)).thenReturn(Keys.BASE_URL + TestKeys.TEST_ID);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Please give valid language.");

    servlet.doPost(request, response);

    assertEquals(0, datastore.prepare(new Query(Keys.USER_FEED)).countEntities(withLimit(5)));
  }

  /**
   * Expect doPost to throw IOException when rssFeedLink field is empty
   * 
   * @throws IOException
   */
  @Test
  public void doPost_ThrowsException_WhenRssFeedLinkInputEmpty() throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter(Keys.LANGUAGE)).thenReturn(TestKeys.TEST_LANGUAGE);
    when(request.getParameter(Keys.RSS_FEED_LINK)).thenReturn("");

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Please give valid link.");

    servlet.doPost(request, response);

    assertEquals(0, datastore.prepare(new Query(Keys.USER_FEED)).countEntities(withLimit(5)));
  }

  /**
   * Assert that getIdFromUrl() obtains the correct input id from the rssFeedLink
   * in the form
   */
  @Test
  public void getIdFromUrl_GeneratesCorrectId() throws IOException {
    String testId = TranslationServlet.getIdFromUrl(Keys.BASE_URL + TestKeys.TEST_ID);

    assertEquals(TestKeys.TEST_ID, testId);
  }
}
