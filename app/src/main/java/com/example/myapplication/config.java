package com.example.myapplication;

public class config {

    public static final String ip_address="http:172.20.104.251:8082";
    public static final String login_url=ip_address+"/login";
    public static final String send_url="http:localhost:5000/stream";
    public static final String update_url=ip_address+"/update";
    public static final String upload_url=ip_address+"/upload";

    public static final  int request_photo_code= 101;
    public static final  int open_picture_gallery= 102;
}
