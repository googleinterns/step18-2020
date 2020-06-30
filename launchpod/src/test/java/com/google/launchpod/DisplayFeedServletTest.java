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

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.launchpod.servlets.DisplayFeedServlet;
import com.google.launchpod.data.UserFeed;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

/** 
 * Runs unit tests for the DisplayFeedServlet that contains a doGet() method.
*/
@RunWith(JUnit4.class)
public class DisplayFeedServletTest extends Mockito {

    @InjectMocks
    private DisplayFeedServlet servlet = new DisplayFeedServlet();

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
     * Asserts that doGet() returns correct XML string when given an entity ID.
     * @throws Exception
    */
    @Test
    public void doGetReturnsCorrectXmlString() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
        Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
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
        assertEquals(stringWriter.toString(), "<p>" + TEST_XML_STRING + "</p>");
    }

    /**
     * Expects that doGet() throws an exception when an entity with request id does not exist in Datastore.
     * @throws Exception
    */
    @Test
    public void doGetEntityNotFound() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
        Entity entityTwo = makeEntity(TEST_PODCAST_TITLE, TEST_MP3_LINK, TEST_TIMESTAMP, TEST_XML_STRING, TEST_EMAIL);
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
        thrown.expect(Exception.class);
        thrown.expectMessage("Sorry. This is not a valid link.");
    }
    
}
