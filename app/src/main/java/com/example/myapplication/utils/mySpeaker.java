package com.example.myapplication.utils;



import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class mySpeaker {
    private TextToSpeech textToSpeech = null;//创建自带语音对象

    public mySpeaker(Context context) {
        initTTS(context);
    }

    private void initTTS(Context context) {
        //实例化自带语音对象
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setPitch(1.0f);//方法用来控制音调
                textToSpeech.setSpeechRate(1.0f);//用来控制语速
            } else {
                Toast.makeText(context, "数据丢失或不支持", Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void startAuto(String data) {
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        textToSpeech.speak(data,//输入中文，若不支持的设备则不会读出来
                TextToSpeech.QUEUE_FLUSH, null);
    }
}
