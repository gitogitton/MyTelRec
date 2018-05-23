package myrelrec.myappl.jp.mytelrec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final String LOG_TAG = getClass().getSimpleName();

    private Context mContext;
    private boolean mIdle = true;
    private MyPhoneStateListener mMyPhoneStateListener;

    @Override
    public void onReceive( Context context, Intent intent ) {

        Log.d( LOG_TAG, "onReceive() start." );

        mContext = context;
        mMyPhoneStateListener = new MyPhoneStateListener( mContext, "" ); //5/23: 今はこのクラスは使ってないので""で逃げておく。

        try {

            //TelephonyManagerの生成
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService( Context.TELEPHONY_SERVICE );
            //リスナーの登録
            if ( telephonyManager != null ) {
                telephonyManager.listen ( mMyPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE ); // Listen for changes to the device call state. //
            }
//リスナー解除方法
//        telephonyManager.listen ( PhoneListener, PhoneStateListener.LISTEN_NONE (0)
//        (Note: if you call this method while in the middle of a binder transaction, you must call clearCallingIdentity() before calling this method. A SecurityException will be thrown otherwise.)
//
        } catch ( Exception e ) {
            Log.e( LOG_TAG, ":" + e );
        }
    }
}
