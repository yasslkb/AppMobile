package com.example.projet_mobile;

import java.util.Date;

public class Comments {
    private String user_id ;
    private String message ;
    private Date timestamp ;

    public Comments(String user_id, String message, Date timestamp) {
        this.user_id = user_id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Comments() {
    }

    public String getUser_id() {
        return user_id;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
