package com.example.myapplication;

public class config {

    public static final String ip_address="http://172.20.115.39:8082/";
    public static final String login_url=ip_address+"/login";
    public static final String send_url="http:localhost:5000/stream";
    public static final String update_url=ip_address+"/update";
    public static final String upload_url=ip_address+"/upload";
    public static final   String map_key="bbc86baf11332627b247bcbb69e452c8";
    public static final   String map_url="http://api.tianditu.gov.cn/geocoder?postStr=%1$s&type=geocode&tk=%2$s";
    public static final  int request_photo_code= 101;
    public static final  int open_picture_gallery= 102;
}
