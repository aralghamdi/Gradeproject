package com.example.gradeprojectv10;

public class Requests {  // this class is to retrieve the Requests from the database
    public String uid , name, date, time, location, description, mobile;

    public  Requests(){


    }

    // store the Requests information
    public Requests(String uid, String name, String date, String time, String location, String description, String mobile) {
        this.uid = uid;
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.mobile = mobile ;
    }
    // get the mobile
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    // get the user id
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    // get the user name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // get the request date
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    // get the request time
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    // get the customer location
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    // get the request description
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


}
// End