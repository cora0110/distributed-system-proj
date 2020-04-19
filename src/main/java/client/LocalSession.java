package client;

import model.User;

public class LocalSession {
  private final String sessionToken;
  private User user;
  private String occupiedFilename;
  private boolean isEditing;

  /**
   * Initializes with token and username.
   */
  LocalSession(String sessionToken, User user) {
    this.sessionToken = sessionToken;
    this.user = user;
    occupiedFilename = null;
  }

  boolean isEditing() {
    return occupiedFilename != null;
  }

    public String getSessionToken() {
        return this.sessionToken;
    }

    public User getUser() {
        return this.user;
    }

    public String getOccupiedFilename() {
        return this.occupiedFilename;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setOccupiedFilename(String occupiedFilename) {
        this.occupiedFilename = occupiedFilename;
    }

    public void setEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }
}
