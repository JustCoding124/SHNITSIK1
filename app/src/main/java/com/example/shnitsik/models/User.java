package com.example.shnitsik.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * The type User.
 */
public class User {
    private String userName;
    /**
     * The Role.
     */
    boolean role;//אם "אמת" אז זה אדמין אם "שקר" אז זה משתמש רגיל
    private String id;
    private String email;

    /**
     * Instantiates a new User.
     *
     * @param userName the user name
     * @param id       the id
     * @param email    the email
     * @param role     the role
     */
    public User(String userName,String id, String email,boolean role) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.userName = userName;
    }

    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets user name.
     *
     * @param userName the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
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
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets role.
     *
     * @return the role
     */
    public boolean getRole() {return role;}

    /**
     * Sets role.
     *
     * @param role the role
     */
    public void setRole(boolean role) {this.role = role;}


}
