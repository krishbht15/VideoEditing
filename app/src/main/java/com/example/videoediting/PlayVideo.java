package com.example.videoediting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;


import java.io.File;
import java.util.ArrayList;

public class PlayVideo extends AppCompatActivity {
    private VideoView mVideo;
    private Uri filePathAudio;
    private boolean audioAdded;
    private Button concatButton, add;
    private ArrayList<String> inputs;
    private static final String TAG = "PlayVideo";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        mVideo = findViewById(R.id.videoVidew);
        audioAdded = false;
        mVideo.setVideoURI(Uri.fromFile(new File(SharedPreferenceImpl.getInstance().get("merge", PlayVideo.this))));
        mVideo.start();
        add = findViewById(R.id.addaaa);
        inputs = new ArrayList<>();
        concatButton = findViewById(R.id.concat);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAudioFromFile();
            }
        });
    }

    private void addAudioFromFile() {
        try {
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), 100);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                filePathAudio = data.getData();
                audioAdded = true;
                FfmpegSingleton.getInstance(PlayVideo.this).addAudio(Uri.parse(SharedPreferenceImpl.getInstance().get("merge", PlayVideo.this)), filePathAudio, PlayVideo.this);
            }
        }
    }


}
