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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.launchpod.servlets.BlobstoreUploadUrlServlet;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** */
@RunWith(JUnit4.class)
public final class BlobstoreUploadUrlServletTest extends Mockito {
    // static final variables
    // private FindMeetingQuery query;
    @InjectMocks

    private BlobstoreUploadUrlServlet servlet = new BlobstoreUploadUrlServlet();

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    // @Mock 
    // BlobstoreService blobstoreService;


    private final String TEST_NAME = "TEST_NAME";
    private final String TEST_UPLOAD_URL = "TEST_UPLOAD_URL";
    private final String TEST_FILE_URL = "TEST_FILE_URL";
    private final String EXPECTED_STRING = "EXPECTED_STRING";
    private final String TEST_URL = "/data";

    @Before 
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void returnCorrectUrl() throws IOException {

        // I don't know how to mock the BlobstoreServiceFactory (and there blobstoreService creation (because it's a static method)). Also can't test createUploadUrl because of this. My options are: change original code to move blobstoreService outside of method, try powerMock?, or assume things are correct up until setting the content type
        when(blobstoreService.createUploadUrl(TEST_URL)).thenReturn(TEST_UPLOAD_URL);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer); // uploadUrl lives in StringWriter()

        servlet.doGet(request, response);
        assertEquals(stringWriter.toString(), EXPECTED_STRING);
        // assertTrue(stringWriter.toString().contains(EXPECTED_STRING));
    }

    // don't need to check for invalid inputs since request and response aren't used in method
}

