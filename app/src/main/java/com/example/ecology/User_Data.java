package com.example.ecology;

public class User_Data {
    private String pass;
    private String email;
    private Double lat;
    private Double lang;

    public User_Data(String password, String email,Double latitude,Double longitude) {
        this.email = email;
        this.pass = password;
        this.lat=latitude;
        this.lang=longitude;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLang() {
        return lang;
    }

    public void setLang(Double lang) {
        this.lang = lang;
    }

}
