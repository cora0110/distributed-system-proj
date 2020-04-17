package model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

//TODO
@Getter
@Setter
public class Request implements Serializable {
    private String docName;
    private int sectionNum;
    private String token;
    private User targetUser;
}
