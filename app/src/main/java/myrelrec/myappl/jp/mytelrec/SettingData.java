package myrelrec.myappl.jp.mytelrec;

import android.content.Context;

import java.io.File;
import java.io.Serializable;

public class SettingData implements Serializable {

    private String mFormat;
    private boolean mAutoStart;
    private boolean mBluetooth;

    public SettingData() {
    }

    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String format) {
        this.mFormat = format;
    }

    public boolean isAutoStart() {
        return mAutoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.mAutoStart = autoStart;
    }

    public boolean isBluetooth() {
        return mBluetooth;
    }

    public void setBluetooth(boolean bluetooth) {
        this.mBluetooth = bluetooth;
    }
}
