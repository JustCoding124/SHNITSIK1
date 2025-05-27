package com.example.shnitsik.models;

/**
 * Represents a review object.
 * This class encapsulates the details of a review, including its unique identifier (ID),
 * the ID of the user who wrote the review, the actual review content (text),
 * the username of the reviewer, and the timestamp indicating when the review was created.
 *
 * The class provides two constructors: a default no-argument constructor, which is
 * often required by frameworks like Firebase for deserialization, and a parameterized
 * constructor to initialize a Review object with all its attributes.
 *
 * It also includes standard getter methods to access the private fields and setter
 * methods to modify the `id`, `userId`, and `text` fields. The `userName` and `timestamp`
 * fields have only getter methods, implying they are typically set at creation and not modified afterwards.
 *
 * @author Ariel Kanitork
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
