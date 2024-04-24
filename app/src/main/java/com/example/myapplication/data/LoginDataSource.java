package com.example.myapplication.data;

import com.example.myapplication.MyR;
import com.example.myapplication.config;
import com.example.myapplication.data.model.LoggedInUser;
import com.example.myapplication.utils.ApiService;
import com.example.myapplication.utils.UserApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            Map<String, String> map = new HashMap<>();
            map.put("account", username);
            map.put("password", password);
            Call<MyR<LoggedInUser>> call = ApiService.getClient().create(UserApi.class).login(map);
            MyR<LoggedInUser> received= call.execute().body();
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