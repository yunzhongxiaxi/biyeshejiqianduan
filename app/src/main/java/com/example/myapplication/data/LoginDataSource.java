package com.example.myapplication.data;

import com.example.myapplication.MyR;
import com.example.myapplication.config;
import com.example.myapplication.data.model.LoggedInUser;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("account", username);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            RequestBody requestJsonBody = RequestBody.create(
                    jsonObject.toString(),
                    MediaType.parse("application/json")
            );

            Request postRequest = new Request.Builder()
                    .url(config.login_url)
                    .post(requestJsonBody)
                    .build();
            OkHttpClient client=new OkHttpClient();
            Response response= client.newCall(postRequest).execute();
            Gson gson = new Gson();
            String jsonData =response.body().string(); // 从响应体获取的 JSON 字符串
            Type responseType = new TypeToken<MyR<LoggedInUser>>(){}.getType();
            MyR<LoggedInUser> received = gson.fromJson(jsonData, responseType);
            if (!received.getSuccess()){
                throw new Exception(received.getMessage());
            }

            return new Result.Success<>(received.getData());
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}