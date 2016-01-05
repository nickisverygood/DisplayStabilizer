package com.project.nicki.displaystabilizer.contentprovider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.project.nicki.displaystabilizer.dataprocessor.proDataFlow;
import com.project.nicki.displaystabilizer.stabilization.stabilize_v1;
import com.project.nicki.displaystabilizer.stabilization.stabilize_v2;

public class DemoDraw extends View {
    private static final String TAG = "DemoDraw";
    public static int drawing = 0;
    public static Paint paint2 = new Paint();
    public static Path path2 = new Path();
    public static Rect rectangle;
    public static Handler mhandler;
    public static Paint paint = new Paint();
    public Path path = new Path();
    public static int rectX,rectY,sideLength;
    protected Context mContext;
    private boolean clear = false;

    public DemoDraw(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    public DemoDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        paint2.setAntiAlias(true);
        paint2.setStrokeWidth(5f);
        paint2.setColor(Color.BLACK);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeJoin(Paint.Join.ROUND);



        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                invalidate();

            }
        };

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Bitmap bitmap=Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        //Canvas mcanvas=new Canvas(bitmap);
        /*
        if (clear) {  // Choose the colour you want to clear with.
            path.reset();
            path2.reset();
            clear = false;
        }
        */
        canvas.drawPath(path, paint);
        canvas.drawPath(path2, paint2);
        // create a rectangle that we'll draw later
        rectangle = new Rect(rectX-sideLength,rectY-sideLength,rectX,rectY);
        canvas.drawRect(rectangle, paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clear = true;
                path.reset();
                path2.reset();
                invalidate();
                Log.d(TAG, "AAAA down");
                Message msgSTART = new Message();
                msgSTART.what = 0;

                float[] dataSTART = new float[2];
                long currTimeSTART = System.currentTimeMillis();
                Bundle drawposBundleSTART = new Bundle();
                dataSTART[0] = eventX;
                dataSTART[1] = eventY;
                drawposBundleSTART.putFloatArray("Draw", dataSTART);
                drawposBundleSTART.putLong("Time", currTimeSTART);
                msgSTART.setData(drawposBundleSTART);

                if(eventX != 0 || eventY!=0){
                    //proDataFlow.drawHandler.sendMessage(msgSTART);
                    stabilize_v2.getDatas.sendMessage(msgSTART);
                }

                //stabilize_v1.getDatas.sendMessage(msgSTART);

                drawing = 0;
                path.moveTo(eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
                clear = false;
                Log.d(TAG, "AAAA Drawing");
                Message msgDRAWING = new Message();
                msgDRAWING.what = 1;

                float[] dataDRAWING = new float[2];
                long currTimeDRAWING = System.currentTimeMillis();
                Message msgDrawing = new Message();
                Bundle drawposBundleDRAWING = new Bundle();
                dataDRAWING[0] = eventX;
                dataDRAWING[1] = eventY;
                drawposBundleDRAWING.putFloatArray("Draw", dataDRAWING);
                drawposBundleDRAWING.putLong("Time", currTimeDRAWING);
                msgDRAWING.setData(drawposBundleDRAWING);

                if(eventX != 0 || eventY!=0){
                    proDataFlow.drawHandler.sendMessage(msgDRAWING);
                    //s7tabilize_v1.mhandler.sendMessage(msgDRAWING);
                    //stabilize_v2.getDatas.sendMessage(msgDRAWING);
                    //stabilize_v1.getDatas.sendMessage(msgDRAWING);
                }




                rectX = (int) eventX;
                rectY = (int) eventY;

                Log.d(TAG, "rectXY " + DemoDraw.rectX + " " + DemoDraw.rectY);
                DemoDraw.sideLength = 200;
                invalidate();

                drawing = 1;
                for (int i = 0; i < event.getHistorySize(); i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    path.lineTo(historicalX, historicalY);
                }
                path.lineTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                clear = false;

                Log.d(TAG, "AAAA up");
                Message msgSTOP = new Message();
                msgSTOP.what = 2;
                float[] dataSTOP = new float[2];
                long currTimeSTOP = System.currentTimeMillis();
                Bundle drawposBundleSTOP = new Bundle();
                dataSTOP[0] = eventX;
                dataSTOP[1] = eventY;
                drawposBundleSTOP.putFloatArray("Draw", dataSTOP);
                drawposBundleSTOP.putLong("Time", currTimeSTOP);
                msgSTOP.setData(drawposBundleSTOP);


                if(eventX != 0 || eventY!=0){
                    proDataFlow.drawHandler.sendMessage(msgSTOP);
                    //stabilize_v1.getDatas.sendMessage(msgSTOP);
                }



                drawing = 2;
                // nothing to do
                break;
            default:
                drawing = 2;
                return false;
        }

        // Schedules a repaint.
        invalidate();
        return true;
    }


}