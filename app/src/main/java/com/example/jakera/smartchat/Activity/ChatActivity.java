package com.example.jakera.smartchat.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jakera.smartchat.Adapter.ChatRecyclerViewAdapter;
import com.example.jakera.smartchat.Entry.BaseMessageEntry;
import com.example.jakera.smartchat.Entry.TextMessageEntry;
import com.example.jakera.smartchat.Entry.VoiceMessageEntry;
import com.example.jakera.smartchat.Interface.ItemClickListener;
import com.example.jakera.smartchat.R;
import com.example.jakera.smartchat.Utils.MediaManager;
import com.example.jakera.smartchat.Utils.OkhttpHelper;
import com.example.jakera.smartchat.Views.AudioRecorderButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by jakera on 18-2-1.
 */

public class ChatActivity extends AppCompatActivity implements Callback,ItemClickListener{

    private String TAG="ChatActivity";

    private RecyclerView recyclerView;
    private List<BaseMessageEntry> datas;
    private ChatRecyclerViewAdapter adapter;

    private TextView tv_send;
    private EditText et_input_text;

    private OkhttpHelper okhttpHelper;

    private ImageButton btn_voice_chat;

    private AudioRecorderButton mAudioRecorderButton;

    private boolean isVoiceMode=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAudioRecorderButton=(AudioRecorderButton)findViewById(R.id.id_recorder_button);
        mAudioRecorderButton.setAudioFinishRecorderListener(new AudioRecorderButton.AudioFinishRecorderListener() {
            @Override
            public void onFinish(float seconds, String filePath) {
                VoiceMessageEntry voiceMessageEntry=new VoiceMessageEntry(seconds,filePath);
                Log.i(TAG,filePath);
                voiceMessageEntry.setPortrait(BitmapFactory.decodeResource(getResources(),R.mipmap.icon));
                voiceMessageEntry.setViewType(BaseMessageEntry.SENDMESSAGE);
                datas.add(voiceMessageEntry);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(datas.size()-1);
            }
        });
        recyclerView=(RecyclerView)findViewById(R.id.recyclerview_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ChatRecyclerViewAdapter();
        adapter.setOnItemClickListener(this);

        datas=new ArrayList<>();

        okhttpHelper=new OkhttpHelper();
        okhttpHelper.setCallback(this);

        TextMessageEntry messageEntry0=new TextMessageEntry();
        messageEntry0.setPortrait(BitmapFactory.decodeResource(getResources(),R.drawable.robot_portrait));
        messageEntry0.setContent("嗨，我是小智，来和我聊天吧！！！");
        messageEntry0.setViewType(TextMessageEntry.RECEIVEMESSAGE);

        et_input_text=(EditText)findViewById(R.id.et_input_text);
        tv_send=(TextView)findViewById(R.id.tv_send);
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextMessageEntry messageEntry=new TextMessageEntry();
                messageEntry.setPortrait(BitmapFactory.decodeResource(getResources(),R.mipmap.icon));
                messageEntry.setContent(et_input_text.getText().toString());
                okhttpHelper.postToTuLingRobot(et_input_text.getText().toString(),"123456");
                messageEntry.setViewType(TextMessageEntry.SENDMESSAGE);
                et_input_text.setText("");
                datas.add(messageEntry);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(datas.size()-1);
            }
        });


        btn_voice_chat=(ImageButton)findViewById(R.id.btn_voice_chat);
        btn_voice_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVoiceMode){
                    et_input_text.setVisibility(View.VISIBLE);
                    mAudioRecorderButton.setVisibility(View.GONE);
                    isVoiceMode=!isVoiceMode;
                }else {
                    et_input_text.setVisibility(View.GONE);
                    mAudioRecorderButton.setVisibility(View.VISIBLE);
                    isVoiceMode=!isVoiceMode;
                }

            }
        });

        datas.add(messageEntry0);

        adapter.setDatas(datas);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String answer=response.body().string();
        TextMessageEntry messageEntry=new TextMessageEntry();
        messageEntry.setPortrait(BitmapFactory.decodeResource(getResources(),R.drawable.robot_portrait));
        messageEntry.setContent(okhttpHelper.parseTuLingResult(answer));
        messageEntry.setViewType(TextMessageEntry.RECEIVEMESSAGE);
        datas.add(messageEntry);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(datas.size()-1);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.release();
    }

    @Override
    public void OnItemClick(View v, int position) {
        if(datas.get(position) instanceof VoiceMessageEntry){
            MediaManager.playSound(((VoiceMessageEntry) datas.get(position)).getFilePath(), new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                }
            });
        }
    }
}