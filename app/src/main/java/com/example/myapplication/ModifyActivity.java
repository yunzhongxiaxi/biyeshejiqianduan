package com.example.myapplication;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.LoginRepository;
import com.example.myapplication.data.model.LoggedInUser;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ModifyActivity extends AppCompatActivity {
    private TextView textView;
    private Spinner spinner;
    private ImageView imageView;

    private EditText editText;

    private Button button;

    private OkHttpClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        client=new OkHttpClient();
        textView=findViewById(R.id.text_birthday);
        spinner=findViewById(R.id.spinner_gender);
        imageView=findViewById(R.id.image_avatar);
        editText=findViewById(R.id.edit_name);
        button=findViewById(R.id.save_modify);
        button.setOnClickListener(this::onSaveClick);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if(LoginRepository.instance==null|| LoginRepository.instance.user==null){
            textView.setText(R.string.default_birthday);
            spinner.setSelection(0);
            editText.setText("姓名");
        }
        else {
            textView.setText(LoginRepository.instance.user.getUserBirthday());
            spinner.setSelection(LoginRepository.instance.user.getUserSex().equals("male")?0:1);
            editText.setText(LoginRepository.instance.user.getUserName());
        }
        if(LoginRepository.instance==null|| LoginRepository.instance.icon==null){
            imageView.setImageResource(R.drawable.icon_default);
        }
        else {
            imageView.setImageBitmap(LoginRepository.instance.icon);
        }

    }
    public void showDatePickerDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        try {
            // 设定日期格式和初始日期
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date initialDate = format.parse(LoginRepository.instance.user.getUserBirthday());
            if(initialDate==null){
                calendar.setTime(new Date());
                throw new Exception("未获取到用户信息");
            }
            calendar.setTime(initialDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        textView.setText(String.format(getResources().getString(R.string.current_time),year,month,dayOfMonth));
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onAvatarClick(View v){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, config.request_photo_code);
        }
        else {
            openGallery();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == config.request_photo_code && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, config.open_picture_gallery);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == config.open_picture_gallery && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                if(selectedImageUri==null){
                    throw new Exception("未选择照片");
                }
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
                // 可以选择在这里上传或者提供一个上传按钮让用户确认上传
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onSaveClick(View v){
        try{
            updateInformation();
            File imageFile=getImageFileFromImageView(imageView);
            uploadImageFile(imageFile);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"程序异常",Toast.LENGTH_LONG).show();
        }
    }

    private void updateInformation(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", LoginRepository.instance.user.getUserId());
            jsonObject.put("userBirthday", textView.getText());
            jsonObject.put("userSex",spinner.getSelectedItem().toString());
            jsonObject.put("userName",editText.getText());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody requestJsonBody = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(config.update_url)
                .post(requestJsonBody)
                .build();
        client.newCall(postRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "网络或服务器状况异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Gson gson = new Gson();
                String jsonData =response.body().string(); // 从响应体获取的 JSON 字符串
                Type responseType = new TypeToken<MyR<Void>>(){}.getType();
                MyR<Void> received = gson.fromJson(jsonData, responseType);
                if(received.getSuccess()){
                    LoginRepository.instance.user.setUserName(String.valueOf(editText.getText()));
                    LoginRepository.instance.user.setUserBirthday((String) textView.getText());
                    LoginRepository.instance.user.setUserSex(spinner.getSelectedItem().toString());
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"保存失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }
    private File getImageFileFromImageView(ImageView imageView) {
        // 获取ImageView中的Drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
                // Convert drawable to bitmap
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    bitmap = bitmapDrawable.getBitmap();
                }
        }
        // 创建一个输出文件
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "upload.jpg");
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            // 压缩bitmap到文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile;
    }
    private void uploadImageFile(File imageFile) {
        try {
            // 创建RequestBody
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);

            // 创建MultipartBody.Part
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("avatar", imageFile.getName(), fileBody);

            // 创建请求体
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(filePart)
                    .build();

            // 创建请求
            Request request = new Request.Builder()
                    .url(config.upload_url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "网络或服务器状况异常", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Gson gson = new Gson();
                    String jsonData =response.body().string(); // 从响应体获取的 JSON 字符串
                    Type responseType = new TypeToken<MyR<Void>>(){}.getType();
                    MyR<Void> received = gson.fromJson(jsonData, responseType);
                    if(received.getSuccess()!=null&& received.getSuccess()){
                        LoginRepository.instance.icon = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    }
                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),received.getSuccess()?"保存成功":"保存失败",Toast.LENGTH_LONG).show();
                            }
                    });
                }
            });

        }catch (Exception e){
          e.printStackTrace();
        }

    }




}