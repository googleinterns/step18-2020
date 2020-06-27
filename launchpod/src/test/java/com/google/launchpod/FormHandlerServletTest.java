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

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.launchpod.servlets.FormHandlerServlet;
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
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** */
@RunWith(JUnit4.class)
public class FormHandlerServletTest extends Mockito {

    @InjectMocks
    private FormHandlerServlet servlet = new FormHandlerServlet();

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock 
    XmlMapper xmlMapper = new XmlMapper(); 

    // Maximum eventual consistency.
    private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    // keys
    private static final String USER_FEED = "UserFeed";
    private static final String XML_NAME = "simple_bean.xml";
    private static final String PODCAST_TITLE = "title"; // TO-DO: verify key strings with Efrain
    private static final String XML_STRING = "xmlString";
    private static final String MP3_LINK = "mp3link"; // URL to existing MP3 file
    private static final String TIMESTAMP = "timestamp";
    private static final String EMAIL = "email"; 

    private static final String TEST_PODCAST_TITLE = "TEST_PODCAST_TITLE";
    private static final String TEST_EMPTY_PODCAST_TITLE = "";
    private static final String TEST_NULL_PODCAST_TITLE = null;
    private static final String TEST_FILE_URL = "TEST_FILE_URL";
    private static final String TEST_MP3_LINK = "TEST_MP3_LINK";
    private static final String TEST_EMPTY_MP3_LINK = "";
    private static final String TEST_NULL_MP3_LINK = null;
    private static final long TEST_TIMESTAMP = System.currentTimeMillis();
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
    private static final String EXPECTED_STRING = "EXPECTED_STRING";
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

    // @Test
    // public void testEventuallyConsistentGlobalQueryResult() {
    //   DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    //   // Key ancestor = KeyFactory.createKey("foo", 3);
    //   //   ds.put(new Entity("yam", ancestor));
    //   //   ds.put(new Entity("yam", ancestor));

    //   // Global query doesn't see the data.
    //   assertEquals(0, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
    //   // Ancestor query does see the data.
    //   assertEquals(2, ds.prepare(new Query("yam", ancestor)).countEntities(withLimit(10)));
    // }

    private void setUpTestData() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(makeEntity());
    }

    private Entity makeEntity() {
        Entity userFeedEntity = new Entity(USER_FEED);
        userFeedEntity.setProperty(PODCAST_TITLE, TEST_PODCAST_TITLE);
        userFeedEntity.setProperty(MP3_LINK, TEST_MP3_LINK);
        userFeedEntity.setProperty(TIMESTAMP, TEST_TIMESTAMP);
        userFeedEntity.setProperty(XML_STRING, TEST_XML_STRING);
        return userFeedEntity;
    }

    @Test
    public void doPostFormInput() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));
        // TO-DO: verify that it returns the URL in the response to the client
    }

    @Test 
    public void doPostFormInputEmptyTitle() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_EMPTY_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query("UserFeed");
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            assertEquals(entity.getProperty(PODCAST_TITLE), "Podcast");
        }
        // TO-DO: verify that it returns the URL in the response to the client
    }

    @Test 
    public void doPostFormInputNullTitle() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_NULL_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query("UserFeed");
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            assertEquals(entity.getProperty(PODCAST_TITLE), "Podcast");
        }
        // TO-DO: verify that it returns the URL in the response to the client
    }

    @Test 
    public void doPostFormInputEmptyMp3Link() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_EMPTY_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query("UserFeed");
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            assertEquals(entity.getProperty(MP3_LINK), "None Listed");
        }
        // TO-DO: verify that it returns the URL in the response to the client
    }

    @Test 
    public void doPostFormInputNullMp3Link() throws IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3_LINK)).thenReturn(TEST_NULL_MP3_LINK);

        servlet.doPost(request, response);
    
        assertEquals(1, ds.prepare(new Query(USER_FEED)).countEntities(withLimit(10)));

        Query query = new Query("UserFeed");
        PreparedQuery results = ds.prepare(query);
        for (Entity entity : results.asIterable()) {
            assertEquals(entity.getProperty(MP3_LINK), "None Listed");
        }
        // TO-DO: verify that it returns the URL in the response to the client
    }

    // TO-DO: verify that MP3 links are valid?

    // This test can only be run if xmlString is public
    @Test
    public void generateXmlString() {
        String xml = servlet.xmlString(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_PUBDATE);
        assertNotNull(xml);
        assertEquals(xml, TEST_XML_STRING);
    }

    @Test
    public void doGetReturnAllEntities() throws IOException {
        setUpTestData();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);
        
        //TO-DO: change this to xml
        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();
        assertEquals(stringWriter.toString(), "<div><p>" + TEST_XML_STRING + "</p></div>");
    }

    @Test
    public void doGetNoEntitiesInDatastore() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response, atLeast(1)).setContentType("text/html");
        writer.flush();

        //TO-DO: check with Efrain on implementing a check for empty Datastore
        assertEquals(stringWriter.toString(), "You have not created any RSS feeds.");
    }

    // @Test
    // public void doPostStoreXML() throws IOException {
    //     StringWriter stringWriter = new StringWriter();
    //     PrintWriter writer = new PrintWriter(stringWriter);
    //     when(response.getWriter()).thenReturn(writer);

    //     servlet.doPost(request, response);
        
    //     writer.flush();
    //     assertEquals(stringWriter.toString(), EXPECTED_STRING);

    //     // store RSS feed URL
    //     // store XML file to Google Cloud Storage
    // }


    // @Test 
    // public void generateXMLFromJava() throws JsonProcessingException {
    //     String xml = xmlMapper.writeValueAsString(new SimpleBean());
    //     assertNotNull(xml);
    // }
    
}