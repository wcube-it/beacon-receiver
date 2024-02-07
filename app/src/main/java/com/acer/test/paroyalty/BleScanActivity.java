package com.acer.test.paroyalty;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.acer.paroyalty.Constant;
import com.acer.paroyalty.LicenseVerifyCallBack;
import com.acer.paroyalty.ParoyaltyManager;
import com.acer.paroyalty.Status;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BleScanActivity extends AppCompatActivity implements View.OnClickListener {

    ParoyaltyManager manager;
    Logger mLogger = LoggerFactory.getLogger("BleScanActivity");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Intent intent = new Intent(Constant.INTENT_ACTION_NAME);
        //intent.putExtra("key", "Your data")
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Button start = (Button) findViewById(R.id.button);
        Button stop = (Button) findViewById(R.id.button2);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        manager = new ParoyaltyManager(this);
        //manager.setGetAdDataPemanagerriod(60);
        manager.initSDK(0, StartServiceActivity.ProjectAppid[0], StartServiceActivity.ProjectAppKey[0], new LicenseVerifyCallBack() {
            @Override
            public void onVerifyResult(Status status) {
                mLogger.debug("onVerifyResult:" + status.toString());

            }
        });

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button) {
                manager.startReceiver();
            Toast.makeText(this, "start scan", Toast.LENGTH_LONG).show();
            endWork(this);
            //StartWork(this, 20);
        } else if (view.getId() == R.id.button2) {
            manager.stopReceiver();
            Toast.makeText(this, "stop scan", Toast.LENGTH_LONG).show();
        }
    }


//    public static void StartWork(Context context, int workTime)
//    {
//        PeriodicWorkRequest myWorker = new PeriodicWorkRequest.Builder(MyWork.class, workTime, TimeUnit.MINUTES).build();
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork("testWork", ExistingPeriodicWorkPolicy.KEEP,myWorker);
//    }

    public static void endWork(Context context)
    {
        WorkManager.getInstance(context).cancelAllWorkByTag("testWork");
    }
}
