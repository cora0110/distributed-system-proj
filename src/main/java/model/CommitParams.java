package model;

import java.io.FileInputStream;
import java.io.Serializable;

public class CommitParams implements Serializable {
    User user;
    // 1 stands for put, 0 stands for delete
    int type;
    FileInputStream fileInputStream;

    String docNanme;

    int sectionNum;

    int commitCode;
}
