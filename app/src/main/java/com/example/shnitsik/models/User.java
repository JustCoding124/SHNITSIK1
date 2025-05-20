package com.example.shnitsik;
import android.view.OnReceiveContentListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class User {
    private String userName;
    boolean role;//אם "אמת" אז זה אדמין אם "שקר" אז זה משתמש רגיל
    private String id;
    private String email;
    private LinkedList<Order> orders = new LinkedList<Order>();
    public User(String userName,String id, String email,boolean role) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkedList<Order> getPastOrders() {
        return this.orders;
    }

    public void setPastOrders(int len, Order[] pastOrders) {
        this.orders.addAll(Arrays.asList(pastOrders));
        sortOrders();
    }
    public void sortOrders(){
        Collections.sort(this.orders, (o1, o2) -> Long.compare(o2.getDateOfOrder(), o1.getDateOfOrder()));
    }
}
