package com.example.myapplication.model;

public class Punto {

    float x,y,z;
    long timestamp;

    public Punto(float x,float y,float z,long timestamp){
        this.x=x;
        this.y=y;
        this.z=z;
        this.timestamp=timestamp;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getZ(){
        return z;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public String toString(){
        return ("x: "+x+" y: "+y+" z: "+z);
    }
}