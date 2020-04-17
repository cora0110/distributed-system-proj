package model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

//TODO
public class Result implements Serializable {
    private int status;
    private String message;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public FileInputStream getFileInputStream() {
        return fileInputStream;
    }

    public FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }
}
