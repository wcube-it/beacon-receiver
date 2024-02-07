package com.acer.test.paroyalty;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.acer.paroyalty.AdData;
import com.acer.paroyalty.AdDataCallBack;
import com.acer.paroyalty.ClickAdCallBack;
import com.acer.paroyalty.LicenseVerifyCallBack;
import com.acer.paroyalty.ParoyaltyManager;
import com.acer.paroyalty.Status;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ParoyaltyManager manager;
    WebView myWebView;
    AdData mAdData;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mAdData = this.getIntent().getParcelableExtra("data");
        if (mAdData.action == 1) {
            Intent intent=new Intent();
            try {
                JSONObject extendObject = new JSONObject(mAdData.actionData);
                intent.setComponent(new ComponentName(extendObject.getString("packageName"), extendObject.getString("className")));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        } else {
            String url = mAdData.actionData;

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
            finish();
        }
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);

        mButton = (Button) findViewById(R.id.clickButton);
        mButton.setOnClickListener(this);

//        mAdData = savedInstanceState.getParcelable("data");
        if(mAdData != null) {
            Log.v("test", "ad data is null");
            myWebView.loadUrl(mAdData.url);
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v("test", "onNewIntent");
        mAdData = intent.getParcelableExtra("data");
        Log.v("test", "url:" + mAdData.url);
        myWebView.loadUrl(mAdData.url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.clickButton) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
