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
   private Button cutVideo,addAudio;
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
        loadFFMpegBinary();
mContext=TrimActivity.this;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        cutVideo=findViewById(R.id.cutButton);
        addAudio=findViewById(R.id.addTrack);

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
addAudio.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        startActivity(new Intent(TrimActivity.this,PlayVideo.class));
    }
});
    }
    private void setAddAudio() {
        try {
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), 100);
        } catch (Exception e) {

        }
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

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.d(TAG, "onFailure: fail of loadFFMpegBinary");
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: succes of loadFFMpegBinary");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.d(TAG, "loadFFMpegBinary: exception "+e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "loadFFMpegBinary: exception "+e.getMessage());

        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "onFailure: failure of execFFmpegBinary "+s);
                }

                @Override
                public void onSuccess(String s) {
//                    Intent intent = new Intent(this, PreviewImageActivity.class);
//                    intent.putExtra(FILEPATH, filePath);
//                    startActivity(intent);
//                    if(!SharedPreferenceImpl.getInstance().get("2",TrimActivity.this).equals(filePath)){
//                       if(videoNumber.equals("10")){ SharedPreferenceImpl.getInstance().save(videoNumber,filePath,TrimActivity.this);
                        Log.d("tag", "onSuccess:balleleeeeeeeeeeeeeeeeeeee ");
                       SharedPreferenceImpl.getInstance().save("2",filePath.toString(),TrimActivity.this);
                        Intent intent=new Intent(TrimActivity.this,PlayVideo.class);
                        intent.putExtra("url",filePath);
                        startActivity(intent);
//                    }
//                       else {
//                           startActivity(new Intent(TrimActivity.this,MAudio.class));
//                       }
//                    }
                }


            @Override
            public void onProgress(String s) {
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {

            }
        });
    } catch (    FFmpegCommandAlreadyRunningException e) {

    }
}

    private void executeCutVideoCommand(int startMs, int endMs) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String yourRealPath = getDataColumn(this, selectedVideoUri,null,null);

        String filePrefix = "cut_video";
        String fileExtn = ".mp4";
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);
        filePath = dest.getAbsolutePath();
        Log.d(TAG, "executeCutVideoCommand: filepath is "+filePath.toString());
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};

        execFFmpegBinary(complexCommand);

    }
    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Log.d(TAG, "getPath: tyoe"+type);
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
