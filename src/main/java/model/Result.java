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
  private FileInputStream fileInputStream;
  private FileOutputStream fileOutputStream;
  RemoteInputStream remoteInputStream;
  RemoteOutputStream remoteOutputStream;

  public Result(int status, String message) {
    this.status = status;
    this.message = message;
  }

}
