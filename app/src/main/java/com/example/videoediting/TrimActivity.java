package com.example.videoediting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.net.URI;

public class TrimActivity extends AppCompatActivity {
    private static final String TAG = "TrimActivity";
   private FFmpeg ffmpeg;
   private Uri selectedVideoUri;
   private String filePath;
   private boolean isAudioAdded;
   private Uri audioUri;
   private EditText cropDuration;
   private Button cutVideo;
    private Runnable r;

    private TextView tvLeft,tvRight;
   private VideoView videoView;
    private RangeSeekBar rangeSeekBar;
    String videoNumber,videouri,key;
    private Context mContext;
    private static final String FILEPATH = "filepath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     videoNumber =String.valueOf(getIntent().getIntExtra("n",0));
     videouri=getIntent().getStringExtra("uri");
     key=getIntent().getStringExtra("key");
        setContentView(R.layout.activity_trim);
        isAudioAdded=false;
        audioUri=null;
        selectedVideoUri= Uri.parse(videouri);
mContext=TrimActivity.this;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        cutVideo=findViewById(R.id.cutButton);

        rangeSeekBar=findViewById(R.id.rangeBar);
        tvLeft=findViewById(R.id.tvLeft);
        tvRight=findViewById(R.id.tvRight);
        videoView=findViewById(R.id.croppingVideo);
        videoView.setVideoURI(selectedVideoUri);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
             int   duration = mp.getDuration() / 1000;
                tvLeft.setText("00:00:00");

                tvRight.setText(getTime(mp.getDuration() / 1000));
                rangeSeekBar.setRangeValues(0, duration);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setSelectedMaxValue(duration);
                rangeSeekBar.setEnabled(true);

                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int) minValue * 1000);

                        tvLeft.setText(getTime((int) bar.getSelectedMinValue()));

                        tvRight.setText(getTime((int) bar.getSelectedMaxValue()));

                    }
                });

                final Handler handler = new Handler();
                handler.postDelayed(r = new Runnable() {
                    @Override
                    public void run() {

                        if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                        handler.postDelayed(r, 1000);
                    }
                }, 1000);
            }
        });
        cutVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);
            FfmpegSingleton.getInstance(mContext).executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000,rangeSeekBar.getSelectedMaxValue().intValue() * 1000,selectedVideoUri,Integer.parseInt(videoNumber)*10,key,mContext);
            }
        });

    }

    private String getTime(int totalseconds) {
        int hours = totalseconds / 3600;
        int reamainigTime = totalseconds % 3600;
        int minutes = reamainigTime / 60;
        int sec = reamainigTime % 60;
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", sec);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                isAudioAdded=true;
                audioUri=data.getData();


            }
            }

    }





   }
