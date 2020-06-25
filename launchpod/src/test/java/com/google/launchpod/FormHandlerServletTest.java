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

import com.google.launchpod.servlets.FormHandlerServlet;

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

    private final String TEST_NAME = "TEST_NAME";
    private final String TEST_FILE_URL = "TEST_FILE_URL";
    private final String EXPECTED_STRING = "EXPECTED_STRING";

    @Before 
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void doPost() throws IOException {

        when(request.getParameter("name")).thenReturn(TEST_NAME);
        when(request.getParameter("file")).thenReturn(TEST_FILE_URL);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);
        
        writer.flush();
        assertEquals(stringWriter.toString(), EXPECTED_STRING);
    }

    // No tests for private methods getUploadedFileUrl or getParameter
    
}