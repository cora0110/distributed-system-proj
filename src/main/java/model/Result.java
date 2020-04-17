package model;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

//TODO
@Getter
@Setter
public class Result implements Serializable {
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
