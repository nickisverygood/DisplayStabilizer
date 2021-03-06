package com.project.nicki.displaystabilizer.contentprovider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.canvas.LipiTKJNIInterface;
import com.canvas.LipitkResult;
import com.canvas.Stroke;
import com.project.nicki.displaystabilizer.dataprocessor.MotionEstimation3;
import com.project.nicki.displaystabilizer.dataprocessor.SensorCollect;
import com.project.nicki.displaystabilizer.init;
import com.project.nicki.displaystabilizer.stabilization.stabilize_v3;

import org.ejml.simple.SimpleMatrix;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.project.nicki.displaystabilizer.UI.UIv1.UIv1_draw0.pxToM;

public class DemoDraw3 extends View {
    ////Constants
    private final String TAG = "DemoDraw3";
    protected Context mContext;
    ////States
    public static boolean resetted = false;
    public static int StrokeResultCount = 0;
    public static boolean orienreset = false;
    public static int drawing = 3;
    public static boolean pending_quaternion_reset = false   ;
    ////Buffer
    public static List<List<stabilize_v3.Point>> sta_pending_to_draw = new ArrayList<>();
    public static List<List<stabilize_v3.Point>> ori_pending_to_draw = new ArrayList<>();
    public static List<List<stabilize_v3.Point>> motion_path = new ArrayList<>();
    public static List<stabilize_v3.Point> pending_to_draw_direct = new ArrayList<>();
    public static Path path = new Path();
    public static Path path2 = new Path();
    public static Path path3 = new Path();
    public static Path path4 = new Path();
    public static Paint paint = new Paint();
    public static Paint paint2 = new Paint();
    public static Paint paint3 = new Paint();
    public static Paint paint4 = new Paint();
    ////Handlers
    public static Handler clean_and_refresh;
    public static Handler refresh;
    ////LipiToolkit
    private LipiTKJNIInterface _lipitkInterface;
    private static LipiTKJNIInterface _recognizer = null;


    public DemoDraw3(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    public DemoDraw3(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Lipi init
        File externalFileDir = getContext().getExternalFilesDir(null);
        final String externalFileDirPath = externalFileDir.getPath();
        //Log.d("JNI", "Path: " + externalFileDirPath);
        _lipitkInterface = new LipiTKJNIInterface(externalFileDirPath, "SHAPEREC_ALPHANUM");
        _lipitkInterface.initialize();
        _recognizer = _lipitkInterface;

        //painter init
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

        paint3.setAntiAlias(true);
        paint3.setStrokeWidth(5f);
        paint3.setColor(Color.RED);
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeJoin(Paint.Join.ROUND);

        paint4.setAntiAlias(true);
        paint4.setStrokeWidth(2f);
        paint4.setColor(Color.BLUE);
        paint4.setStyle(Paint.Style.STROKE);
        paint4.setStrokeJoin(Paint.Join.ROUND);

        //Handlers init
        refresh = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                invalidate();
            }
        };
        clean_and_refresh = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                path = new Path();
                path2 = new Path();
                path3 = new Path();
                path4 = new Path();
                invalidate();
            }
        };

    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            //drawCanvas(canvas, path3, pending_to_draw_direct);
            draw_ListofPaths(canvas, paint3, path3, sta_pending_to_draw);
            draw_ListofPaths(canvas, paint4, path4, motion_path);

        } catch (Exception ex) {
            //Log.e("onDraw",String.valueOf(ex));
        }

        canvas.drawPath(path, paint);
        //canvas.drawPath(path2, paint2);
    }



    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final float eventX = event.getX();
        final float eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                init.initStabilize_v4.mstabilize_v3_func.initQuaternionReset();
                init.initglobalvariable.view2rawCoordinate_add = new float[]{
                        event.getRawX()-eventX,
                        event.getRawY()-eventY
                };
                resetted = false;
                orienreset = false;
                drawing = 0;
                pending_quaternion_reset = true;
                new passTouch(event);
                path.moveTo(eventX, eventY);
                path.lineTo(eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
                init.initglobalvariable.TouchVal = new float[]{
                        event.getX() ,
                        -event.getY()
                };
                Log.d("Tou",String.valueOf(eventX+"  "+ eventY));
                drawing = 1;
                new passTouch(event);
                path.lineTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                init.initStabilize_v4.mstabilize_v3_func.initQuaternionReset();
                new passTouch(event);
                drawing = 2;
                break;
            default:
                drawing = 3;
                return false;
        }

        // Schedules a repaint.
        invalidate();
        return true;
    }



    ////Toolkit
    //draw list of path
    public void draw_ListofPaths(Canvas canvas, Paint paint, Path path, List<List<stabilize_v3.Point>> pending_to_draw) {
        path = new Path();
        for(int i=0;i< pending_to_draw.size();i++){
            drawCanvas(canvas, path, pending_to_draw.get(i),paint);
        }

    }
    //drawCanvas
    private void drawCanvas(Canvas canvas, Path mpath, final List<stabilize_v3.Point> mpts, Paint paint) {
        List<stabilize_v3.Point> pts = new ArrayList<>(mpts);
        //rotate
        //finger_Xto0

        //TEMPORARY
        rotatePts(pts);


        /*==
        List<stabilize_v3.Point> tmppts = new ArrayList<>();
        for(int i=50;i<500;i++){
            tmppts.add(new stabilize_v3.Point(50,i));
        }
        if(tmppts.size()>1){
            double[][] Xto0 = new double[][]{{(double) (0 - tmppts.get(tmppts.size() - 1).x)}, {(double) (0 - tmppts.get(tmppts.size() - 1).y)}, {1}};
            SimpleMatrix Xto0_m = new SimpleMatrix(Xto0);
            SimpleMatrix rot_m = new SimpleMatrix(MotionEstimation3.currRot);
            for (int i = 0; i < tmppts.size(); i++) {
                SimpleMatrix ori = new SimpleMatrix(new double[][]{{(double) tmppts.get(i).x}, {(double) tmppts.get(i).y}, {1}});
                SimpleMatrix fin = rot_m.mult(ori.plus(Xto0_m)).minus(Xto0_m);
                tmppts.set(i,new stabilize_v3.Point((float) fin.get(0, 0), (float) fin.get(1, 0)));
                Log.i("matrix", String.valueOf(fin.toString()));
            }
        }

        pts.addAll(tmppts);
*/

        if (pts.size() > 1) {
            final int SMOOTH_VAL = 6;
            for (int i = pts.size() - 2; i < pts.size(); i++) {
                //Log.e("draw", String.valueOf(pts.get(i).x));
                if (i >= 0) {
                    stabilize_v3.Point point = pts.get(i);
                    if (i == 0) {
                        stabilize_v3.Point next = pts.get(i + 1);
                        point.dx = ((next.x - point.x) / SMOOTH_VAL);
                        point.dy = ((next.y - point.y) / SMOOTH_VAL);
                    } else if (i == pts.size() - 1) {
                        stabilize_v3.Point prev = pts.get(i - 1);
                        point.dx = ((point.x - prev.x) / SMOOTH_VAL);
                        point.dy = ((point.y - prev.y) / SMOOTH_VAL);
                    } else {
                        stabilize_v3.Point next = pts.get(i + 1);
                        stabilize_v3.Point prev = pts.get(i - 1);
                        point.dx = ((next.x - prev.x) / SMOOTH_VAL);
                        point.dy = ((next.y - prev.y) / SMOOTH_VAL);
                    }
                }
            }

            boolean first = true;
            for (int i = 0; i < pts.size(); i++) {
                stabilize_v3.Point point = pts.get(i);
                if (first) {
                    first = false;
                    mpath.moveTo(point.x, point.y);
                } else {
                    stabilize_v3.Point prev = pts.get(i - 1);
                    mpath.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);

                }
            }
            canvas.drawPath(mpath, paint);
        } else {
            if (pts.size() == 1) {
                //stabilize_v3.Point point = pts.get(0);
                //canvas.drawCircle(point.x, point.y, 2, paint3);
            }
        }
    }

    //pass touch
    public class passTouch {
        public passTouch(final MotionEvent event) {
            init.initTouchCollection.set_Touch(event);
        }
    }

    //recognize strokes
    public static recognized_data recognize_stroke(List<List<SensorCollect.sensordata>> lists) {
        if (lists.size() > 0) {
            String[] character = new String[0];

            List<Stroke> _strokes = new ArrayList<>();

            for (List<SensorCollect.sensordata> stroke : lists) {
                Stroke mstroke = new Stroke();
                for (SensorCollect.sensordata point : stroke) {
                    mstroke.addPoint(new PointF(point.getData()[0], point.getData()[1]));
                }
                _strokes.add(mstroke);
            }

            _strokes = merge_strokes(_strokes);

            Stroke[] _recognitionStrokes = new Stroke[_strokes.size()];
            for (int s = 0; s < _strokes.size(); s++)
                _recognitionStrokes[s] = _strokes.get(s);

            _recognitionStrokes = new Stroke[1];
            _recognitionStrokes[0] = _strokes.get(0);

            LipitkResult[] results = _recognizer.recognize(_recognitionStrokes);
            String configFileDirectory = _recognizer.getLipiDirectory() + "/projects/alphanumeric/config/";
            character = new String[results.length];
            for (int i = 0; i < character.length; i++) {
                character[i] = _recognizer.getSymbolName(results[i].Id, configFileDirectory);
                Log.e("jni", _recognizer.getSymbolName(results[i].Id, configFileDirectory) + " ShapeAID = " + results[i].Id + " Confidence = " + results[i].Confidence);
            }

            StrokeResultCount = results.length;

            return new recognized_data(results);
        } else {
            return null;
        }

    }

    //merge strokes
    private static List<Stroke> merge_strokes(List<Stroke> strokes) {
        List<Stroke> return_strokeList = new ArrayList<>();
        Stroke merged_stroke = new Stroke();
        for (Stroke mstroke : strokes) {
            for (int i = 0; i < mstroke.getPoints().size(); i++) {
                merged_stroke.addPoint(mstroke.getPointAt(i));
            }
        }
        return_strokeList.add(merged_stroke);
        return return_strokeList;
    }

    //rotate points
    public List<stabilize_v3.Point> rotatePts(List<stabilize_v3.Point> pts){
        if(pts.size()>1 ){
            double[][] Xto0 = new double[][]{{(double) (0 - pts.get(pts.size() - 1).x)}, {(double) (0 - pts.get(pts.size() - 1).y)}, {0}};
            SimpleMatrix Xto0_m = new SimpleMatrix(Xto0);
            SimpleMatrix rot_m = new SimpleMatrix(init.initglobalvariable.RotationVal);
            for (int i = 0; i < pts.size(); i++) {
                SimpleMatrix ori = new SimpleMatrix(new double[][]{{(double) pts.get(i).x}, {(double) pts.get(i).y}, {0}});
                SimpleMatrix fin = rot_m.mult(ori.plus(Xto0_m)).minus(Xto0_m);
                pts.set(i,new stabilize_v3.Point((float) fin.get(0, 0), (float) fin.get(1, 0)));
                //Log.i("matrix", String.valueOf(fin.toString()));
            }
        }
        return  pts;
    }

    ////Classes
    //recognizes buffer class
    public static class recognized_data {
        String configFileDirectory = _recognizer.getLipiDirectory() + "/projects/alphanumeric/config/";
        String[] characterArr;
        float[] confidenceArr;

        public recognized_data(LipitkResult[] results) {
            characterArr = new String[results.length];
            confidenceArr = new float[results.length];
            for (int i = 0; i < results.length; i++) {
                characterArr[i] = _recognizer.getSymbolName(results[i].Id, configFileDirectory);
                confidenceArr[i] = results[i].Confidence;
            }
        }

        public String getCharIndex(int i) {
            return characterArr[i];
        }

        public float getConfidenceIndex(int i) {
            return confidenceArr[i];
        }
    }



}