package com.fst.apps.ftelematics.utils;


import android.util.Log;

import com.fst.apps.ftelematics.entities.LastLocation;

import java.util.Comparator;

import static java.lang.Math.abs;

public class LocationComparator implements Comparator {
    Double fromLat, fromLong;



    public int compare(Object o1, Object o2) {
        LastLocation s1 = (LastLocation) o1;
        LastLocation s2 = (LastLocation) o2;
         if (s1.getDistanceFromLoc() == s2.getDistanceFromLoc())
            return 0;
        else if (s1.getDistanceFromLoc() > s2.getDistanceFromLoc())
            return 1;
        else
            return -1;
    }
}