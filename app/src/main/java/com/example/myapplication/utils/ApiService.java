package com.example.myapplication.utils;

import android.content.Context;

import com.example.myapplication.R;
import com.example.myapplication.config;

import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = createHttpClient();
            retrofit = new Retrofit.Builder()
                    .baseUrl(config.ip_address)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient client = createHttpClient(context);
            retrofit = new Retrofit.Builder()
                    .baseUrl(config.ip_address)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient createHttpClient() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);

        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
    }
    private static OkHttpClient createHttpClient(Context context) {
        return new OkHttpClient.Builder()
                .cookieJar(new PersistentCookieJar(context))
                .build();
    }

}

