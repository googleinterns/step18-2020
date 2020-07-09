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
    private static final String TEST_MP3_LINK = "TEST_MP3_LINK";
    private static final long TEST_TIMESTAMP = System.currentTimeMillis();
    private static final String TEST_ID = "123456";
    private static final String TEST_ID_TWO = "789012";
    private static final String TEST_PUBDATE = "2020/06/26 01:32:06";
    private static final String TEST_EMAIL = "123@abc.com";
    private static final String TEST_INCORRECT_EMAIL = "123@cde.com";
    private static final String TEST_EMAIL_TWO = "456@abc.com";
    private static final String EMPTY_STRING = "";
    private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com?id=";

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
     * Asserts that doPost() takes in form inputs from client and successfully stores that information in a Datastore entity.
    */
    @Test
    public void doPost_StoresCorrectFormInput() throws IOException {
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

        assertEquals(TEST_PODCAST_TITLE, entity.getProperty(PODCAST_TITLE).toString());
        assertEquals(TEST_MP3_LINK, entity.getProperty(MP3_LINK).toString());
    }

    /**
     * Asserts that doPost() takes in form inputs from client, successfully stores that information in a Datastore entity,
     * and returns a URL link to the generated RSS feed.
    */
    @Test
    public void doPost_ReturnsCorrectUrl() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);

        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        Entity entity = ds.prepare(query).asSingleEntity();

        assertEquals(TEST_PODCAST_TITLE, entity.getProperty(PODCAST_TITLE).toString());
        assertEquals(TEST_MP3_LINK, entity.getProperty(MP3_LINK).toString());
        RSS rssFeed = new RSS(TEST_PODCAST_TITLE, TEST_MP3_LINK);
        TEST_XML_STRING = RSS.toXmlString(rssFeed);
        assertEquals(TEST_XML_STRING, entity.getProperty(XML_STRING).toString());

        String id = Long.toString(entity.getKey().getId());
        String rssLink = BASE_URL + id;

        verify(response, atLeast(1)).setContentType("text/html");
        assertEquals(rssLink, stringWriter.toString());
    }

    /**
     * Expects doPost() to throw an IOException when the title field is empty.
    */
    @Test
    public void doPost_FormInputEmptyTitle_ThrowsErrorMessage() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn("");
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);

        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("No Title inputted, please try again.");
    }

    /**
     * Expects doPost() to throw an IOException when the title field is null.
    */
    @Test
    public void doPost_FormInputNullTitle_ThrowsErrorMessage() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(null);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);

        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("No Title inputted, please try again.");
    }

    /**
     * Expects doPost() to throw an IOException when the MP3 link field is empty.
    */
    @Test
    public void doPost_FormInputEmptyMp3Link_ThrowsErrorMessage() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn("");

        servlet.doPost(request, response);

        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        PreparedQuery results = ds.prepare(query);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("No Mp3 inputted, please try again.");
    }

    /**
     * Expects doPost() to throw an IOException when the MP3 link field is null.
    */
    @Test
    public void doPost_FormInputNullMp3Link_ThrowsErrorMessage() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(null);

        servlet.doPost(request, response);

        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query(USER_FEED);
        Entity entity = ds.prepare(query).asSingleEntity();
        thrown.expect(IOException.class);
        thrown.expectMessage("No Mp3 inputted, please try again.");
    }

    /**
     * Asserts that doGet() returns correct XML string when given an entity ID, with one entity in Datstore.
    */
    @Test
    public void doGet_SingleEntity_ReturnsCorrectXmlString() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_XML_STRING);
        String id = KeyFactory.keyToString(entity.getKey());
        ds.put(entity);

        when(request.getParameter(ID)).thenReturn(id);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        RSS rss = new RSS(entity.getProperty(PODCAST_TITLE).toString(), entity.getProperty(MP3_LINK).toString());
        String test_xml_string = RSS.toXmlString(rss);

        verify(response, exactly(1)).setContentType("text/html");
        writer.flush();
        assertEquals("<p>" + test_xml_string + "</p>", stringWriter.toString());
    }

    /**
     * Asserts that doGet() returns correct XML string when given an entity ID, with multiple entities in Datastore.
    */
    @Test
    public void doGet_MultipleEntities_ReturnsCorrectXmlString() throws IOException {
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

        RSS rss = new RSS(entity.getProperty(PODCAST_TITLE).toString(), entity.getProperty(MP3_LINK).toString());
        String test_xml_string = RSS.toXmlString(rss);

        verify(response, exactly(1)).setContentType("text/html");
        writer.flush();
        assertEquals("<p>" + test_xml_string + "</p>", stringWriter.toString());
    }

    /**
     * Expects that doGet() returns an error message when an entity with request id does not exist in Datastore.
    */
    @Test
    public void doGet_EntityNotFound() throws IOException {
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

        verify(response, exactly(1)).setContentType("text/html");
        writer.flush();
        assertEquals("<p>Sorry. This is not a valid link.</p>", stringWriter.toString());
    }

    /**
     * Asserts that doGet() returns an error message when there are no entities in Datastore period.
     * TO-DO: add this test to testing file for LoginServlet (MVP)
    */
    @Test
    public void doGet_ReturnsErrorMsgForNoEntitiesInDatastore() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response, exactly(1)).setContentType("text/html");
        writer.flush();
        assertEquals("You have not created any RSS feeds.", stringWriter.toString());
    }
}
