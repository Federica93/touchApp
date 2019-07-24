package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ConvertActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);

        Button buttonLoad= findViewById(R.id.openFile);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    performFileSearch();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void performFileSearch() throws IOException {

        TextView textTime = (TextView) findViewById(R.id.fileName);
        String fileName = textTime.getText().toString();
        try {
            File f = new File(getApplication().getFilesDir(), fileName + "_mergeFile.csv");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            float[] orientation = new float[3];
            ArrayList<String> strings = new ArrayList<>();
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String[] row = line.split(";");
                orientation = getOrientation(row);

                String newLine = row[0] + ";" + row[1] + ";" + row[2] + ";" + row[6] + ";" + row[7] + ";" + row[8] + ";"
                        + Math.toDegrees(Double.parseDouble(Float.toString(orientation[0]))) + ";"
                        + Math.toDegrees(Double.parseDouble(Float.toString(orientation[1]))) + ";"
                        + Math.toDegrees(Double.parseDouble(Float.toString(orientation[2]))) + ";"
                        + row[9] + ";" + row[10] + ";" + row[11] + ";" + row[12];
                strings.add(newLine);
                //System.out.println(row.length);
            }
            br.close();

            FileOutputStream outputStream;
            outputStream = openFileOutput(fileName + "_mergeFile_def.csv", Context.MODE_PRIVATE);
            outputStream.write(("accX;accY;accZ;gyrX;gyrY;gyrZ;orientX;orientY;orientZ;mode;walker;phone;sex\n").getBytes());
            for (String s : strings)
                outputStream.write((s + "\n").getBytes());
            outputStream.flush();
            outputStream.close();

            fileUnion(strings);
        } catch (FileNotFoundException f){
            Toast.makeText(this,
                    "file non trovato",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void fileUnion(ArrayList<String> arr){
        try {
            ArrayList<String> tot=new ArrayList<>();
            File f = new File(getApplication().getFilesDir(), "mergeTot.csv");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            ArrayList<String> strings = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                tot.add(line);
            }
            br.close();
            tot.addAll(arr);
            FileOutputStream outputStream;
            outputStream = openFileOutput("mergeTot.csv", Context.MODE_PRIVATE);
            for (String s:tot)
                outputStream.write((s+"\n").getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public float[] getOrientation(String[] line){
        float[] acc=new float[3];
        float[] mag=new float[3];
        acc[0]=Float.parseFloat(line[0]);
        acc[1]=Float.parseFloat(line[1]);
        acc[2]=Float.parseFloat(line[2]);
        mag[0]=Float.parseFloat(line[3]);
        mag[1]=Float.parseFloat(line[4]);
        mag[2]=Float.parseFloat(line[5]);

        float[] rotation=new float[9];
        float[] orientation=new float[3];
        SensorManager.getRotationMatrix(rotation,null,acc,mag);
        SensorManager.getOrientation(rotation,orientation);

        return orientation;
    }
}