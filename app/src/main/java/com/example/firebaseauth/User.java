package com.example.firebaseauth;

import com.google.firebase.firestore.DocumentId;

public class User {

    @DocumentId
    public String uid;
    public String name;
    public String contact;

    public User() {

    }

    public User(String uid, String name, String contact) {
        this.uid = uid;
        this.name = name;
        this.contact = contact;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
