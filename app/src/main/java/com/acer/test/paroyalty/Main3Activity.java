package com.acer.test.paroyalty;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main3Activity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED| WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);



        showDialog();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();

    }

    private void showDialog() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        final int index = pref.getInt("project_index", 0);
//        Intent intent=new Intent(getBaseContext(),Main3Activity.class);
////
////        startActivity(intent);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = LayoutInflater.from(this).inflate(R.layout.layout, null);
        VideoView view = (VideoView)customLayout.findViewById(R.id.video_view);
        TextView mTextView = (TextView) customLayout.findViewById(R.id.content);
        String content = "";
        view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        Log.v("test", "path:" + Environment.getExternalStorageDirectory());
        ComponentName componentName = new ComponentName("","");
        //String path = "android.resource://" + getPackageName() + "/" + StartServiceActivity.VideoIdList[index];
        String configString = readFromFile("config.json");
        try {
            JSONObject extendObject = new JSONObject(configString);
            componentName = new ComponentName(extendObject.getString("packageName"), extendObject.getString("className"));
            content = extendObject.getString("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTextView.setText(content);
        Log.v("test", configString);
        String path = Environment.getExternalStorageDirectory()+"/searchlight/demo.mp4";

        //Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+"<path to your video>");
        view.setVideoURI(Uri.parse(path));
        //view.setVideoPath();
        view.start();

        builder.setView(customLayout);
        final ComponentName finalComponentName = componentName;
        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PaRoyaltyService.mLastShowDialogTimeStamp = System.currentTimeMillis();
                        Intent intent=new Intent();
                        //intent.setComponent(new ComponentName("grasea.familife", "tw.com.family.www.familymark.WelcomeActivity"));
                        //intent.setComponent(new ComponentName("com.carrefour.carrefourapp", "com.cloud.interactive.carrefour.carrefourmain.CarrefourMainActivity"));
                        intent.setComponent(finalComponentName);
                        startActivity(intent);
                        PaRoyaltyService.showDialog = false;
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {
                        PaRoyaltyService.mLastShowDialogTimeStamp = System.currentTimeMillis();
                        PaRoyaltyService.showDialog = false;
                        finish();

                    }
                });
        AlertDialog dialog = builder.create();



        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY-1);
        //dialog.getWindow().setType(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON );
        dialog.show();
    }

    public static String pathRoot = Environment.getExternalStorageDirectory() + "/searchlight/";
    public static String readFromFile(String nameFile) {
        String aBuffer = "";
        try {
            File myFile = new File(pathRoot + nameFile);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }
    private String getConfigFileContent() {
        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard,"searchlight/config.json");


        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

}
