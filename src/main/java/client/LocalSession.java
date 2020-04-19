package client;

import lombok.Getter;
import lombok.Setter;
import model.User;

@Getter
@Setter
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
}
