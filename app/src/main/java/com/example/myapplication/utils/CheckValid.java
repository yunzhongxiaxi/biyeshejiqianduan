package com.example.myapplication.utils;

public class CheckValid {
    public static float getFloatFromString(String str,float min,float max,float defaultNum) {
        try {
            float value = Float.parseFloat(str);
            return value >= min && value <= max?value:defaultNum;
        } catch (NumberFormatException e) {
            // 如果解析失败，说明字符串不是一个有效的浮点数
            return defaultNum;
        }
    }
}
