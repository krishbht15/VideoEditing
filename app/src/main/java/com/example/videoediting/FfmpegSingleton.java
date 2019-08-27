package com.example.videoediting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FfmpegSingleton {
    private static FFmpeg ffmpeg;
    private static Context mContext;
    private static final String TAG = "FfmpegSingleton";
    private static FfmpegSingleton ffmpegSingleton=new FfmpegSingleton();
    public static FfmpegSingleton getInstance(Context context){
        mContext=context;
        ffmpeg = FFmpeg.getInstance(context);

        return ffmpegSingleton;
    }

    private void outputFfmpeg(final String[] command, final int choice, final String key ) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "onFailure: failure of outputFfmpeg "+s);
                }

                @Override
                public void onSuccess(String s) {
                    if (choice == 1) {
                        Toast.makeText(mContext, key + " Audio added ", Toast.LENGTH_SHORT).show();
                        SharedPreferenceImpl.getInstance().save(key, command[command.length - 1], mContext);

                    }else if(choice==33){
                        Toast.makeText(mContext,  " Video Joined ", Toast.LENGTH_SHORT).show();
                        SharedPreferenceImpl.getInstance().save(key, command[command.length - 1], mContext);
                        mContext.startActivity(new Intent(mContext,PlayVideo.class));

                    }
                    else {
                        Toast.makeText(mContext, key + " Video Cropped ", Toast.LENGTH_SHORT).show();
                        SharedPreferenceImpl.getInstance().save(key, command[command.length - 1], mContext);
                        ((Activity) mContext).finish();
                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "onProgress: "+key);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "onStart: "+key);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "onFinish: "+key);
                }
            });
        } catch (    FFmpegCommandAlreadyRunningException e) {

        }
    }
    public void concat(ArrayList<String > files){

        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );


        String filePrefix = "reverse_video";
        String fileExtn = ".mp4";
        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
        String x="file "+"'"+files.get(0)+"'"+"\n"+"file "+"'"+files.get(1)+"'";
        String out=createTxt(x);
        Log.d(TAG, "concat: outtx t "+out);
        String[] destinationCommand={"-safe","0","-f","concat","-i",out,"-c","copy",dest.getAbsolutePath()};
        outputFfmpeg(destinationCommand,33,"merge");
    }


    public String createTxt( String sBody){
        try {
        File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            String filePrefix = "merge";
            String fileExtn = ".txt";
            int fileNo = 0;
            File dest=new File(root,filePrefix+fileExtn);
            while (dest.exists()) {
                fileNo++;
                dest = new File(root, filePrefix + fileNo + fileExtn);
            }
            Log.d(TAG, "createTxt: destt"+dest+"sbode "+sBody);
        FileWriter writer = new FileWriter(dest);
        writer.append(sBody);
        writer.flush();
        writer.close();
        return dest.getAbsolutePath();
    } catch (IOException e) {
        e.printStackTrace();
    }
        return "";
    }
    public void addAudio(Uri videoUri,Uri filePathAudio,Context context){
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC
        );
        String yourRealPath = getDataColumn( filePathAudio,null,null,context);
        String filePrefix = "add_video";
        String fileExtn = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());

        String [] command={"-i",videoUri.toString(),"-i",yourRealPath,"-c","copy","-map","0:0","-map","1:0","-shortest",dest.getAbsolutePath()};
        outputFfmpeg(command,1,"audio");
    }

    public void executeCutVideoCommand(int startMs, int endMs,Uri selectedVideoUri,int choice,String key,Context context) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );
        String yourRealPath = getDataColumn( selectedVideoUri,null,null,context);

        String filePrefix = key+"_video";
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
        Log.d(TAG, "executeCutVideoCommand: filepath is "+dest.getAbsolutePath());

        String[] complexCommand = { "-ss", "" + startMs / 1000, "-i", yourRealPath,  "-to", "" + endMs  / 1000, "-c","copy", dest.getAbsolutePath()};
        outputFfmpeg(complexCommand,choice,key );

    }
    private String getDataColumn( Uri uri, String selection,
                                 String[] selectionArgs,Context context) {
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

}
