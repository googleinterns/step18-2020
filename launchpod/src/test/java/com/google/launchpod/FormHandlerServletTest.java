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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.mockito.Mockito;

/** */
@RunWith(JUnit4.class)
public class FormHandlerServletTest extends Mockito {

    @InjectMocks 
    BlobStoreUploadUrlServlet servlet = new BlobstoreUploadUrlServlet();

    @Test
    public void testUpload() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse.response = mock(HttpServletResponse.class);

        when(request.getParameter("name")).thenReturn("name"); // TODO: replace "name" with static final variable
        when(request.getParameter("file")).thenReturn("file"); // TODO: replace "file" with static final variable

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);
        
        // verify
        writer.flush();
        assertTrue(StringWriter.toString().contains("My expected string"));
    }
    
}