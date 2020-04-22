package com.distributed.client;

import com.distributed.model.User;

/**
 * LocalSession.java
 * Keeps track of the current user and document information for the current session.
 *
 * @version 2020-4-21
 */
public class LocalSession {
  private final String sessionToken;
  private User user;
  private String occupiedFilePath;
  private String occupiedFileName;
  private int sectionIndex;

  /**
   * Initialize with token and username.
   */
  LocalSession(String sessionToken, User user) {
    this.sessionToken = sessionToken;
    this.user = user;
    occupiedFilePath = null;
  }

  boolean isEditing() {
    return occupiedFilePath != null;
  }

  String getSessionToken() {
    return this.sessionToken;
  }

  public User getUser() {
    return this.user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  String getOccupiedFilePath() {
    return occupiedFilePath;
  }

  void setOccupiedFilePath(String occupiedFilePath) {
    this.occupiedFilePath = occupiedFilePath;
  }

  String getOccupiedFileName() {
    return occupiedFileName;
  }

  void setOccupiedFileName(String occupiedFileName) {
    this.occupiedFileName = occupiedFileName;
  }

  int getSectionIndex() {
    return sectionIndex;
  }

  void setSectionIndex(int sectionIndex) {
    this.sectionIndex = sectionIndex;
  }
}
