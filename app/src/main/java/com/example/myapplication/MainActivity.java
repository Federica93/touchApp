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
    String file="file",time,mode,phone,walker,sex;
    Intent globalService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // istanzia intent
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
// una volta riempito il form, i dati verranno inviati come parametri al servizi attivato dall'intent
        if(v.getTag() == null){
            TextView textTime = (TextView) findViewById(R.id.time);
            time = textTime.getText().toString();
            globalService.putExtra("time", time);
            TextView textFile = (TextView) findViewById(R.id.file);
            file = textFile.getText().toString();
            globalService.putExtra("file", file);
            TextView textMode = (TextView) findViewById(R.id.mode);
            mode = textMode.getText().toString();
            globalService.putExtra("mode", mode);
            TextView textPhone = (TextView) findViewById(R.id.phone);
            phone = textPhone.getText().toString();
            globalService.putExtra("phone", phone);
            TextView textWalker = (TextView) findViewById(R.id.walker);
            walker = textWalker.getText().toString();
            globalService.putExtra("walker", walker);
            TextView textSex = (TextView) findViewById(R.id.sex);
            sex = textSex.getText().toString();
            globalService.putExtra("sex", sex);
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
