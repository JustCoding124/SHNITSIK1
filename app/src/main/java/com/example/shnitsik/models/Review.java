package com.example.shnitsik.models;

/**
 * The type Review.
 */
public class Review {
    private String id;
    private String userId;
    private String text;
    private String userName;
    private String timestamp;


    /**
     * Instantiates a new Review.
     */
    public Review() {} // נדרש לפיירבייס

    /**
     * Instantiates a new Review.
     *
     * @param id        the id
     * @param userId    the user id
     * @param text      the text
     * @param userName  the user name
     * @param timestamp the timestamp
     */
    public Review(String id, String userId, String text, String userName, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.userName = userName;
        this.timestamp = timestamp;
    }


    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets user id.
     *
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets text.
     *
     * @param text the text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public String getTimestamp() {
        return this.timestamp;
    }
}
