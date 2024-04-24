package com.example.myapplication.utils;

import com.example.myapplication.MyR;
import com.example.myapplication.data.model.LoggedInUser;

import java.util.Map;
import java.util.Objects;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserApi {
    @POST("login")
    Call<MyR<LoggedInUser>> login(@Body Map<String,String> loginInfo);
    @POST("update")
    Call<MyR<Void>> updateUser(@Body Map<String, Object> userInfo);
    @POST("upload")
    @Multipart
    Call<MyR<Void>> uploadFile(@Part("userId") int userId, @Part MultipartBody.Part file);
}
