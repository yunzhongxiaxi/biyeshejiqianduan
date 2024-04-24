package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class PersistentCookieJar implements CookieJar {
    private SharedPreferences sharedPreferences;

    public PersistentCookieJar(Context context) {
        this.sharedPreferences = context.getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        // 保存 cookies 到 SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            // 只保存需要的 cookie 或设置过滤条件
            editor.putString(cookie.name(), cookie.toString());
        }
        editor.apply();
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        // 从 SharedPreferences 加载 cookies
        List<Cookie> cookies = new ArrayList<>();
        Map<String, ?> allCookies = sharedPreferences.getAll();
        for (String key : allCookies.keySet()) {
            String cookieString = sharedPreferences.getString(key, null);
            if (cookieString != null) {
                Cookie parsedCookie = Cookie.parse(url, cookieString);
                if (parsedCookie != null && parsedCookie.expiresAt() > System.currentTimeMillis()) {
                    cookies.add(parsedCookie);
                }
            }
        }
        return cookies;
    }
}
