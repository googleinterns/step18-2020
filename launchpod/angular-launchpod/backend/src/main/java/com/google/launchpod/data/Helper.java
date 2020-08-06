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
package com.google.launchpod.data;

import javax.servlet.http.HttpServletResponse;
import com.google.common.base.Strings;
import com.google.launchpod.data.Keys;
import com.google.launchpod.data.RSS;
import com.google.launchpod.data.Channel;

/*
 * Store helper methods used in servlets and test files.
 */
public class Helper {
  /**
  * Helper method for repeated code in catching exceptions.
  */
  public static void writeResponse(HttpServletResponse res, String message, int statusCode) throws IOException {
    res.setContentType("text/html");
    res.getWriter().println(message);
    res.setStatus(statusCode);
  }

  /**
  * Given and RSS feed and episode details, adds that episode to the RSS Feed and returns the XML of that modified feed.
  */
  public static String createModifiedXml(RSS rssFeed, String episodeTitle, String episodeDescription, String episodeLanguage, String email, String mp3Link) throws JsonProcessingException {
    Channel channel = rssFeed.getChannel();
    channel.addItem(episodeTitle, episodeDescription, episodeLanguage, email, mp3Link);
    String modifiedXmlString = RSS.toXmlString(rssFeed);
    return modifiedXmlString;
  }
}