package com.google.launchpod.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/rss-feed")
public class FormHandlerServlet extends HttpServlet {
  
  private static final String USER_FEED = "UserFeed";
  private static final String PODCAST_TITLE = "title";
  private static final String TIMESTAMP = "timestamp";
  private static final String MP3LINK = "mp3link";
  private static final String XML_STRING = "xmlString";

  /**
   *  request user inputs in form fields then create Entity and place in datastore
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String podcastTitle = req.getParameter(PODCAST_TITLE);
    String mp3Link = req.getParameter(MP3LINK);
    long timestamp = System.currentTimeMillis();
    String xmlString - xmlString(name, mp3Link);

    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(PODCAST_TITLE, podcastTitle);
    userFeedEntity.setProperty(MP3LINK, mp3Link);
    userFeedEntity.setProperty(TIMESTAMP, timestamp);
    userFeedEntity.setProperty(XML_STRING ,xmlString);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);
  }

  private String xmlString(String name, String mp3Link){
    String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  +
                       "<rss version=\"2.0\">" + 
                       "  <channel>" +
                       "    <language>en</language>"  +
                       "    <itunes:author>User</itunes:author>" + 
                       "    <title>" + name + "</title>" + 
                       "    <item>" +
                       "      <title>" + name + "</title>" + 
                       "      <summary>This is episode 4</summary>" + 
                       "      <description>This is episode 4</description>" + 
                       "      <link>" + mp3Link +"</link>" +
                       "      <enclosure url=\"" + mp3Link + "\" type=\"audio/mpeg\" length=\"185000\"/>" +
                       "      <pubDate>Thu, 20 Apr 2020 04:20:00 +0800</pubDate>" +
                       "      <itunes:author/>" + 
                       "      <itunes:duration>03:05</itunes:duration>" + 
                       "      <itunes:explicit>No</itunes:explicit>" + 
                       "      <guid isPermaLink=\"false\">uhwefpoihEOUUHSFEOIwqkhdho-=</guid>" + 
                       "    </item>"
                       "  </channel>"
                       "</rss>";

    return xmlString;

  }

  public Document generateXMLfromJava(){
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;

    try{
        //Create DocumentBuilder for string
        builder = factory.newDocumentBuilder();
         
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        return doc;

    }catch (Exception e) {
        e.printStackTrace();
    }
    return null;
  }
}
