package model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

//TODO
public class Result implements Serializable {
    public int status;
    public String message;
    public FileInputStream fileInputStream;
    public FileOutputStream fileOutputStream;

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
