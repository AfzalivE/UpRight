package com.hackathon.ergomovement;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;

public class AnalyzerService extends AbstractService {
    private static final String TAG = Service.class.getSimpleName();
    private Timer timer = new Timer();

    public static int modeId = 1; //0 - hardcore, 1- medium, 2 - lazy

    public static AnalyzerService App;

    @Override
    public void onStartService() {
        App = this;
        Log.d(TAG, "Service started");
        setupSensor();
        MyTask t = new MyTask();
        // Increment counter and send to activity every 1000ms
        timer.scheduleAtFixedRate(t, 0, 1000);
    }

    public void pushMessage(int code) {
        send(Message.obtain(null, code));
    }

    @Override
    public void onStopService() {
        mSensorManager.unregisterListener(sa);
        Log.d(TAG, "Service stopped");
    }

    @Override
    public void onReceiveMessage(Message msg) {
        Log.d(TAG, "Receiving messages from activity");
        switch (msg.what) {
            case 0:
                modeId = 0;
                break;
            case 1:
                modeId = 1;
                break;
            case 2:
                modeId = 2;
                break;
            default:
                break;
        }
    }

    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;
    SensorActivity sa;

    private void setupSensor() {
        sa = new SensorActivity();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(sa, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}

class MyTask extends TimerTask {
    IntervalStatsAnalyzer xParser, yParser, zParser;
    int intervalLength = 5;
    Magic magic = new Magic();

    public MyTask() {
        xParser = new IntervalStatsAnalyzer(intervalLength);
        yParser = new IntervalStatsAnalyzer(intervalLength);
        zParser = new IntervalStatsAnalyzer(intervalLength);
    }

    public void run() {
        //Log.d("TEST", "Values are " + SensorActivity.mSensorX +", "+ SensorActivity.mSensorY +", "+ SensorActivity.mSensorZ);
        xParser.pushNumber(SensorActivity.mSensorX);
        yParser.pushNumber(SensorActivity.mSensorY);
        zParser.pushNumber(SensorActivity.mSensorZ);

        IntervalStats xStats = xParser.GetStats();
        IntervalStats yStats = yParser.GetStats();
        IntervalStats zStats = zParser.GetStats();

        Log.d("TEST", "Mean values are " + xStats.Mean + ", " + yStats.Mean + ", " + zStats.Mean);
        Log.d("TEST", "Amplitude values are " + xStats.get_Amplitude() + ", " + yStats.get_Amplitude() + ", " + zStats.get_Amplitude());

        XYZ mean = new XYZ(xStats.Mean, yStats.Mean, zStats.Mean);
        XYZ amp = new XYZ(xStats.get_Amplitude(), yStats.get_Amplitude(), zStats.get_Amplitude());

        magic.setStats(mean, amp);
    }
}

class XYZ {
    public XYZ() {
    }

    public XYZ(float x, float y, float z) {
        X = x;
        Y = y;
        Z = z;
    }

    public float X, Y, Z;

    public float Sum() {
        return X + Y + Z;
    }

    public float Mean() {
        return Sum() / 3;
    }
}

class Magic {
    static final int STATUS_SITTING = 0;
    static final int STATUS_JUMPING = 1;
    static final int STATUS_FUCKEDUP = 2;

    double magic_value_x_HC = 8;
    double magic_value_z_HC = 3.5;
    double magic_value_amp_HC = 4;

    double magic_value_x_LZ = 8;
    double magic_value_z_LZ = 3.5;
    double magic_value_amp_LZ = 4;

    double magic_value_x = 8;
    double magic_value_z = 3.5;
    double magic_value_amp = 4;

    public void setStats(XYZ mean, XYZ amplitude) {
        if (amplitude.Sum() > getAValue(AnalyzerService.modeId)) {
            AnalyzerService.App.pushMessage(STATUS_JUMPING);
        } else if (mean.X < getXValue(AnalyzerService.modeId) || mean.Z < getZValue(AnalyzerService.modeId)) {
            AnalyzerService.App.pushMessage(STATUS_FUCKEDUP);
        } else {
            AnalyzerService.App.pushMessage(STATUS_SITTING);
        }
    }

    private double getXValue(int mode) {
        if(mode == 0)
            return magic_value_x_HC;
        else if(mode == 1)
            return magic_value_x;
        else
            return magic_value_x_LZ;
    }

    private double getZValue(int mode) {
        if(mode == 0)
            return magic_value_z_HC;
        else if(mode == 1)
            return magic_value_z;
        else
            return magic_value_z_LZ;
    }

    private double getAValue(int mode) {
        if(mode == 0)
            return magic_value_amp_HC;
        else if(mode == 1)
            return magic_value_amp;
        else
            return magic_value_amp_LZ;
    }
}

class IntervalStatsAnalyzer {
    float[] stats;

    public IntervalStatsAnalyzer(int length) {
        stats = new float[length];
    }

    public void pushNumber(float n) {
        for (int i = 0; i < stats.length - 1; i++) {
            stats[i] = stats[i + 1];
        }
        stats[stats.length - 1] = n;
    }

    public IntervalStats GetStats() {
        IntervalStats res = new IntervalStats();
        float sum = 0, min = stats[0], max = stats[0];
        for (int i = 0; i < stats.length; i++) {
            float n = stats[i];
            sum += n;
            if (n > max)
                max = n;
            if (n < min)
                min = n;
        }
        res.Mean = sum / stats.length;
        res.Top = max;
        res.Bottom = min;
        return res;
    }
}

class IntervalStats {
    public float Mean, Top, Bottom;

    public float get_Amplitude() {
        return Top - Bottom;
    }
}

class SensorActivity implements SensorEventListener {
    public static float mSensorX, mSensorY, mSensorZ;

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        //Right in here is where you put code to read the current sensor values and
        //update any views you might have that are displaying the sensor information
        //You'd get accelerometer values like this:
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        mSensorX = event.values[1];
        mSensorY = event.values[0];
        mSensorZ = event.values[2];
    }
}
