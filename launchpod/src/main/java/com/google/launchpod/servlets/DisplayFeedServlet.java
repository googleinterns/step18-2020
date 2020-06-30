package com.google.launchpod.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.launchpod.data.UserFeed;

@WebServlet("/display-feed")
public class DisplayFeedServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String ID = "id";

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        // Get ID passed in request
        String id = req.getParameter(ID);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Turn ID back into a key
        Key desiredFeedKey = KeyFactory.stringToKey(id);

        // Search datastore for matching key, then display its XML string
        UserFeed desiredUserFeed;
        try {
            desiredUserFeed = UserFeed.fromEntity(datastore.get(desiredFeedKey));
            res.getWriter().println("<p>" + desiredUserFeed.xmlString + "</p>");
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            res.getWriter().println("<p>Sorry. This is not a valid link.</p>");
        }
    return;
  }
}