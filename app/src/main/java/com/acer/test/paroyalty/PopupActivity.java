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
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.acer.paroyalty.AdData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class PopupActivity extends AppCompatActivity {

    AdData mAdData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED| WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mAdData = this.getIntent().getParcelableExtra("data");
        showDialog();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mAdData = intent.getParcelableExtra("data");

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = LayoutInflater.from(this).inflate(R.layout.popup, null);
        builder.setView(customLayout);
        WebView view = (WebView)customLayout.findViewById(R.id.web_view);
        view.getSettings().setMediaPlaybackRequiresUserGesture(false);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setLoadWithOverviewMode(true);
        view.getSettings().setUseWideViewPort(true);
        view.getSettings().setBuiltInZoomControls(true);
        view.getSettings().setDisplayZoomControls(false);
        TextView mTextView = (TextView) customLayout.findViewById(R.id.content);
        mTextView.setText(mAdData.content);
        view.loadUrl(mAdData.url);

        builder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PaRoyaltyService.mLastShowDialogTimeStamp = System.currentTimeMillis();


                        if (mAdData.action == 1) {
                            Intent intent = new Intent();
                            //intent.setComponent(new ComponentName("grasea.familife", "tw.com.family.www.familymark.WelcomeActivity"));
                            //intent.setComponent(new ComponentName("com.carrefour.carrefourapp", "com.cloud.interactive.carrefour.carrefourmain.CarrefourMainActivity"));
                            try {
                                JSONObject extendObject = new JSONObject(mAdData.actionData);
                                intent.setComponent(new ComponentName(extendObject.getString("packageName"), extendObject.getString("className")));
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            String url = mAdData.actionData;

                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(browserIntent);
                        }


                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {

                        finish();

                    }
                });
        AlertDialog dialog = builder.create();



        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY-1);
        //dialog.getWindow().setType(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON );
        dialog.show();
    }


}
