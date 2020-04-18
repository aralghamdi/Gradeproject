package com.example.gradeprojectv10;

public class Chats { // this class is to retrieve the chats from the database

    // create string variables to store the message information
    public String date, from, message, time;

    public Chats(){
    }

    // store the message information
    public Chats(String date, String from, String message, String time) {
        this.date = date;
        this.from = from;
        this.message = message;
        this.time = time;
    }

    // get the message date
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    // get the message sender name
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    //get the text of the message
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    //get the time of the messages
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
// End
