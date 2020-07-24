package com.google.launchpod.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import com.google.launchpod.data.Item;
import com.google.launchpod.data.RSS;

@WebServlet("/reverse-feed")
public class TranslationApiDelegate extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String USER_FEED = "UserFeed";
    private static final String RSS_FEED_LINK = "rssFeedLink";
    private static final String LANGUAGE = "language";
    private static final String EMAIL = "email";
    private static final String XML_STRING = "xmlString";
    private static final XmlMapper XML_MAPPER = new XmlMapper();
    private static final String BASE_URL = "https://launchpod-step18-2020.appspot.com/rss-feed?id=";

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws JsonParseException, JsonMappingException, IOException {
        String link = req.getParameter(RSS_FEED_LINK);
        String targetLanguage = req.getParameter(LANGUAGE);
        if (link == null || link == "") {
            throw new IOException("Please give valid link.");
        }
        if (targetLanguage == null || targetLanguage.isEmpty()) {
            throw new IOException("Please give valid language.");
        }
        try {
            XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            String id = getIdFromUrl(link);

            Key desiredFeedKey = KeyFactory.stringToKey(id);
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity desiredFeedEntity = datastore.get(desiredFeedKey);
            String xmlString = (String) desiredFeedEntity.getProperty(XML_STRING);
            RSS rssFeed = XML_MAPPER.readValue(xmlString, RSS.class);

            // Channel description
            String translate = Translate(rssFeed.getChannel().getDescription());
            rssFeed.getChannel().setDescription(translate);

            // Language
            rssFeed.getChannel().setLanguage(targetLanguage);

            // Episodes
            for (Item item : rssFeed.getChannel().getItem()) {
                // Episode title
                translate = Translate(item.getTitle());
                item.setTitle(translate);

                // Episode description
                translate = Translate(item.getDescription());
                item.setDescription(translate);
            }

            // Generate Translated XML string then place it into datastore
            String translatedXmlString = RSS.toXmlString(rssFeed);
            Entity translatedUserFeedEntity = new Entity(USER_FEED);
            translatedUserFeedEntity.setProperty(XML_STRING, translatedXmlString);
            translatedUserFeedEntity.setProperty(LANGUAGE, targetLanguage);
            translatedUserFeedEntity.setProperty(EMAIL, "123@abc.com");
            datastore.put(translatedUserFeedEntity);
            String translatedFeedId = KeyFactory.keyToString(translatedUserFeedEntity.getKey());

            // display new translated string to the user
            res.setContentType("text/html");
            res.getWriter().println(BASE_URL + translatedFeedId);

        } catch (EntityNotFoundException e) {
            res.sendError(HttpServletResponse.SC_CONFLICT, "Unable to translate. Try again");
            return;
        }
    }

    /**
     *  create a fake translated string for unit testing
     * @param text
     * @return reversed string
     */
    public static String Translate(String text) {
        if (text.length() <= 1) {
            return text;
        }
        return text.charAt(text.length() - 1) + text.substring(1, text.length() - 1) + text.charAt(0);
    }

    public static String getIdFromUrl(String rssLink) throws MalformedURLException {
        // Get the ID from the link that is being pasted
        URL feedUrl = new URL(rssLink);
        String queryString = feedUrl.getQuery();
        String[] querySplit = queryString.split("=");
        String id = querySplit[1];
        return id;
    }
}
