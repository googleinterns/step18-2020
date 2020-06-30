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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.launchpod.servlets.FormHandlerServlet;
import com.google.launchpod.data.UserFeed;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.fasterxml.jackson.core.JsonProcessingException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
 * Runs unit tests for the FormHandlerServlet that contains doPost(), doGet(), and xmlString() methods.
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

    private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    // keys
    private static final String USER_FEED = "UserFeed";
    private static final String PODCAST_TITLE = "title";
    private static final String XML_STRING = "xmlString";
    private static final String MP3_LINK = "mp3link"; // URL to existing MP3 file
    private static final String TIMESTAMP = "timestamp";
    private static final String EMAIL = "email"; 

    private static final String TEST_PODCAST_TITLE = "TEST_PODCAST_TITLE";
    private static final String TEST_EMPTY_PODCAST_TITLE = "";
    private static final String TEST_NULL_PODCAST_TITLE = null;
    private static final String TEST_MP3_LINK = "TEST_MP3_LINK";
    private static final String TEST_EMPTY_MP3_LINK = "";
    private static final String TEST_NULL_MP3_LINK = null;
    private static final long TEST_TIMESTAMP = System.currentTimeMillis();
    private static final String TEST_ID = "123456";
    private static final String TEST_ID_TWO = "789012";
    private static final String TEST_XML_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  +
                       "<rss version=\"2.0\">" + 
                       "  <channel>" +
                       "    <language>en</language>"  +
                       "    <itunes:author>User</itunes:author>" + 
                       "    <title>" + TEST_PODCAST_TITLE + "</title>" + 
                       "    <item>" +
                       "      <title>" + TEST_PODCAST_TITLE + "</title>" + 
                       "      <summary>This is episode 4</summary>" + 
                       "      <description>This is episode 4</description>" + 
                       "      <link>" + TEST_MP3_LINK +"</link>" +
                       "      <enclosure url=\"" + TEST_MP3_LINK + "\" type=\"audio/mpeg\" length=\"185000\"/>" +
                       "      <pubDate>Thu, 20 Apr 2020 04:20:00 +0800</pubDate>" +
                       "      <itunes:author/>" + 
                       "      <itunes:duration>03:05</itunes:duration>" + 
                       "      <itunes:explicit>No</itunes:explicit>" + 
                       "      <guid isPermaLink=\"false\">uhwefpoihEOUUHSFEOIwqkhdho-=</guid>" + 
                       "    </item>" +
                       "  </channel>" +
                       "</rss>";
    private static final String TEST_PUBDATE = "2020/06/26 01:32:06";
    private static final String TEST_EMAIL = "123@abc.com";
    private static final String TEST_INCORRECT_EMAIL = "123@cde.com";
    private static final String TEST_EMAIL_TWO = "456@abc.com";
    private static final String EMPTY_STRING = "";

    private static final UserFeed TEST_USER_FEED = new UserFeed(TEST_PODCAST_TITLE, TEST_EMAIL, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING);


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
     * Creates a test DatastoreService with two test entities from two different users.
    */
    private void setUpTestData() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL));
        ds.put(makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL_TWO));
    }

    /**
     * Creates a test user feed entity.
    */
    private Entity makeEntity(String title, String mp3Link, long timestamp, String xmlString, String email) {
        Entity userFeedEntity = new Entity(USER_FEED);
        userFeedEntity.setProperty(PODCAST_TITLE, title);
        userFeedEntity.setProperty(MP3_LINK, mp3Link);
        userFeedEntity.setProperty(TIMESTAMP, timestamp);
        userFeedEntity.setProperty(XML_STRING, xmlString);
        userFeedEntity.setProperty(EMAIL, email);
        return userFeedEntity;
    }

    /**
     * Creates a test URL given the title of the podcast and the identifier key of its Datastore entity.
    */
    private String createUrl(String title, String id) {
        String url = "<div><p>" + title + "URL: <a href=\"https://launchpod-step18-2020.appspot.com/?id=" + id + "\">" + id + "</a></p></div>";
        return url;
    }

    /**
     * Asserts that doPost() takes in form inputs from client and successfully stores that information in a Datastore entity.
     * @throws Exception
    */
    @Test
    public void doPostStoresCorrectFormInput() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query("UserFeed");
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            assertEquals(entity.getProperty(PODCAST_TITLE), TEST_PODCAST_TITLE);
            assertEquals(entity.getProperty(MP3_LINK), TEST_MP3_LINK);
        }
    }

    /**
     * Expects doPost() to throw an Exception when the title field is empty.
     * @throws Exception
    */
    @Test 
    public void doPostFormInputEmptyTitle() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_EMPTY_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            thrown.expect(Exception.class);
            thrown.expectMessage("There was no title inputted. Try again");
        }
    }

    /**
     * Expects doPost() to throw an Exception when the title field is null.
     * @throws Exception
    */
    @Test 
    public void doPostFormInputNullTitle() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_NULL_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            thrown.expect(Exception.class);
            thrown.expectMessage("There was no title inputted. Try again");
        }
    }

    /**
     * Expects doPost() to throw an Exception when the MP3 link field is empty.
     * @throws Exception
    */
    @Test 
    public void doPostFormInputEmptyMp3Link() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_EMPTY_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            thrown.expect(Exception.class);
            thrown.expectMessage("There was no MP3 link inputted. Try again");
        }
    }

    /**
     * Expects doPost() to throw an Exception when the MP3 link field is null.
     * @throws Exception
    */
    @Test
    public void doPostFormInputNullMp3Link() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_NULL_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            thrown.expect(Exception.class);
            thrown.expectMessage("There was no MP3 link inputted. Try again");
        }
    }

    // This test can only be run if xmlString() is public
    @Test
    public void generateXmlString() {
        String xml = servlet.xmlString(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_PUBDATE);
        assertNotNull(xml);
        assertEquals(xml, TEST_XML_STRING);
    }

    /**
     * Asserts that doGet() returns correct URL when there's only one entity in Datastore period.
     * @throws Exception
    */
    @Test
    public void doGetReturnsOneUrlForOneUserOneEntity() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
        ds.put(entity);
        String id = KeyFactory.keyToString(entity.getKey());
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);
        
        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        String testUrl = createUrl(TEST_PODCAST_TITLE, id);
        assertEquals(stringWriter.toString(), testUrl); // only one URL in Datastore
    }

    /**
     * Asserts that doGet() returns correct URLs when there are multiple entities in Datastore belonging to the same user.
     * @throws Exception
    */
    @Test
    public void doGetReturnsMultipleUrlsForOneUser() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
        Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
        String id = KeyFactory.keyToString(entity.getKey());
        String idTwo = KeyFactory.keyToString(entityTwo.getKey());
        ds.put(entity);
        ds.put(entityTwo);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);
        
        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        String testUrls = createUrl(TEST_PODCAST_TITLE, id) + createUrl(TEST_PODCAST_TITLE, idTwo);
        assertEquals(stringWriter.toString(), testUrls);
    }

    /**
     * Asserts that doGet() returns the correct URL when there are multiple entities in Datastore belonging to different users.
     * @throws Exception
    */
    @Test
    public void doGetReturnsOneUrlForOneUserMultipleEntities() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
        Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL_TWO);
        String id = KeyFactory.keyToString(entity.getKey());
        String idTwo = KeyFactory.keyToString(entityTwo.getKey());
        ds.put(entity);
        ds.put(entityTwo);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);
        
        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        String testUrls = createUrl(TEST_PODCAST_TITLE, id) + createUrl(TEST_PODCAST_TITLE, idTwo);
        assertEquals(stringWriter.toString(), testUrls);
    }

    /**
     * Asserts that doGet() returns an error message when the request contains the incorrect user address.
     * @throws Exception
    */
    @Test
    public void doGetReturnsErrorMsgForIncorrectEmailAddress() throws Exception {
        setUpTestData();

        when(request.getParameter(EMAIL)).thenReturn(TEST_INCORRECT_EMAIL);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        assertEquals(stringWriter.toString(), "You have not created any RSS feeds.");
    }
    
    /**
     * Asserts that doGet() returns an error message when there are no entities in Datastore belonging to the user.
     * @throws Exception
    */
    @Test 
    public void doGetReturnsErrorMsgForNoEntitiesforUser() throws Exception {
        setUpTestData();

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        assertEquals(stringWriter.toString(), "You have not created any RSS feeds.");
    }

    // This test is the same as the one above because the cases of no entities for the user and no entities period in datastore have the same output. in this case, should I keep both tests just to keep track of what cases I've covered or should I just keep one?
    /**
     * Asserts that doGet() returns an error message when there are no entities in Datastore period.
     * @throws Exception
    */
    @Test
    public void doGetReturnsErrorMsgForNoEntitiesInDatastore() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        assertEquals(stringWriter.toString(), "You have not created any RSS feeds.");
    }
}
