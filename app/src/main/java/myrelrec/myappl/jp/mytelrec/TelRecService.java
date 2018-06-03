package myrelrec.myappl.jp.mytelrec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class TelRecService extends Service {

    private final String LOG_TAG = getClass().getSimpleName();
    private final String KEY_FILE_TYPE = "key_fileType"; //どっか共通に出来る？（define in FragmentMain）

    private ServiceHandler mServiceHandler;
    private String mIntentFileType;

    private MyPhoneStateListener mMyListener = null;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage( Message msg ) {
            Log.d( LOG_TAG, "Thread works !!" );

            Context context = getApplicationContext();

//            //通知を発行するmanager
//            NotificationManager nManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );

            //通知エリアをタップされた時に起動するアプリを設定
            Intent notifyIntent = new Intent();
            notifyIntent.setClass( context, MainActivity.class );
            PendingIntent pendingIntent = PendingIntent.getActivity( context, 0, notifyIntent, 0 );

            //通知内容設定 (android 4 より下をサポートするなら NotificationCompat)
            Notification.Builder builder = new Notification.Builder( context );
            builder.setSmallIcon( R.mipmap.ic_launcher_round );
            builder.setContentTitle( "通話記録中" );
            builder.setContentText( "これは setContextText() です" );
            builder.setTicker( "setTicker() です。" );
            builder.addAction( R.drawable.ic_settings_white_24dp, "OK(表示してみただけです。)", (PendingIntent)null ); //ContentTextの下にでる。OK/Cancelボタンなど何某かのAction(PendingIntentで指定するのだろう)につなげる。APIレベルで・・・。とりあえず出たので。
            builder.setContentIntent( pendingIntent );

            startForeground( 111, builder.build() ); // id=0はダメ！！

            setMyListener(); //PhoneStateListener 登録
       }
    }

    @Override
    public void onCreate() {

        Log.d( LOG_TAG, "onCreate() started !" );

//        super.onCreate();

        HandlerThread thread = new HandlerThread( "ServiceStartArguments", Process.PHONE_UID );
        thread.start();

        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler( serviceLooper );

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d( LOG_TAG, "onStartCommand() started !" );

//        return super.onStartCommand(intent, flags, startId);

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage( msg );

        mIntentFileType = intent.getStringExtra( KEY_FILE_TYPE );
        Log.d( LOG_TAG, "mIntentFileType->" + mIntentFileType );

        // If we get killed, after returning from here, restart　、、復活してくれる。（と理解してるけれど）
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d( LOG_TAG, "onBind() started !" );
        return null; //他からのバインドは不許可
    }

    @Override
    public void onDestroy() {
        Log.d( LOG_TAG, "onDestroy()" );
        resetMyListener(); //PhoneStateListener 解除
        super.onDestroy();
    }

    //
    // private methods
    //
    private void setMyListener() {

        //TelephonyManagerの生成
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService( Context.TELEPHONY_SERVICE );
        //リスナーの登録
        if ( telephonyManager != null ) {
             mMyListener = new MyPhoneStateListener( this, mIntentFileType );
            telephonyManager.listen ( mMyListener, PhoneStateListener.LISTEN_CALL_STATE ); // Listen for changes to the device call state. //
            Log.d( LOG_TAG, "Telephony Listener set ! listener addr->"+mMyListener );
        } else {
            Log.d( LOG_TAG, "Telephony Listener Not set !" );
        }

    }

    private void resetMyListener() {

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService( Context.TELEPHONY_SERVICE );
        //リスナーの登録 解除
        if ( telephonyManager != null ) {
            Log.d( LOG_TAG, "Telephony Listener cancel ! listener addr->"+mMyListener );
            telephonyManager.listen ( mMyListener, PhoneStateListener.LISTEN_NONE ); // Listen for changes to the device call state. //
            mMyListener = null;
        } else {
            Log.d( LOG_TAG, "Telephony Listener Not cancel !" );
        }

    }
}
