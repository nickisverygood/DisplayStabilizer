package com.project.nicki.displaystabilizer.dataprocessor.utils.Filters;

import android.util.Log;

import com.project.nicki.displaystabilizer.init;

import java.util.ArrayList;
import java.util.List;

import jama.Matrix;
import jkalman.JKalman;

import static java.lang.Float.NaN;

/**
 * Created by nickisverygood on 3/6/2016.
 */
public class filterCollection {

    //HighPassFilter
    public float[] LowFreq;
    //LowPassFilter
    float[] output;
    //KalmanFilter
    JKalman kalman = null;
    Matrix s, c, m;
    //MovingAvgFilter
    private List<Float>[] rolling;

    public float[] HighPassFilter(float[] data, float alpha) {

        if (LowFreq == null) {
            LowFreq = new float[data.length];
            System.arraycopy(data, 0, LowFreq, 0, data.length);
            return data;
        }

        for (int i = 0; i < data.length; i++) {
            LowFreq[i] = alpha * LowFreq[i] + (1 - alpha) * data[i];
            data[i] = data[i] - LowFreq[i];
        }

        return data;
    }

    public float[] LowPassFilter(float[] data, float alpha) {
        if (output == null) {
            output = new float[data.length];
            System.arraycopy(data, 0, output, 0, data.length);
            return data;
        }
        for (int i = 0; i < data.length; i++) {
            output[i] = alpha * data[i] + (1 - alpha) * output[i];
        }
        return output;
    }

    public float[] MovingAvgFilter(float[] data, int sample) {
        if (rolling == null) {
            rolling = (ArrayList<Float>[]) new ArrayList[data.length];
            for (int i = 0; i < data.length; i++) {
                rolling[i] = new ArrayList<>();
            }
        }

        //reshape
        for (int i = 0; i < data.length; i++) {
            while (rolling[i].size()>sample){
                rolling[i].remove(0);
            }
        }

        float[] toreturn = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            if (rolling[i].size() < sample || sample<2) {
                rolling[i].add(data[i]);
            } else {
                rolling[i].remove(0);
                rolling[i].add(data[i]);
            }
            toreturn[i] = calculateAverage(rolling[i]);
        }

        return toreturn;
    }

    private float calculateAverage(List<Float> marks) {
        Float sum = 0f;
        if (!marks.isEmpty()) {
            for (Float mark : marks) {
                sum += mark;
            }
            return sum.floatValue() / marks.size();
        }
        return sum;
    }

    //StaticFilter
    public float[] StaticFilter(float[] data, boolean static_sta) {
        //Log.d("static?", String.valueOf(static_sta));
        try {
            if (init.initglobalvariable.sStaticVal.getLatestData().getValues()[0] == 1 && static_sta == true) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = 0;
                }
                return data;
            } else {
                return data;
            }
        }catch (Exception ex){
            return data;
        }

    }

    //StaticFilter
    public float[] TooHighFilter(float[] data, float threshold) {
        /*
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] > threshold) {
                buffer[i] = 0;
            }
        }
        */
        for (int i = 0; i < data.length; i++) {
            if (Math.abs(data[i]) > Math.abs(threshold)) {
                data[i] = 0;
            }
        }
        return data;
    }

    public float[] KalmanFilter(float[] data, boolean activate) {
        if (activate == true && data.length ==3) {
            if (kalman == null ) {

                try {
                    //kalman = new JKalman(4, 2);
                    kalman = new JKalman(6, 3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                double x = 0;
                double y = 0;
                double z = 0;

                // init

                //Matrix s = new Matrix(4, 1); // state [x, y, dx, dy, dxy]
                //Matrix c = new Matrix(4, 1); // corrected state [x, y, dx, dy, dxy]
                //Matrix m = new Matrix(2, 1); // measurement [x]
                Matrix s = new Matrix(6, 1); // state [x, y, z, dx, dy, dz]
                Matrix c = new Matrix(6, 1); // corrected state
                Matrix m = new Matrix(3, 1); // measurement [x, y, z]

                //m.set(0, 0, x);
                //m.set(1, 0, y);
                m.set(0, 0, x);
                m.set(1, 0, y);
                m.set(2, 0, z);

                // transitions for x, y, dx, dy
            /*
            double[][] tr =
                    {{1, 0, 1, 0},
                            {0, 1, 0, 1},
                            {0, 0, 1, 0},
                            {0, 0, 0, 1}};
            kalman.setTransition_matrix(new Matrix(tr));
            */
                double[][] tr = {{1, 0, 0, 1, 0, 0},
                        {0, 1, 0, 0, 1, 0},
                        {0, 0, 1, 0, 0, 1},
                        {0, 0, 0, 1, 0, 0},
                        {0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 1}};
                kalman.setTransition_matrix(new Matrix(tr));
                // 1s somewhere?
                kalman.setError_cov_post(kalman.getError_cov_post().identity());
                return data;
            }

            // check state before
            s = kalman.Predict();

            // function init :)
            // m.set(1, 0, rand.nextDouble());
            try {
                Matrix m = new Matrix(3, 1); // measurement [x]
                m.set(0, 0, (double) data[0]);
                m.set(1, 0, (double) data[1]);
                m.set(2, 0, (double) data[2]);
                // look better
                c = kalman.Correct(m);
                s = kalman.Predict();
                //Log.d("kalman", String.valueOf((float) c.get(0, 0)));
                if((float) c.get(0, 0) != NaN && (float) c.get(1, 0) != NaN && (float) c.get(2, 0) != NaN ){
                    return new float[]{(float) c.get(0, 0), (float) c.get(1, 0), (float) c.get(2, 0)};
                }else{
                    return data;
                }

            } catch (Exception ex) {
                return data;
                }

        }
        return data;
    }


}

