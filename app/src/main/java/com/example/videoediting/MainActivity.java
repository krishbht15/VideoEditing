package com.example.videoediting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static int VIDEO_REQUEST=101;
    private static final String TAG = "MainActivity";
    private Context mContext= MainActivity.this;
    private VideoView recordingLeft,recordingRight;
    private Uri videoLUri=null,videoRUri=null;
    private boolean first=false;
    private boolean second=false;
    private boolean fVisited=false;
    private boolean sVisited=false;

    @Override
    protected void onResume() {
        super.onResume();
        if(fVisited){
            recordingLeft.setVideoURI(Uri.fromFile(new File(SharedPreferenceImpl.getInstance().get("first",mContext))));
    recordingLeft.start();
//    recordingLeft.se
        }
        if (sVisited){
            recordingRight.setVideoURI(Uri.fromFile(new File(SharedPreferenceImpl.getInstance().get("second",mContext))));
            recordingRight.start();

        }


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if(SharedPreferenceImpl.getInstance().contains("2",MainActivity.this)){
//            Intent intent=new Intent(MainActivity.this,PlayVideo.class);
//            intent.putExtra("url",SharedPreferenceImpl.getInstance().get("2",MainActivity.this));
//            startActivity(intent);
//        }
        recordingLeft=findViewById(R.id.firstVideo);
        recordingRight=findViewById(R.id.secondVideo);

        findViewById(R.id.capture2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                second=true;
                captureVideo(view);

            }
        });
                   findViewById(R.id.playvideo).setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           ArrayList<String > input=new ArrayList<>();
                           input.add(SharedPreferenceImpl.getInstance().get("second",MainActivity.this));
                           input.add(SharedPreferenceImpl.getInstance().get("first",MainActivity.this));
                           FfmpegSingleton.getInstance(MainActivity.this).concat(input);
                       }
                   });
        findViewById(R.id.capture1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                first=true;

                captureVideo(view);
            }
        });


    }
    public void captureVideo(View view){
        Intent video=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (video.resolveActivity(getPackageManager())!=null){
            startActivityForResult(video,VIDEO_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: nodhahad");

        if(requestCode==VIDEO_REQUEST && resultCode==RESULT_OK){
            Log.d(TAG, "onActivityResult: nodhahad");

            if(first){
                Log.d(TAG, "onActivityResult: first");first=false;
            fVisited=true;
              Log.d(TAG, "onActivityResult: nodhahad");
                videoLUri=data.getData();

                Intent intent=new Intent(MainActivity.this,TrimActivity.class);
                intent.putExtra("n",1);
                intent.putExtra("key","first");
                intent.putExtra("uri",videoLUri.toString());
                startActivity(intent);
            }
            else if(second){
                Log.d(TAG, "onActivityResult: second");
                second=false;
                sVisited=true;

                videoRUri=data.getData();
                Intent intent=new Intent(MainActivity.this,TrimActivity.class);
                intent.putExtra("n",2);
                intent.putExtra("key","second");

                intent.putExtra("uri",videoRUri.toString());
                startActivity(intent);
            }
        }
    }
}
