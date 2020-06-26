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
import com.google.appengine.api.datastore.Query;
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
    private static final String MP3LINK = "mp3link"; // URL to existing MP3 file
    private static final String TIMESTAMP = "timestamp";

    private static final String TEST_PODCAST_TITLE = "TEST_PODCAST_TITLE";
    private static final String TEST_FILE_URL = "TEST_FILE_URL";
    private static final String TEST_MP3_LINK = "TEST_MP3_LINK";
    private static final long TEST_TIMESTAMP = System.currentTimeMillis();
    private static final String TEST_XML_STRING = "TEST_XML_STRING";
    private static final String EXPECTED_STRING = "EXPECTED_STRING";
    private static final String EMPTY_STRING = "";

    private static final Entity userFeedEntity = new Entity(USER_FEED);



    @Before 
    public void setUp() throws Exception {
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

    @Test
    public void doPostFormInput() throws IOException {
        // doPost() - gets inputs from UI form
        when(request.getParameter(PODCAST_TITLE)).thenReturn(TEST_PODCAST_TITLE);
        when(request.getParameter(MP3LINK)).thenReturn(TEST_MP3_LINK);

        userFeedEntity.setProperty(PODCAST_TITLE, TEST_PODCAST_TITLE);
        userFeedEntity.setProperty(MP3LINK, TEST_MP3_LINK);
        userFeedEntity.setProperty(TIMESTAMP, TEST_TIMESTAMP);
        userFeedEntity.setProperty(XML_STRING, TEST_XML_STRING);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(userFeedEntity);

        // TO-DO: verify response.sendRedirect()
    }

    @Test
    public void doPostStoreXML() throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);
        
        writer.flush();
        assertEquals(stringWriter.toString(), EXPECTED_STRING);

        // store RSS feed URL
        // store XML file to Google Cloud Storage
    }


    @Test 
    public void generateXMLFromJava() throws JsonProcessingException {
        String xml = xmlMapper.writeValueAsString(new SimpleBean());
        assertNotNull(xml);
    }

    @Test
    public void getUploadedObject() {
        // create URL from file uploaded to page
    }
    
}