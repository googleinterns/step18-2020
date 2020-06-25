package com.google.launchpod.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/form-handler")
public class FormHandlerServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    
  }

  private String getUploadedFileUrl(HttpServletRequest req, String inputName){
    BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstore.getUploads(req);
    List<BlobKey> keys = blobs.get(inputName);

    //When there is nothing uploaded
    if(keys.isEmpty() || keys == null){
      return null;
    }
    //Get first input from form
    BlobKey key = keys.get(0);

    //when user does not select form
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(key);
    if(blobInfo.getSize() == 0){
      blobstore.delete(key);
      return null;
    }

    return "";
  }
}
