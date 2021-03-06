package com.project.nicki.displaystabilizer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.project.nicki.displaystabilizer.UI.UIv1.ParamControl;
import com.project.nicki.displaystabilizer.UI.UIv1.UIv1_draw0;
import com.project.nicki.displaystabilizer.contentprovider.utils.TouchCollect;
import com.project.nicki.displaystabilizer.dataprocessor.MotionEstimation4.MotionEstimation4;
import com.project.nicki.displaystabilizer.dataprocessor.SensorCollect;
import com.project.nicki.displaystabilizer.dataprovider.SensorCollection;
import com.project.nicki.displaystabilizer.dataprovider.getAcceGyro;
import com.project.nicki.displaystabilizer.dataprovider.representation.Quaternion;
import com.project.nicki.displaystabilizer.dataprovider.udpBroadcast;
import com.project.nicki.displaystabilizer.dataprovider.udpRecieve;
import com.project.nicki.displaystabilizer.stabilization.stabilize_v3_1;
import com.project.nicki.displaystabilizer.stabilization.stabilize_v4;

/**
 * init.java: the start point of the app
 * DO:
 * # start threads
 * # initialize static classes
 * # start intents
 * # set parameters
 * # set unit
 */


public class init extends AppCompatActivity {
    ////# initialize static classes
    public static globalvariable initglobalvariable = new globalvariable();
    public static TouchCollect initTouchCollection = new TouchCollect();
    public static stabilize_v3_1 initStabilize = new stabilize_v3_1();
    public static  stabilize_v4 initStabilize_v4 = new stabilize_v4();
    ////# OBSOLETE
    public static String rk4_Log = "rk4_" + String.valueOf(System.currentTimeMillis());
    public static SensorCollect initSensorCollection = new SensorCollect();
    //Constants
    public static double widthm, heightcm, widthpix, heightpix, pix2m;
    String TAG = "init";
    //Thread
    public static Thread mudpbroadcast;
    public static Thread mudprecieve;


    ////# set parameters
    public static SharedPreferences getSharedPreferences(Context ctxt) {
        return ctxt.getSharedPreferences("FILE", 0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stabilize_v4 mstabilize_v4 = new stabilize_v4();
        Quaternion q = new Quaternion();
        q.setXYZW(2,3,4,5);
        q.normalise();
        mstabilize_v4.projectTouchVec(new float[]{1,1,1},new float[]{2,2,2},q,0.25f);
        ////# set unit
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthm = dm.widthPixels * 0.0254 / dm.xdpi;
        heightcm = dm.heightPixels * 0.0254 / dm.ydpi;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        widthpix = size.x;
        heightpix = size.y;
        pix2m = widthm / widthpix;

        ////# Start Threads
        try {
            new Thread(new getAcceGyro(getBaseContext())).start();
            new Thread(new SensorCollection(getBaseContext())).start();
            mudpbroadcast = new Thread(new udpBroadcast(getBaseContext(),init.initglobalvariable.ipportVal));
            mudpbroadcast.start();
            //mudprecieve = new Thread(new udpRecieve());
           // mudprecieve.start();

        } catch (Exception ex) {
            Log.e("init", String.valueOf(ex));
        }


        ////# start intents
        //Intent: Draw Screen
        Intent goto_UIv1_draw0 = new Intent();
        overridePendingTransition(0, 0);
        goto_UIv1_draw0.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        goto_UIv1_draw0.setClass(init.this, UIv1_draw0.class);
        startActivity(goto_UIv1_draw0);


        /** OBSOLETE
         //Intent: Splash Screen
         Intent goto_UIv1_splash0 = new Intent();
         overridePendingTransition(0, 0);
         goto_UIv1_splash0.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
         goto_UIv1_splash0.setClass(init.this, UIv1_splash.class);
         //startActivity(goto_UIv1_splash0);

         //Intent: Canvas(lipitoolkit)
         Intent goto_Canvas1 = new Intent();
         overridePendingTransition(0, 0);
         goto_Canvas1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
         goto_Canvas1.setClass(init.this, Canvas1.class);
         //startActivity(goto_Canvas1);

         Intent goto_UIv1_settings0= new Intent();
         overridePendingTransition(0, 0);
         goto_UIv1_settings0.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
         goto_UIv1_settings0.setClass(init.this, UIv1_settings0.class);
         startActivity(goto_UIv1_settings0);
         */

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}

