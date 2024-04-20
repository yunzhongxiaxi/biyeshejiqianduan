package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;

import com.example.myapplication.utils.MsgAdapter;
import com.example.myapplication.utils.myListener;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myapplication.entity.Msg;
import com.example.myapplication.utils.mySpeaker;
import com.example.myapplication.utils.myTime;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private   myListener listener;
    private mySpeaker speaker;
    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView recyclerView;

    private ImageButton sendButton,audioButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private EditText editText;
    private MsgAdapter adapter;

    OkHttpClient client;
    PlayerView playerView;

    ExoPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            try{
                switch (msg.what) {
                    case 1:
                        //进行UI操作
                        editText.setText((CharSequence) msg.obj);
                        break;
                    case 2:
                        Bundle bundle= (Bundle) msg.obj;
                        if(bundle.getBoolean("success")){
                            addNewMessage(bundle.getString("response"),0);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"请求错误",Toast.LENGTH_LONG).show();
                        }
                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    int [] films=new int[]{R.raw.idle,R.raw.happy,R.raw.neutral,R.raw.serious,R.raw.surprise};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText=findViewById(R.id.input);
        sendButton=findViewById(R.id.send);
        audioButton=findViewById(R.id.audio);
        playerView=findViewById(R.id.player);
        recyclerView =  findViewById(R.id.msg_recycler_view);
        navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // 启动ModifyActivity
                    Intent intent = new Intent(MainActivity.this, ModifyActivity.class);
                    startActivity(intent);
                }

                // 关闭抽屉
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        listener=new myListener(MainActivity.this,handler);
        speaker=new mySpeaker(MainActivity.this);
        client = new OkHttpClient();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        audioButton.setOnClickListener(view -> {
            if (initPermission()){
                listener.listenAudio();
            }
        });
        sendButton.setOnClickListener(view -> {
            requestAnswer(handler);
        });

        recyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(adapter);

        player= new ExoPlayer.Builder(MainActivity.this).build();
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        playerView.setPlayer(player);
        playerView.setUseController(false);
        changeVideo(0);
        addNewMessage(getString(R.string.hello_word),0);
    }
    public void addNewMessage(String msg,int type){
        Msg message = new Msg(msg,type,myTime.getCurrentTime());
        if(type==Msg.TYPE_RECEIVED){
            speaker.startAuto(msg);
        }
        msgList.add(message);
        adapter.notifyItemInserted(msgList.size()-1);
        recyclerView.scrollToPosition(msgList.size()-1);
    }
    public void changeVideo(int videoId){
        String uri = "android.resource://" + "myapplication" + "/" + films[videoId];
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }
    private boolean initPermission() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        if (toApplyList.size()==0){
            return true;
        }
        String[] tmpList = new String[toApplyList.size()];
        ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        return false;
    }
    private void requestAnswer(Handler handler){
        int runningCallsCount = client.dispatcher().runningCallsCount();
        int queuedCallsCount = client.dispatcher().queuedCallsCount();

        boolean isRequesting = runningCallsCount > 0 || queuedCallsCount > 0;
        if(isRequesting){
            Dispatcher dispatcher = client.dispatcher();
            // 取消所有活跃的请求
            for (Call call : dispatcher.runningCalls()) {
                call.cancel();
            }
            // 取消所有等待的请求
            for (Call call : dispatcher.queuedCalls()) {
                call.cancel();
            }
            Toast.makeText(this,"已取消请求",Toast.LENGTH_LONG).show();
            sendButton.setImageResource(R.drawable.round_chat_bubble_outline_24);
            return;
        }
        String toSend=editText.getText().toString();
        if(toSend.length()==0){
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
            editText.startAnimation(shake);
            return;
        }
        sendButton.setImageResource(R.drawable.discard_send);
        editText.setText("");
        addNewMessage(toSend,1);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", toSend);
            jsonObject.put("top_k", "5");
            jsonObject.put("method","DFS_woFilter_w2");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody requestJsonBody = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json")
        );

        Request postRequest = new Request.Builder()
                .url(config.send_url)
                .post(requestJsonBody)
                .build();
        client.newCall(postRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=2;
                Bundle bundle=new Bundle();
                bundle.putBoolean("success",false);
                message.obj=bundle;
                handler.sendMessage(message);
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Message message=Message.obtain();
                message.what=2;
                Bundle bundle=new Bundle();
                bundle.putBoolean("success",true);
                bundle.putString("response",response.body().string());
                message.obj=bundle;
                handler.sendMessage(message);
            }
        });
    }
}