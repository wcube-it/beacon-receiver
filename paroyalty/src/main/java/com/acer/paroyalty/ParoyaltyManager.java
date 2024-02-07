package com.acer.paroyalty;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ParoyaltyManager {
    Context mContext;
    static final String TAG = "ParoyaltyManager";
    Logger mLogger = LoggerFactory.getLogger("ParoyaltyManager");


    boolean mIsDev = false;
    RequestQueue mVolleyQueue;
    String mCurrntUUID;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBleScanner;
    boolean mIsScaning = false;
    long mLastGetAdTime = 0;
    int mLastStoreId = 0;
    boolean isVerifyBlePackage = true;
    String mAppId;
    AdDataCallBack mAdDataCallBack;
    long mTimePerios = 5 * 60 * 1000;
    boolean isPassLicenseVerfiy = false;
    long mLastShowReceiveLog = 0;


    public ParoyaltyManager(Context context) {
        mContext = context;
        mVolleyQueue = Volley.newRequestQueue(mContext);
    }

    public void initSDK(int environment, String appid, String licenseKey, final LicenseVerifyCallBack callBack) {
        if (environment == 0) {
            mIsDev = true;
        }
        mLogger.debug("initSDK mIsDev=" + mIsDev + ",mAppId=" + appid + ",licenseKey=" + licenseKey);
        mAppId = appid;
        JSONObject postparams = new JSONObject();
        try {
            postparams.put("appId", appid);
            postparams.put("appKey", licenseKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = (mIsDev ? Constant.DEV_HOST : Constant.PRODUCTION_HOST) + Constant.PATH_VERIFY_LICENSE;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mLogger.debug("verify response:" + response.toString());
                            int errorCode = response.getInt("errorCode");
                            if (errorCode == 0) {
                                Long time = System.currentTimeMillis();
                                if (time < response.getLong("expirationDate")) {
                                    callBack.onVerifyResult(Status.LICENSE_EXPIRED);
                                    mLogger.debug("Status.LICENSE_EXPIRED");
                                } else {
                                    mCurrntUUID = response.getString("uuid");
                                    setGetAdDataPeriod(response.getInt("resumBroadcast") + response.getInt("stopBroadcast"));
                                    isPassLicenseVerfiy = true;
                                    callBack.onVerifyResult(Status.SUCCESS);
                                    mLogger.debug("verify PASS, mCurrntUUID=" + mCurrntUUID);
                                }
                            } else if (errorCode == -1) {
                                callBack.onVerifyResult(Status.LICENSE_ERROR);
                                Log.v(TAG, "LICENSE_ERROR");
                                mLogger.debug("LICENSE_ERROR");
                            } else {
                                callBack.onVerifyResult(Status.OTHERS_ERROR);
                                Log.v(TAG, "OTHERS_ERROR");
                                mLogger.debug("OTHERS_ERROR");
                            }
                        } catch (JSONException e) {
                            callBack.onVerifyResult(Status.OTHERS_ERROR);
                            Log.v(TAG, "init sdk json exception");
                            mLogger.debug("init sdk json exception");
                            mLogger.debug(e.toString());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onVerifyResult(Status.NETWROK_ERROR);
                        mLogger.debug("Volley error:" + error.toString());

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

    public int startReceiver() {
        mLogger.debug("startReceiver, isPassLicenseVerfiy=" + isPassLicenseVerfiy);
        if (!isPassLicenseVerfiy) {
            return -1;
        }
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        mIsScaning = true;

        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        if(android.os.Build.VERSION.SDK_INT  >= 26) {
            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH);
        }
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBleScanner == null) {
            return -1;
        }
        ScanFilter.Builder builder1 = new ScanFilter.Builder();
        ScanFilter filter = builder1.build();
        //final List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().build());
        List<ScanFilter> filters_v2 = new ArrayList<>();
//
        byte[] uuidByte = hexToByteArray(mCurrntUUID.replace("-", ""));


        byte[] preData = {(byte) 0x02, (byte) 0x15};
        byte[] postData = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04};


        byte[] payload = new byte[22];
        System.arraycopy(preData, 0, payload, 0, preData.length);
        System.arraycopy(uuidByte, 0, payload, preData.length, uuidByte.length);
        System.arraycopy(postData, 0, payload, preData.length + uuidByte.length, postData.length);
        Log.v(TAG, "payload=" + ByteTools.bytesToHex(payload));
        mLogger.debug("payload=" + ByteTools.bytesToHex(payload));
        byte[] payloadMask = {(byte) 0xFF, (byte) 0xFF, // this makes it a iBeacon
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, // uuid
                (byte) 0x00, (byte) 0x00,  // Major
                (byte) 0x00, (byte) 0x00}; // Minor
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setManufacturerData(76, payload, payloadMask)
                .build();
        filters_v2.add(scanFilter);
        Intent intent = new Intent(mContext, BleReceiver.class);
        intent.putExtra("o-scan", true);
        intent.putExtra(Constant.INTENT_EXTRA_KEY_APPID, mAppId);
        intent.putExtra(Constant.INTENT_EXTRA_KEY_ISDEVE, mIsDev);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, pendingIntent);
        if(android.os.Build.VERSION.SDK_INT  >= 26) {
            //mBleScanner.stopScan(pendingIntent);
            mBleScanner.startScan(filters_v2, builder.build(), mScanCallback);
        } else {
            mBleScanner.startScan(filters_v2, builder.build(), mScanCallback);
        }
        return 0;
    }

    public void setGetAdDataPeriod(int sec) {
        mTimePerios = sec * 1000;

    }

    public void stopReceiver() {
        mLogger.debug("stopReceiver");
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBleScanner != null) {
            try {
                mLogger.debug("stopScan");
                Intent intent = new Intent(mContext, BleReceiver.class);
                intent.putExtra("o-scan", true);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if(android.os.Build.VERSION.SDK_INT  >= 26) {
                    mBleScanner.stopScan(pendingIntent);
                } else {
                    mBleScanner.stopScan(mScanCallback);
                }

            } catch (Exception e) {
                mLogger.debug("stopScan exception:" + e.toString());
                e.printStackTrace();
            }
        }
    }

    public void release() {
        mContext = null;
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (System.currentTimeMillis() > mLastShowReceiveLog + 5*1000) {
                mLogger.debug("receive info");
                mLastShowReceiveLog = System.currentTimeMillis();
            }
            parseResult(result);
        }

        public void onBatchScanResults(List<ScanResult> results) {
        }

        public void onScanFailed(int errorCode) {
        }
    };

    private void parseResult(ScanResult result) {
        long currentTime = System.currentTimeMillis();
        if (currentTime < (mLastGetAdTime + mTimePerios)) {
            return;
        }
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

        if (!isVerifyBlePackage || checkBit == xor2bit) {
            mLogger.debug("check pass");
            getAdData(storeid);
        }
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


    private String getSerialId() {
        return Installation.id(mContext);
    }

    private void getAdData(int storeid) {
        if (mAdDataCallBack == null) {
            mLogger.debug("mAdDataCallBack null return");
            return;
        }
        mLastGetAdTime = System.currentTimeMillis();
        String url = (mIsDev ? Constant.DEV_HOST : Constant.PRODUCTION_HOST) + Constant.PATH_GET_ADDATA;
        JSONObject postparams = new JSONObject();

        try {
            postparams.put("appId", mAppId);
            postparams.put("deviceId", getSerialId());
            Log.v(TAG, "getSerialId=" + getSerialId());
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
                                AdData[] adData = new AdData[array.length()];
                                for (int i = 0; i < array.length(); i++) {
                                    adData[i] = AdData.parseFromJson(array.getJSONObject(i));
                                }
                                mAdDataCallBack.onReceiveAdData(adData);

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


    public static byte[] hexToByteArray(String hex) {
        hex = hex.length() % 2 != 0 ? "0" + hex : hex;
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public void registerAdDataCallBack(AdDataCallBack callBack) {
        mAdDataCallBack = callBack;
    }

    public void clickAd(String token, final ClickAdCallBack callBack) {
        mLogger.debug("clickAd");
        if (!isPassLicenseVerfiy) {
            callBack.onClickAdResult(-1, "Verify license fail");
            mLogger.debug("Verify license fail");
            return;
        }

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("trackToken", token);
            postparams.put("timestamp", System.currentTimeMillis() / 1000);
        } catch (JSONException e) {
            mLogger.debug("JSONException, e=" + e.toString());
            e.printStackTrace();
        }

        String url = (mIsDev ? Constant.DEV_HOST : Constant.PRODUCTION_HOST) + Constant.PATH_TRACK_LOG;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mLogger.debug("click ad, response:" + response);
                        try {
                            int errorCode = response.getInt("errorCode");
                            if (errorCode == 0) {
                                callBack.onClickAdResult(0, "");
                            } else {
                                callBack.onClickAdResult(-1, "server error");
                            }
                        } catch (JSONException e) {
                            mLogger.debug("JSONException, e=" + e.toString());
                            callBack.onClickAdResult(-1, "Jsonobject error");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLogger.debug("VolleyError, error:" + error.toString());
                        callBack.onClickAdResult(-1, "VolleyError:" + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return createBasiAuthUser(Constant.SERVER_USERNAME, Constant.SERVER_PASSWORD);
            }
        };
        mVolleyQueue.add(jsonObjReq);
    }
}
