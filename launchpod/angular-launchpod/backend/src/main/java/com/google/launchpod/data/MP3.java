package com.google.launchpod.data;

public final class MP3 {

  // TO-DO: rename fileName
  private final String fileName;
  private final String mp3Link;
  private final String userEmail;
  private final String pubDate;

  /**
   * Creates MP3 Object to store in Datastore.
   * @param fileName : name of the MP3 file
   * @param mp3Link : link to the MP3 file. TO-DO: figure out if we actually need this or not
   * @param userEmail : email of user who uploaded the file
   * @param pubDate : date MP3 file was published
   */
  private MP3(String fileName, String mp3Link, String userEmail, String pubDate) {
    this.fileName = fileName;
    this.mp3Link = mp3Link;
    this.userEmail = userEmail;
    this.pubDate = pubDate;
  }

  public String getFileName(){
    return this.fileName;
  }

  public String getLink(){
    return this.mp3Link;
  }

  public String getUserEmail() {
    return this.userEmail;
  }

  public String getPubDate(){
    return this.pubDate;
  }
}
