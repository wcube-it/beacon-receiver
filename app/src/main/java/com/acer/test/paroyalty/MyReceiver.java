package com.acer.test.paroyalty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acer.paroyalty.AdData;
import com.acer.paroyalty.Constant;

import java.util.ArrayList;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("test", "MyReceiver ad title=");

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        ArrayList<AdData> adData = intent.getParcelableArrayListExtra(Constant.INTENT_EXTRA_KEY_AD_DATA);
        Log.v("test", "MyReceiver ad title=" + adData.get(0).getTitle());
    }
}
