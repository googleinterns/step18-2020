package com.google.launchpod.data;

public final class MP3 {

  private final String entityId;
  private final String userEmail;

  /**
   * Creates MP3 Object to store in Datastore.
   * @param entityId : name of the MP3 file (renamed to be the entity ID)
   * @param userEmail : email of user who uploaded the file
   */
  private MP3(String entityId, String userEmail) {
    this.entityId = entityId;
    this.userEmail = userEmail;
  }

  public String getEntityId(){
    return this.entityId;
  }

  public String getUserEmail() {
    return this.userEmail;
  }
}
