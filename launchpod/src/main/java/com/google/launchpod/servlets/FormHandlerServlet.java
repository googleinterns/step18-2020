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
  private static final String PODCAST_NAME = "name";
  private static final String TIMESTAMP = "timestamp";
  private static final String MP3LINK = "mp3link";

  private static final String 

  /**
   *  request user inputs in form fields then create Entity and place in datastore
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String name = req.getParameter(NAME);
    String mp3Link = req.getParameter(MP3LINK);
    long timestamp = System.currentTimeMillis();

    Entity userFeedEntity = new Entity(USER_FEED);
    userFeedEntity.setProperty(NAME, name);
    userFeedEntity.setProperty(MP3LINK, mp3Link);
    userFeedEntity.setProperty(TIMESTAMP, timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userFeedEntity);
  }

  private String xmlString(String name, String mp3L){
    String xmlString = "";
    return xmlString

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
