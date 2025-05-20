package com.example.shnitsik;

public class Review {
    private String id;
    private String userId;
    private String text;
    private String userName;
    private String timestamp;


    public Review() {} // נדרש לפיירבייס

    public Review(String id, String userId, String text, String userName, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.userName = userName;
        this.timestamp = timestamp;
    }


    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getTimestamp() {
        return this.timestamp;
    }
}
