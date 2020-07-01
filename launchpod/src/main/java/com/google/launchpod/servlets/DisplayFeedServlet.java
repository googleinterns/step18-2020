package com.google.launchpod.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.launchpod.data.UserFeed;

@WebServlet("/display-feed")
public class DisplayFeedServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String ID = "id";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setContentType("text/html");
    // Get ID passed in request
    String id = req.getParameter(ID);
    //set filter to search for id
    Filter idFilter = new FilterPredicate(ID, FilterOperator.EQUAL, id);
    Query query = new Query(FormHandlerServlet.USER_FEED).setFilter(idFilter);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    //create entity that contains id from datastore
    Entity desiredFeedEntity = datastore.prepare(query).asSingleEntity();

    //If there is no data prepared as entity return invalid link
    if(desiredFeedEntity == null){
      res.getWriter().println("<p>Sorry. This is not a valid link.</p>");
      return;
    }
    // generate xml string
    UserFeed desiredUserFeed = UserFeed.fromEntity(desiredFeedEntity);
    XmlMapper xmlMapper = new XmlMapper();
    String xmlString = xmlMapper.writeValueAsString(desiredUserFeed);
    res.getWriter().println("<p>" + xmlString + "</p>");
  }
}