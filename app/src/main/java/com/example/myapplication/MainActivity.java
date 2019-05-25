package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    public final static int REQUEST_CODE = -1010101;
    String file="file",time;
    Intent globalService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalService = new Intent(this,GlobalTouchService.class);
        checkDrawOverlayPermission();
    }

    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT > 23 && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT > 23 && Settings.canDrawOverlays(this)) {
                startService(globalService);
                Toast.makeText(this, "Access granted",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void buttonClicked(View v){

        if(v.getTag() == null){
            TextView textTime = (TextView) findViewById(R.id.tempo);
            time = textTime.getText().toString();
            globalService.putExtra("time", time);
            TextView textFile = (TextView) findViewById(R.id.file);
            file = textFile.getText().toString();
            globalService.putExtra("file", file);
            startService(globalService);
            this.finish();
            v.setTag("on");
            ((Button)v).setText("Stop Service");
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
        }
        else{
            stopService(globalService);
            v.setTag(null);
            ((Button)v).setText("Start Service");
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
        }
    }
}
