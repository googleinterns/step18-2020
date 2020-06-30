package com.google.launchpod.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.launchpod.data.UserFeed;

@WebServlet("/display-feed")
public class DisplayFeedServlet extends HttpServlet{

  private static final long serialVersionUID = 1L;
  private static final String ID = "id";

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
    res.setContentType("text/html");
    //Get ID passed in request
    String id = req.getParameter(ID);
    Query query = new Query(FormHandlerServlet.USER_FEED).addSort(FormHandlerServlet.TIMESTAMP, SortDirection.ASCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    //Turn ID back into a key
    Key desiredFeedKey = KeyFactory.stringToKey(id);

    //Search datastore for matching key, then display its XML string
    for(Entity entity: results.asIterable()){
      if(entity.getKey() == desiredFeedKey){
          UserFeed desiredUserFeed = UserFeed.fromEntity(entity);
          res.getWriter().println("<p>" + desiredUserFeed.xmlString + "</p>");
          return;
      }
    }
  }
}