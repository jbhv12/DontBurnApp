package com.example.jbhv12.dontburn;

/**
 * Created by jbhv12 on 14/09/17.
 */

public class Leg {
    public int distance,duration;
    public double dir_start, dir_end;
    public Leg(double dir_end, double dir_start,int distance, int duration){
        this.dir_end = dir_end;
        this.dir_start = dir_start;
        this.distance = distance;
        this.duration = duration;
    }
}
