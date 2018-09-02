package myrelrec.myappl.jp.mytelrec;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

public class TelRecIntentService extends IntentService {

    private final String LOG_TAG = getClass().getSimpleName();

    public TelRecIntentService() {
        super( "TelRecIntentService" );
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TelRecIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d( LOG_TAG, "onHandleIntent() start." );

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

        Log.d( LOG_TAG, "onHandleIntent() end." );

    }

    //
    // private methods
    //
    private void startRecorder() {

        //録音
        MediaRecorder mediarecorder; //録音用のメディアレコーダークラス
        final String filePath = SettingData.sRecordingFilePath +"/sample.mp4"; //録音用のファイルパス

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
