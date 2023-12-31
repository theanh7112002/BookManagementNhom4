package com.example.bookmanagement.Models;

public class ModelCategory {
    String id, theLoai, uid;
    String timestamp;

    public ModelCategory() {

    }

    public ModelCategory(String id, String theLoai, String uid, String timestamp) {
        this.id = id;
        this.theLoai = theLoai;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTheLoai() {
        return theLoai;
    }

    public void setTheLoai(String theLoai) {
        this.theLoai = theLoai;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
