package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.example.myapplication.model.Posizione;
import com.example.myapplication.model.Punto;
import com.example.myapplication.model.Velocita;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GlobalTouchService extends Service implements OnTouchListener {

    private VelocityTracker mVelocityTracker = null;
    private static final String DEBUG_TAG = "Gestures";
    long inizioPressione = 0, finePressione = 0, tempoInizio;
    float mDownX, mDownY, velX, velY;
    int i, time;
    boolean isOnClick;
    String file;
    private SensorManager sensorManager;
    private long lastUpdate;
    SensorEventListener listen;
    Sensor accel,gyro,magnetic;

    ArrayList<Velocita> velocita = new ArrayList<>();
    ArrayList<Posizione> posizionePressione = new ArrayList<>();
    ArrayList<Posizione> posizioneRilascio = new ArrayList<>();
    ArrayList<Long> holdTime = new ArrayList<>();
    ArrayList<Long> KeyDown_keyDown_time = new ArrayList<>();
    ArrayList<Long> KeyUp_keyDown_time = new ArrayList<>();
    ArrayList<Punto> accelerometro = new ArrayList<>();
    ArrayList<Punto> giroscopio = new ArrayList<>();
    ArrayList<Punto> magnetometro = new ArrayList<>();

    private String TAG = this.getClass().getSimpleName();
    // window manager
    private WindowManager mWindowManager;
    // linear layout will use to detect touch event
    private LinearLayout touchLayout;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //setto il sensore
        sensorManager = (SensorManager) getApplicationContext()
                .getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
        listen = new SensorListen();

        //setto il touch
        tempoInizio = System.currentTimeMillis();
        file = intent.getStringExtra("file");
        time = Integer.parseInt(intent.getStringExtra("time"));

        //registro i sensori
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(listen, accel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listen, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listen, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //scommentare
        touchLayout = new LinearLayout(this);
        LayoutParams lp = new LayoutParams(1,1);
        touchLayout.setLayoutParams(lp);
        touchLayout.setOnTouchListener(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams mParams;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            mParams = new WindowManager.LayoutParams(
                    1, // width of layout 30 px
                    WindowManager.LayoutParams.MATCH_PARENT, // height is equal to full screen
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    // this window won't ever get key input focus
                    PixelFormat.TRANSLUCENT);
        } else {
            mParams = new WindowManager.LayoutParams(
                    1, // width of layout 30 px
                    WindowManager.LayoutParams.MATCH_PARENT, // height is equal to full screen
                    WindowManager.LayoutParams.TYPE_PHONE,
                    // Type Ohone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
        }
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowManager.addView(touchLayout, mParams);// fine
    }

    @Override
    public void onDestroy() {
        if (mWindowManager != null) {
            if (touchLayout != null) mWindowManager.removeView(touchLayout);
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.setLayoutParams(new LayoutParams(1,1));
        int index = event.getActionIndex();

        float eventX = event.getX();
        float eventY = event.getY();
        int pointerId = event.getPointerId(index);
        long execTime = System.currentTimeMillis();
        if (execTime - tempoInizio > time * 1000) {
            salvaArray();
            sensorManager.unregisterListener(listen);
            Toast.makeText(this,
                    "servizio terminato",
                    Toast.LENGTH_LONG).show();
            System.out.println("****stop****");
            stopSelf();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                i = 0;
                velX = 0; velY = 0;
                mDownX = event.getX(); mDownY = event.getY();
                isOnClick = true;
                if (mVelocityTracker == null)
                    mVelocityTracker = VelocityTracker.obtain();
                else
                    mVelocityTracker.clear();
                mVelocityTracker.addMovement(event);
                posizionePressione.add(new Posizione(eventX, eventY));
                if (inizioPressione == 0) {
                    inizioPressione = System.currentTimeMillis();
                    KeyUp_keyDown_time.add(Long.parseLong("0"));
                    KeyDown_keyDown_time.add(Long.parseLong("0"));
                } else {
                    long time = System.currentTimeMillis();
                    long diffInSec = (time - inizioPressione);
                    KeyDown_keyDown_time.add(diffInSec);
                    System.out.println("KeyDownKeyDown: " + diffInSec + "ms");
                    inizioPressione = time;
                    diffInSec = (inizioPressione - finePressione);
                    KeyUp_keyDown_time.add(diffInSec);
                    System.out.println("KeyUpKeyDown: " + diffInSec + "ms");
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mVelocityTracker.getXVelocity(pointerId)) != 0)
                    i++;
                velX = velX + Math.abs(mVelocityTracker.getXVelocity(pointerId));
                velY = velY + Math.abs(mVelocityTracker.getYVelocity(pointerId));
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                System.out.println(mVelocityTracker.getXVelocity(pointerId));
                Log.d("", "Y velocity: " + mVelocityTracker.getYVelocity(pointerId));
                break;
            case MotionEvent.ACTION_UP:
                if (i == 0)
                    velocita.add(new Velocita(velX, velY));
                else
                    velocita.add(new Velocita(velX / i, velY / i));
                posizioneRilascio.add(new Posizione(eventX, eventY));
                finePressione = System.currentTimeMillis();
                long diffInSec = (finePressione - inizioPressione);
                System.out.println("HoldTime: " + diffInSec + "ms");
                holdTime.add(diffInSec);
                break;
            case MotionEvent.ACTION_OUTSIDE:
                if (inizioPressione==0)
                    inizioPressione=System.currentTimeMillis();
                else {
                    diffInSec= System.currentTimeMillis()-inizioPressione;
                    KeyDown_keyDown_time.add(diffInSec);
                    holdTime.add(Long.parseLong("0"));
                    KeyUp_keyDown_time.add(Long.parseLong("0"));
                    velocita.add(new Velocita(0,0));
                }
                System.out.println("*****fuori schermo**** x: "+event.getX()+" y:"+event.getY());
                break;
        }//fine
        return false;
    }

    public void salvaArray() {

        File csv = new File(file);
        System.out.println(csv.exists());
        if (csv.exists())
            csv.delete();
        FileOutputStream outputStream;

        try {
            //scrivo i tocchi e la velocità sul file
            outputStream = openFileOutput(file+"_touch.csv", Context.MODE_PRIVATE);
            outputStream.write(("HoldTime;keyDownKeyDownTime;keyUPKeyDownTime;pressioneX;" +
                    "pressioneY;rilascioX;rilascioY;velocitaX;velocitaY\n").getBytes());
            for (int i = 0; i < holdTime.size(); i++)
                outputStream.write((holdTime.get(i) + ";" + KeyDown_keyDown_time.get(i) + ";"
                        + KeyUp_keyDown_time.get(i) + ";" + posizionePressione.get(i).getX() + ";" + posizionePressione.get(i).getY()
                        + ";" + posizioneRilascio.get(i).getX() + ";" + posizioneRilascio.get(i).getY() + ";" + velocita.get(i).getX() + ";" + velocita.get(i).getY()+"\n").getBytes());
            outputStream.flush();
            outputStream.close();

            //scrivo accelerometro sul file
            outputStream = openFileOutput(file+"_accelerometro.csv", Context.MODE_PRIVATE);
            outputStream.write(("X;Y;Z;timestamp\n").getBytes());
            for (int i = 0; i < accelerometro.size(); i++)
                outputStream.write((+ accelerometro.get(i).getX() + ";" + accelerometro.get(i).getY() +
                        ";" + accelerometro.get(i).getZ()+";"+accelerometro.get(i).getTimestamp()+"\n").getBytes());
            outputStream.flush();
            outputStream.close();

            //scrivo magnetometro sul file
            outputStream = openFileOutput(file+"_magnetometro.csv", Context.MODE_PRIVATE);
            outputStream.write(("X;Y;Z;timestamp\n").getBytes());
            for (int i = 0; i < magnetometro.size(); i++)
                outputStream.write((+ magnetometro.get(i).getX() + ";" + magnetometro.get(i).getY() +
                        ";" + magnetometro.get(i).getZ()+";"+magnetometro.get(i).getTimestamp()+"\n").getBytes());
            outputStream.flush();
            outputStream.close();

            //scrivo giroscopio sul file
            outputStream = openFileOutput(file+"_giroscopio.csv", Context.MODE_PRIVATE);
            outputStream.write(("X;Y;Z;timestamp\n").getBytes());
            for (int i = 0; i < giroscopio.size(); i++)
                outputStream.write((+ giroscopio.get(i).getX() + ";" + giroscopio.get(i).getY() +
                        ";" + giroscopio.get(i).getZ()+";"+giroscopio.get(i).getTimestamp()+"\n").getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        long timestamp=event.timestamp;
        accelerometro.add(new Punto(x,y,z,timestamp));
        System.out.println("accelerometro: x=" + x + " y=" + y + " z=" + z+ " timestamp=" + timestamp);
    }

    private void getGyroscope(SensorEvent event) {
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        long timestamp=event.timestamp;
        giroscopio.add(new Punto(x,y,z,timestamp));
        System.out.println("giroscopio: x=" + x + " y=" + y + " z=" + z+ " timestamp=" + timestamp);
    }

    private void getMagneticField(SensorEvent event) {
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        long timestamp=event.timestamp;
        magnetometro.add(new Punto(x,y,z,timestamp));
        System.out.println("magnetometro: x=" + x + " y=" + y + " z=" + z+ " timestamp=" + timestamp);
    }

    public class SensorListen implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            long execTime = System.currentTimeMillis();

            //scommentare per avviare solo i 3 sensori//
           /* if (execTime - tempoInizio > time * 1000) {
                try {
                    salvaArray(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sensorManager.unregisterListener(listen);
                Toast.makeText(getApplicationContext(),
                        "servizio terminato",
                        Toast.LENGTH_LONG).show();
                System.out.println("****stop****");
                stopSelf();
            }*/

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
               getAccelerometer(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                getGyroscope(event);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                getMagneticField(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
    }
}
