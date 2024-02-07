package com.acer.paroyalty;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class BleReceiver extends BroadcastReceiver {
    Logger mLogger = LoggerFactory.getLogger("BleReceiver");
    RequestQueue mVolleyQueue;
    boolean mIsDev = false;
    String mAppId;
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mLogger.debug("BleReceiver", "receive pending intentAAAAA");
        mVolleyQueue = Volley.newRequestQueue(context);
        mContext = context;
        getExtraData(intent);
        Log.d("test", "receive pending intentAAAAA");
        int bleCallbackType = intent.getIntExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE, -1);
        ArrayList<ScanResult> scanResults = intent.getParcelableArrayListExtra(
                BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);
        Log.d("test", "bleCallbackType: "+bleCallbackType);

        if (bleCallbackType != -1 && scanResults != null && scanResults.size() > 0) {
            Log.d("test", "Passive background scan callback type: "+bleCallbackType);

            ScanResult result = scanResults.get(0);
            parseResult(result);


            String channelId = "default_channel_id";
            String channelDescription = "Default Channel";
//Check if notification channel exists and if not create one
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
                if (notificationChannel == null) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    notificationChannel = new NotificationChannel(channelId, channelDescription, importance);
                    notificationChannel.setLightColor(Color.GREEN);
                    notificationChannel.enableVibration(false);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
            String currentTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("即日起全家咖啡買一送一")
                    .setContentText("time:" + currentTime)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            // notificationId is a unique int for each notification that you must define
            //notificationManager.notify(111, builder.build());
            // Do something with your ScanResult list here.
            // These contain the data of your matching BLE advertising packets
        }

    }

    private void getExtraData(Intent intent) {
        mAppId = intent.getStringExtra(Constant.INTENT_EXTRA_KEY_APPID);
        mIsDev = intent.getBooleanExtra(Constant.INTENT_EXTRA_KEY_ISDEVE, false);
    }


    public int indexOf(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }


    private void parseResult(ScanResult result) {
        long currentTime = System.currentTimeMillis();
//        if (currentTime < (mLastGetAdTime + mTimePerios)) {
//            return;
//        }
        mLogger.debug("parseResult");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Date currentLocalTime = cal.getTime();

        DateFormat date = new SimpleDateFormat("MM-dd-HH");
        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        String localTime = date.format(currentLocalTime);
        int day = Integer.parseInt(localTime.split("-")[1]);
        int month = Integer.parseInt(localTime.split("-")[0]);
        int hours = Integer.parseInt(localTime.split("-")[2]);


        int orignBitData = (hours << 2) + (day << 7) + (month << 12);
        mLogger.debug("orignBitData=" + orignBitData);

        int bit8_p1 = orignBitData >> 8;
        int bit8_p2 = orignBitData & 0x000000FF;

        int xor8bit = bit8_p1 ^ bit8_p2;

        int bit4_p1 = xor8bit >> 4;
        int bit4_p2 = xor8bit & 0x0000000F;
        int xor4bit = bit4_p1 ^ bit4_p2;

        int bit2_p1 = xor4bit >> 2;
        int bit2_p2 = xor4bit & 0x00000003;
        int xor2bit = bit2_p1 ^ bit2_p2;
        mLogger.debug("xor2bit=" + xor2bit);

        mLogger.debug("day:" + day + ", month:" + month + ", hours:" + hours);
        byte[] beaconStartArray = {(byte)0x1a, (byte)0xff, (byte)0x4c };
        int ibeaconStartIndex =  indexOf(result.getScanRecord().getBytes(), beaconStartArray) ;
        mLogger.debug("ibeaconStart=" + ibeaconStartIndex);

        mLogger.debug("raw=" + ByteTools.bytesToHexWithSpaces(result.getScanRecord().getBytes()));

        int major = 0x00 << 24 | 0x00 << 16 | (result.getScanRecord().getBytes()[22+ibeaconStartIndex] & 0xff) << 8 | (result.getScanRecord().getBytes()[23+ibeaconStartIndex] & 0xff);
        mLogger.debug("major=" + major);
        int storeid = 0x00 << 24 | 0x00 << 16 | (result.getScanRecord().getBytes()[24+ibeaconStartIndex] & 0xff) << 8 | (result.getScanRecord().getBytes()[25+ibeaconStartIndex] & 0xff);
        mLogger.debug("storeid=" + storeid);
        int checkBit = major & 0x00000003;
        mLogger.debug("checkBit=" + checkBit);

        if (checkBit == xor2bit) {
            mLogger.debug("check pass");
            getAdData(storeid);
        }
    }


    private void getAdData(int storeid) {

        //mLastGetAdTime = System.currentTimeMillis();
        String url = (mIsDev ? Constant.DEV_HOST : Constant.PRODUCTION_HOST) + Constant.PATH_GET_ADDATA;
        JSONObject postparams = new JSONObject();

        try {
            postparams.put("appId", mAppId);
            postparams.put("deviceId", Installation.id(mContext));
            String currentHours = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
            postparams.put("hours", Integer.parseInt(currentHours));
            postparams.put("storeId", storeid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            mLogger.debug("get ad data, response:" + response.toString());
                            int errorCode = response.getInt("errorCode");
                            if (errorCode == 0 && response.getJSONArray("adData").length() > 0) {
                                JSONArray array = response.getJSONArray("adData");
                                //AdData[] adData = new AdData[array.length()];
                                ArrayList<AdData> adData = new ArrayList<AdData>();
                                for (int i = 0; i < array.length(); i++) {
                                    adData.add(AdData.parseFromJson(array.getJSONObject(i)));
                                }
                                mLogger.debug("ad title=" + adData.get(0).getContent());
                                Intent intent = new Intent(Constant.INTENT_ACTION_NAME);
                                intent.setPackage(mContext.getPackageName());
                                //intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                                //intent.setPackage(mContext.getPackageName());
                                intent.putParcelableArrayListExtra(Constant.INTENT_EXTRA_KEY_AD_DATA, adData);
                                //mContext.sendBroadcast(intent);
                                mContext.sendBroadcast(intent);
                                mLogger.debug("send broadcast");
                            }
                        } catch (JSONException e) {
                            mLogger.debug("get ad data, JSONException:" + e.toString());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLogger.debug("get ad data , volley error:" + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return createBasiAuthUser(Constant.SERVER_USERNAME, Constant.SERVER_PASSWORD);
            }
        };
        mVolleyQueue.add(jsonObjReq);

    }

    private Map<String, String> createBasiAuthUser(String user, String pass) {
        Map<String, String> headerMap = new HashMap<String, String>();
        String creadentials = user + ":" + pass;
        String base64Credentials = Base64.encodeToString(creadentials.getBytes(), Base64.NO_WRAP);
        headerMap.put("Authorization", "Basic " + base64Credentials);
        return headerMap;
    }
}