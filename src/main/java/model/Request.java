package model;

import java.io.Serializable;

//TODO
public class Request implements Serializable {
    private String docName;
    private int sectionNum;
    private String token;
    private User targetUser;

    public String getDocName() {
        return docName;
    }

    public int getSectionNum() {
        return sectionNum;
    }

    public String getToken() {
        return token;
    }

    public User getTargetUser() {
        return targetUser;
    }
}
