package com.lobemusonda.schoolbustracker_driver;

public class Message {
    private String title, username, message, time;

    public Message(String title, String username, String message, String time) {
        this.title = title;
        this.username = username;
        this.message = message;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
