package com.example.myapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class myTime {
    public  static String getCurrentTime(){
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("HH:mm");
        Date curDate =  new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

}
