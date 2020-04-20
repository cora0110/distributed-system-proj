package com.distributed.client;

import com.distributed.model.User;

public class LocalSession {
  private final String sessionToken;
  private User user;
  private String occupiedFilePath;
  private String occupiedFileName;
  private int sectionIndex;

  /**
   * Initializes with token and username.
   */
  LocalSession(String sessionToken, User user) {
    this.sessionToken = sessionToken;
    this.user = user;
    occupiedFilePath = null;
  }

  boolean isEditing() {
    return occupiedFilePath != null;
  }

  public String getSessionToken() {
    return this.sessionToken;
  }

  public User getUser() {
    return this.user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getOccupiedFilePath() {
    return occupiedFilePath;
  }

  public void setOccupiedFilePath(String occupiedFilePath) {
    this.occupiedFilePath = occupiedFilePath;
  }

  public String getOccupiedFileName() {
    return occupiedFileName;
  }

  public void setOccupiedFileName(String occupiedFileName) {
    this.occupiedFileName = occupiedFileName;
  }

  public int getSectionIndex() {
    return sectionIndex;
  }

  public void setSectionIndex(int sectionIndex) {
    this.sectionIndex = sectionIndex;
  }
}
