package client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalSession {
  private final String sessionToken;
  private final String username;
  private String occupiedFilename;
  private boolean isEditing;

  /**
   * Initializes with token and username.
   */
  LocalSession(String sessionToken, String username) {
    this.sessionToken = sessionToken;
    this.username = username;
    occupiedFilename = null;
  }

  boolean isEditing() {
    return occupiedFilename != null;
  }
}
