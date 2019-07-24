package com.example.myapplication;

import android.hardware.SensorManager;

import com.example.myapplication.model.Punto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class OrderFile {

    ArrayList<Punto> accelerometro, magnetometro, giroscopio;

    public OrderFile(ArrayList accelerometro, ArrayList magnetometro, ArrayList giroscopio) {
        this.accelerometro = accelerometro;
        this.magnetometro = magnetometro;
        this.giroscopio = giroscopio;
    }

    public void order() {
        ArrayList<Punto> newGiroscopio = new ArrayList<>();
        ArrayList<Punto> newMagnetometro = new ArrayList<>();
        for (Punto punto : accelerometro) {
            newGiroscopio.add(findAvgPoint(punto.getTimestamp(), giroscopio));
            newMagnetometro.add(findAvgPoint(punto.getTimestamp(), magnetometro));
        }

        magnetometro = newMagnetometro;
        giroscopio = newGiroscopio;
    }

    public Punto findAvgPoint(long timestamp, ArrayList<Punto> sensor) {
        Punto min1 = findPrecPoint(timestamp, sensor);
        sensor.remove(min1);
        //Punto min2=findMinPoint(timestamp,sensor);
        Punto min2 = findSuccPoint(timestamp, sensor);
        sensor.add(min1);

        float avgX = (min1.getX() + min2.getX()) / 2;
        float avgY = (min1.getY() + min2.getY()) / 2;
        float avgZ = (min1.getZ() + min2.getZ()) / 2;

        Punto p = new Punto(avgX, avgY, avgZ, timestamp);
        System.out.println(p.toString());

        return new Punto(avgX, avgY, avgZ, timestamp);
    }

    public Punto findPrecPoint(long timestamp, ArrayList<Punto> sensor) {
        long min = timestamp;
        System.out.println("minimo: " + min);
        Punto result = new Punto(0, 0, 0, 0);
        for (Punto punto : sensor) {
            if (Math.abs(punto.getTimestamp() - timestamp) < min && timestamp > punto.getTimestamp()) {
                result = punto;
                min = Math.abs(punto.getTimestamp() - timestamp);
            }
        }
        return result;
    }

    public Punto findSuccPoint(long timestamp, ArrayList<Punto> sensor) {
        long min = timestamp;
        System.out.println("minimo: " + min);
        Punto result = new Punto(0, 0, 0, 0);
        for (Punto punto : sensor) {
            if (Math.abs(punto.getTimestamp() - timestamp) < min && timestamp < punto.getTimestamp()) {
                result = punto;
                min = Math.abs(punto.getTimestamp() - timestamp);
            }
        }
        return result;
    }

    public ArrayList getAcc() {
        return accelerometro;
    }

    public ArrayList getMag() {
        return magnetometro;
    }

    public ArrayList getGyr() {
        return giroscopio;
    }

    public void calculateOrientation(){

        float[] rMatrix = new float[9];
        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrixFromVector(rMatrix, orientationValues);

        //calculate Euler angles now
        SensorManager.getOrientation(rMatrix, orientationValues);

        //The results are in radians, need to convert it to degrees
        convertToDegrees(orientationValues);
    }

    private void convertToDegrees(float[] vector){
        for (int i = 0; i < vector.length; i++){
            vector[i] = Math.round(Math.toDegrees(vector[i]));
        }
        System.out.println("yaw: "+vector[0]+" pitch: "+vector[1]+" roll: "+vector[2]);

    }
    public static void main(String[] args) throws FileNotFoundException {

        Scanner sc = new Scanner(new File("C:\\Users\\feder\\Desktop\\Vecchie Maria_Al\\XiaomiMiA2Lite\\Maria\\MARIA_UP_accelerometro.csv"));

        ArrayList<Punto> acc = new ArrayList<>();

        String lin = sc.nextLine();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] number = line.split(";");
            Punto p = new Punto(Float.parseFloat(number[0]), Float.parseFloat(number[1]), Float.parseFloat(number[2]), Long.parseLong(number[3]));
            System.out.println(line);
            acc.add(p);
        }

        sc = new Scanner(new File("C:\\Users\\feder\\Desktop\\Vecchie Maria_Al\\XiaomiMiA2Lite\\Maria\\MARIA_UP_magnetometro.csv"));

        ArrayList<Punto> mag = new ArrayList<>();
        lin = sc.nextLine();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] number = line.split(";");
            Punto p = new Punto(Float.parseFloat(number[0]), Float.parseFloat(number[1]), Float.parseFloat(number[2]), Long.parseLong(number[3]));
            mag.add(p);
        }


        sc = new Scanner(new File("C:\\Users\\feder\\Desktop\\Vecchie Maria_Al\\XiaomiMiA2Lite\\Maria\\MARIA_UP_giroscopio.csv"));
        lin = sc.nextLine();
        ArrayList<Punto> gyr = new ArrayList<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] number = line.split(";");
            Punto p = new Punto(Float.parseFloat(number[0]), Float.parseFloat(number[1]), Float.parseFloat(number[2]), Long.parseLong(number[3]));
            gyr.add(p);
        }

        OrderFile o = new OrderFile(acc, mag, gyr);
        o.calculateOrientation();
       /* ArrayList<Punto> accelerometro = new ArrayList<>();
        ArrayList<Punto> magnetometro = new ArrayList<>();
        ArrayList<Punto> giroscopio = new ArrayList<>();

        // metti le info
        String mode = "upstairs";
        String walker = "f007";
        String phone = "XiaomiMiA2Lite";
        String sex = "female";

        accelerometro = o.getAcc();
        magnetometro = o.getMag();
        giroscopio = o.getGyr();

        PrintWriter p = new PrintWriter("maria_upstairs_merge.csv");
        p.print("accX;accY;accZ;magX;magY;magZ;gyrX;gyrY;gyrZ;mode;walker;phone;sex\n");

        for (int i = 0; i < acc.size(); i++) {
            p.print(accelerometro.get(i).getX() + ";" + accelerometro.get(i).getY() +
                    ";" + accelerometro.get(i).getZ() + ";" + magnetometro.get(i).getX() + ";" + magnetometro.get(i).getY() +
                    ";" + magnetometro.get(i).getZ() + ";" + giroscopio.get(i).getX() + ";" + giroscopio.get(i).getY() +
                    ";" + giroscopio.get(i).getZ() + ";" + mode + ";" + walker + ";" + phone + ";" + sex + "\n");
        }
        p.close();*/
    }
}
