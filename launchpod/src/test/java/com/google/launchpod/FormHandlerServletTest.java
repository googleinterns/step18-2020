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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
    private static final String ID = "id";

    private static final String TEST_PODCAST_TITLE = "TEST_PODCAST_TITLE";
    private static final String TEST_EMPTY_PODCAST_TITLE = "";
    private static final String TEST_NULL_PODCAST_TITLE = null;
    private static final String TEST_MP3_LINK = "TEST_MP3_LINK";
    private static final String TEST_EMPTY_MP3_LINK = "";
    private static final String TEST_NULL_MP3_LINK = null;
    private static final long TEST_TIMESTAMP = System.currentTimeMillis();
    private static final String TEST_ID = "123456";
    private static final String TEST_ID_TWO = "789012";
    private static final String TEST_XML_STRING;
    private static final String TEST_PUBDATE = "2020/06/26 01:32:06";
    private static final String TEST_EMAIL = "123@abc.com";
    private static final String TEST_INCORRECT_EMAIL = "123@cde.com";
    private static final String TEST_EMAIL_TWO = "456@abc.com";
    private static final String EMPTY_STRING = "";
    private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com?id=";

    // private static final UserFeed TEST_USER_FEED = new UserFeed(TEST_PODCAST_TITLE, TEST_EMAIL, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING);

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
     * Creates a test URL given the title of the podcast and the identifier key of its Datastore entity.
    */
    private String createUrl(String title, String id) {
        String url = "<div><p>" + title + "URL: <a href=\"https://launchpod-step18-2020.appspot.com/?id=" + id + "\">" + id + "</a></p></div>";
        return url;
    }

    /**
     * Create RSS XML string given a user feed entity.
     * @return xml String
     * @throws IOException
    */
    private static String xmlString(RSS rssFeed) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        String xmlString = xmlMapper.writeValueAsString(rssFeed);
        return xmlString;
    }

    /**
     * Asserts that doPost() takes in form inputs from client, successfully stores that information in a Datastore entity, 
     * and returns a URL link to the generated RSS feed.
     * @return URL link to the generated RSS Feed
     * @throws IOException
    */
    @Test
    public void doPostStoresCorrectFormInputReturnsCorrectUrl() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query("UserFeed");
        Entity entity = ds.prepare(query).asSingleEntity();

        assertEquals(entity.getProperty(PODCAST_TITLE), TEST_PODCAST_TITLE);
        assertEquals(entity.getProperty(MP3_LINK), TEST_MP3_LINK);
        RSS rssFeed = new RSS(TEST_PODCAST_TITLE, TEST_MP3_LINK);
        TEST_XML_STRING = xmlString(rssFeed);
        assertEquals(entity.getProperty(XML_STRING), TEST_XML_STRING);

        String id = Long.toString(entity.getKey().getId());
        String rssLink = BASE_URL + id;

        verify(response, atLeast(1)).setContentType("text/html");
        String expectedUrl = "<a href=\"" + rssLink + "\">" + rssLink + "</a>";
        assertEquals(expectedUrl, stringWriter.toString());
    }

    /**
     * Expects doPost() to throw an IOException when the title field is empty.
     * @throws IOException
    */
    @Test 
    public void doPostFormInputEmptyTitle() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_EMPTY_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("There was no title inputted. Try again");
    }

    /**
     * Expects doPost() to throw an IOException when the title field is null.
     * @throws IOException
    */
    @Test 
    public void doPostFormInputNullTitle() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_NULL_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("There was no title inputted. Try again");
    }

    /**
     * Expects doPost() to throw an IOException when the MP3 link field is empty.
     * @throws IOException
    */
    @Test 
    public void doPostFormInputEmptyMp3Link() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_EMPTY_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("There was no MP3 link inputted. Try again");
    }

    /**
     * Expects doPost() to throw an IOException when the MP3 link field is null.
     * @throws IOException
    */
    @Test
    public void doPostFormInputNullMp3Link() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_NULL_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("There was no MP3 link inputted. Try again");
    }

    /**
     * Asserts that doGet() returns correct XML string when given an entity ID.
     * @return XML string 
     * @throws IOException
    */
    @Test
    public void doGetReturnsCorrectXmlString() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_XML_STRING);
        Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_XML_STRING);
        String id = KeyFactory.keyToString(entity.getKey());
        String idTwo = KeyFactory.keyToString(entityTwo.getKey());
        ds.put(entity);
        ds.put(entityTwo);

        when(request.getParameter(ID)).thenReturn(id);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        RSS rss = new RSS(entity.getProperty(PODCAST_TITLE), entity.getProperty(MP3_LINK));
        TEST_XML_STRING = xmlString(rss);

        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        assertEquals(stringWriter.toString(), "<p>" + TEST_XML_STRING + "</p>");
    }

    /**
     * Expects that doGet() returns an error message when an entity with request id does not exist in Datastore.
     * @return invalid link message
     * @throws IOException
    */
    @Test
    public void doGetEntityNotFound() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_XML_STRING);
        Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_XML_STRING);
        String id = KeyFactory.keyToString(entity.getKey());
        String idTwo = KeyFactory.keyToString(entityTwo.getKey());
        ds.put(entity);
        ds.put(entityTwo);

        when(request.getParameter(ID)).thenReturn(id);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        assertEquals("<p>Sorry. This is not a valid link.</p>", stringWriter.toString());
    }

    /**
     * Asserts that doGet() returns an error message when there are no entities in Datastore period.
     * @throws IOException
    */
    @Test
    public void doGetReturnsErrorMsgForNoEntitiesInDatastore() throws IOException {
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
