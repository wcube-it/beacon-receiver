package com.acer.test.paroyalty;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.acer.paroyalty.ParoyaltyManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class StartServiceActivity extends AppCompatActivity implements View.OnClickListener {
    ParoyaltyManager manager;
    RadioGroup mRadGropProject, mRadioGroupAdData;
    Button mClickButton;
    //private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    //private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    public static String ProjectName[] = {"Project F", "Project U", "Project C", "Project I", "Project H", "Project demo"};
    public static String ProjectAppid[] = {"NO000001", "NO000002", "NO000003", "NO000004", "NO000005", "NO000006"};
//    public static int VideoIdList[] = {R.raw.video, R.raw.uupon, R.raw.car, R.raw.food, R.raw.h};
//    public static String Content[] = {"Let\'s Cafe 單品 盧安達紅波旁 咖啡會員限時限量買一送一\n\n詳情請看 FamilyMart app.",
//            "優惠多點 生活滿點，悠遊卡全家3倍送\n詳情請見UUPON app",
//            "家樂福集點換購 GoodPlus美感革命 \n 活動日期：即日起 ~ 06/15 \n 活動詳情請見  家樂福 APP",
//            "聖德科斯生機食品: 用心嚴格檢驗好品質，享受有機健康生活\n詳請請見 聖德科斯 APP",
//            "光泉茉莉茶園把蜜茶柚茶變成脆冰棒啦～\n 7/1起➔小萊獨家開賣! 嚐鮮優惠第2件5折!\n 詳請請見: 萊爾富APP"};
    public static String ProjectAppKey[] = {"3tCeCA6qDfdu1pImSiZOSuvnhNihM4yoae", "BnGZFcrxN7d2phGYxvkCCuvUcN9t8Caw", "5EmpAHckapTVxtbv6MgNEMk4gfe625hU", "eBQnrMwtxeK9w3bEen3Fg4RYfwdgwmgU", "q2UzT2tP495che3eZsmZS2WvnYc8QZxp", "8WlA5qxEVP7bv0ifFDaNAUj0rtminque"};
//    public static ComponentName AppComponentName[] = {new ComponentName("grasea.familife", "tw.com.family.www.familymark.WelcomeActivity"),
//            new ComponentName("com.ddpowers.uupon", "com.ddpowers.uupon.activity.SplashActivity"),
//            new ComponentName("com.carrefour.carrefourapp", "com.cloud.interactive.carrefour.carrefourmain.CarrefourMainActivity"),
//            new ComponentName("com.hilife.vipapp", "common.view.StartupActivity"),
//            new ComponentName("com.hilife.vipapp", "com.hilife.vipapp.common.view.StartupActivity")};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startAlarm();
        setContentView(R.layout.activity_start_service);
        //sendNofitication();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //Log.v("ddd", "test:" + this.checkSelfPermission("android.permission.ACCESS_BACKGROUND_LOCATION"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission("android.permission.ACCESS_BACKGROUND_LOCATION")
                        != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale("android.permission.ACCESS_BACKGROUND_LOCATION")) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app needs background location access");
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 在這裡調用你的函數
                                LogView();
                            }
                        });
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{"android.permission.ACCESS_BACKGROUND_LOCATION"},
                                        101);
                            }

                        });
                        builder.show();
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 在這裡調用你的函數
                                LogView();
                            }
                        });
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }

                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    "android.permission.ACCESS_BACKGROUND_LOCATION"},
                            102);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 在這裡調用你的函數
                            LogView();
                        }
                    });
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
        mRadGropProject = (RadioGroup) findViewById(R.id.radioGroup1);
        mRadioGroupAdData = (RadioGroup) findViewById(R.id.adDataRadioGroup);

        mClickButton = (Button) findViewById(R.id.clickButton);
        mClickButton.setOnClickListener(this);
        final RadioButton[] rb = new RadioButton[ProjectName.length];
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        int index = pref.getInt("project_index", 0);
        int sourceIndex = pref.getInt("source_index", R.id.adSourceServer);

        for (int i = 0; i < ProjectName.length; i++) {
            rb[i] = new RadioButton(this);
            rb[i].setText(ProjectName[i]);
            rb[i].setId(i);
            mRadGropProject.addView(rb[i]);
        }
        mRadGropProject.check(index);
        mRadioGroupAdData.check(sourceIndex);
    }
    public void LogView() {
        Log.v("ABCD","TESTOK");
    }
    @Override
    public void onClick(View view) {
        //sendNofitication();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("project_index", mRadGropProject.getCheckedRadioButtonId());
        editor.putInt("source_index", mRadioGroupAdData.getCheckedRadioButtonId());
        editor.commit();
        Intent intent = new Intent();
        intent.setClass(this, PaRoyaltyService.class);
        this.stopService(intent);
        if (Build.VERSION.SDK_INT > 25) {
            getApplication().startForegroundService(intent);
        } else {
            getApplication().startService(intent);
        }

    }

    private void sendNofitication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("test_test", "test", importance);
            channel.setDescription("test");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Log.v("test", "sendNotification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "test_test")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(111, builder.build());
    }


    public void startAlarm() {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int interval = 60 * 1000; // 30 seconds of interval.
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }
}
