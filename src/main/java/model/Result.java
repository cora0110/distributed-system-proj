package model;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteOutputStream;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//TODO
@Getter
@Setter
@NoArgsConstructor
public class Result implements Serializable {
  List<String> unreadNotifications;
  private int status;
  private String message;
  private RemoteInputStream remoteInputStream;
  private RemoteOutputStream remoteOutputStream;

  public Result(int status, String message) {
    this.status = status;
    this.message = message;
  }

  public Result(int status, String message, RemoteInputStream remoteInputStream) {
    this.status = status;
    this.message = message;
    this.remoteInputStream = remoteInputStream;
  }
}
