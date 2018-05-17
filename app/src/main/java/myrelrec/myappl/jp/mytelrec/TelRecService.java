package myrelrec.myappl.jp.mytelrec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class TelRecService extends Service {

    private final String LOG_TAG = getClass().getSimpleName();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage( Message msg ) {
            Log.d( LOG_TAG, "Thread works !!" );

            Context context = getApplicationContext();

            //通知を発行するmanager
            NotificationManager nManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );

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
            builder.setContentIntent( pendingIntent );

            startForeground( 111, builder.build() ); // id=0はダメ！！

            startRecorder();

//            try {
//                Thread.sleep( 30000 );
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
       }
    }

    @Override
    public void onCreate() {

//        super.onCreate();

        HandlerThread thread = new HandlerThread( "ServiceStartArguments", Process.PHONE_UID );
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler( mServiceLooper );

        Log.d( LOG_TAG, "onCreate() started !" );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText( this, "TelRecService starting", Toast.LENGTH_SHORT ).show();
//        return super.onStartCommand(intent, flags, startId);

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart　、、復活してくれる。（と理解してるけれど）
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; //他からのバインドは不許可
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d( LOG_TAG, "onDestroy()" );
    }

    //
    // private methods
    //
    private void startRecorder() {

        //録音
        MediaRecorder mediarecorder; //録音用のメディアレコーダークラス
        final String filePath = "/storage/sdcard0/telrec/sample.mp4"; //録音用のファイルパス

        try{
            File mediafile = new File(filePath);
            if( mediafile.exists() ) {
                //ファイルが存在する場合は削除する
                mediafile.delete();
            }
            mediafile = null;
            mediarecorder = new MediaRecorder();
            //音声ソースをマイクに指定
//            mediarecorder.setAudioSource( MediaRecorder.AudioSource.MIC );
            mediarecorder.setAudioSource( MediaRecorder.AudioSource.VOICE_CALL ); //電話の受話送話両方、、だと思う。
            //出力フォーマットに DEFAULT を指定（DEFAULTって何？WAV？）
            mediarecorder.setOutputFormat( MediaRecorder.OutputFormat.MPEG_4 );
            //エンコーダーも Default にする  ???
            mediarecorder.setAudioEncoder( MediaRecorder.AudioEncoder.DEFAULT );
            //ファイルの保存先を指定
            mediarecorder.setOutputFile( filePath );
            //録音の準備をする
            mediarecorder.prepare();
            //録音開始
            Log.d( LOG_TAG, "recorder start !" );
            mediarecorder.start();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
