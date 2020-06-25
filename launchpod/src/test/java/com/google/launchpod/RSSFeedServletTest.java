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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.launchpod.servlets.RSSFeedServlet;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** */
@RunWith(JUnit4.class)
public class RSSFeedServletTest extends Mockito {

    @InjectMocks
    private RSSFeedServlet servlet = new RSSFeedServlet();

    @Mock 
    XmlMapper xmlMapper = new XmlMapper(); 

    private final String XML_NAME = "simple_bean.xml";

    @Before
    public void setUp() {
        // query = new FindMeetingQuery;
    }

    @Test
    public void doPost() throws IOException {
        // putting XML file in Blobstore
    }

    @Test 
    public void fromJavaGenerateXML() throws IOException {
        xmlMapper.writeValue(new File(XML_NAME), new SimpleBean());
        File file = new File(XML_NAME);
        assertNotNull(file);
    }

}