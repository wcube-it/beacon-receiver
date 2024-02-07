package com.acer.test.paroyalty;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.acer.paroyalty.AdData;
import com.acer.paroyalty.AdDataCallBack;
import com.acer.paroyalty.LicenseVerifyCallBack;
import com.acer.paroyalty.ParoyaltyManager;
import com.acer.paroyalty.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class PaRoyaltyService extends Service implements AdDataCallBack {
    BroadcastReceiver screenReceiver;
    ParoyaltyManager manager;
    public static long mLastShowDialogTimeStamp=0;
    public static boolean showDialog = false;
    private boolean mAdSourceLocalVideo = false;
    private int count = 100001;
    Logger mLogger = LoggerFactory.getLogger("PaRoyaltyService");

    public PaRoyaltyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mLogger.debug("onTaskRemoved");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mLogger.debug("onCreate");
        registerBroadcastReceivers();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        int index = pref.getInt("project_index", 0);
        mAdSourceLocalVideo = pref.getInt("source_index", R.id.adSourceLocal) == R.id.adSourceLocal;
        manager = new ParoyaltyManager(this);
        //manager.setGetAdDataPemanagerriod(60);
        manager.initSDK(1, StartServiceActivity.ProjectAppid[index], StartServiceActivity.ProjectAppKey[index], new LicenseVerifyCallBack() {
            @Override
            public void onVerifyResult(Status status) {
                mLogger.debug("onVerifyResult:" + status.toString());
                if(status == Status.SUCCESS) {
                    manager.startReceiver();
                    manager.setGetAdDataPeriod(60);
                    mLogger.debug("startReceiver");
                }
            }
        });
        manager.registerAdDataCallBack(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRunningInForeground();

        return START_STICKY;
    }

    private void startRunningInForeground() {

        if (Build.VERSION.SDK_INT >= 26) {
            if(Build.VERSION.SDK_INT > 26){
                String CHANNEL_ONE_ID = "com.acer.test.paroyalty";
                String CHANNEL_ONE_NAME = "Screen service";
                NotificationChannel notificationChannel = null;
                notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                        CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.createNotificationChannel(notificationChannel);
                }

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setChannelId(CHANNEL_ONE_ID)
                        .setContentTitle("test")
                        .setContentText("test")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setLargeIcon(icon)
                        .build();

                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                startForeground(101, notification);
            }
            //if version 26
            else{
                startForeground(101, updateNotification());

            }
        }
        //if less than version 26
        else{
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Activity logger")
                    .setContentText("data recording on going")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setOngoing(true).build();

            startForeground(101, notification);
        }
    }

    private Notification updateNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StartServiceActivity.class), PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this)
                .setContentTitle("test")
                .setTicker("test")
                .setContentText("test")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
    }

//    private void detectingDeterminateOfServiceCall(Bundle b) {
//        if(b != null){
//            Log.i("screenService", "bundle not null");
//            if(b.getBoolean("phone restarted")){
//                storeInternally("Phone restarted");
//            }
//        }else{
//            Log.i("screenService", " bundle equals null");
//        }
//        documentServiceStart();
//    }


//    private void documentServiceStart() {
//        Log.i("screenService", "started running");
//    }


    private void registerBroadcastReceivers() {
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (Objects.requireNonNull(intent.getAction())){
                    case Intent.ACTION_SCREEN_ON:
                        //or do something else
                        mLogger.debug("Screen on");
                        storeInternally("Screen on");
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        //or do something else
                        mLogger.debug("Screen off");
                        storeInternally("Screen off");
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (manager == null) {
                            break;
                        }
                        switch(state) {
                            case BluetoothAdapter.STATE_OFF:
                                manager.stopReceiver();
                                mLogger.debug("BluetoothAdapter.STATE_OFF");
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                break;
                            case BluetoothAdapter.STATE_ON:
                                manager.startReceiver();
                                mLogger.debug("BluetoothAdapter.STATE_ON");
                                break;
                            case BluetoothAdapter.STATE_TURNING_ON:

                                break;
                        }
                        break;
                }
            }
        };

        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(screenReceiver, screenFilter);
    }

    private void storeInternally(String screen_on) {
        Log.v("test", "screen:" + screen_on);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLogger.debug("onDestroy");
        unregisterReceiver(screenReceiver);
        if (manager != null) {
            manager.stopReceiver();
            manager.release();
        }
    }

    @Override
    public void onReceiveAdData(AdData[] adData) {
        if (adData.length > 1) {
            mLogger.debug("onReceiveAdData:" + adData[0].title);
        }
        if (adData.length > 0 ) {
            if (mAdSourceLocalVideo) {
                if (!showDialog && (System.currentTimeMillis() - mLastShowDialogTimeStamp) > 20000) {
                    showDialogActivity();
                    mLogger.debug("showDialogActivity");
                }
            } else {
                AdData ad = adData[0];
                Log.v("test1", ad.toString());
                if (ad.displayMode == 0) {
                    sendNotification(ad);
                    mLogger.debug("sendNotification");
                } else {
                    mLogger.debug("show popup windows");
                    Intent intent = new Intent(this, PopupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("data", ad);
                    startActivity(intent);
                }
            }
        }

        mLogger.debug("adData:" + adData[0].toString());
        //Toast.makeText(this, adData[0].url, Toast.LENGTH_LONG).show();
    }



    private void sendNotification(AdData ad) {
        mLogger.debug("sendNotification");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("test_test", "test", importance);
            channel.setDescription("test");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Log.v("test", "sendNotification");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("data", ad);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "test_test")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(ad.title)
                .setContentText(ad.content)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(ad.content));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1002, builder.build());
        count++;

    }

    private void showDialogActivity() {
        Intent intent = new Intent(getBaseContext(),Main3Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }




}

