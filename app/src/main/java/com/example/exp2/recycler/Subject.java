package com.example.exp2.recycler;

public class Subject {
    public String id;
    public String name;

    // Required default constructor for Firebase
    public Subject() {
    }

    public Subject(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
