package myrelrec.myappl.jp.mytelrec;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class MyPhoneStateListener extends PhoneStateListener implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    private final String LOG_TAG = getClass().getSimpleName();
    private  String recFilePath = "";      //録音ファイルの保存先
    private boolean mRecNow = false; //録音中はtrue
    private boolean mReceive = false; //着信の場合にtrue
    private MediaRecorder mMediaRecorder = null;
    private Context mContext;
    private String mFileType;
    private String mCallNumber = "";

    public MyPhoneStateListener( Context context, String fileType, String path ) {
        mContext = context;
        mFileType = fileType;
        recFilePath = path;
        Log.d( LOG_TAG, "fileType->" + mFileType + "file_path->" + recFilePath );
    }

    @Override
    public void onCallStateChanged(int state, String callNumber ) {
        //callNumberは着信番号みたい。発信はパーミッション PROCESS_OUTGOING_CALLS　が必要。(発信先番号とかは「発信の監視」になってしまうもんね。悪意があれば・・・)

        super.onCallStateChanged( state, callNumber );

        Log.d( LOG_TAG, "state :" + state );


        //for debug
        AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
        if ( audioManager != null ) {
            if ( audioManager.isBluetoothScoOn() ) {
                Log.d( LOG_TAG, "SCO on !!" );
            } else {
                Log.d( LOG_TAG, "SCO off !!" );
            }
        } else {
            Log.d( LOG_TAG, "AudioManager is null !" );
        }


        switch( state ){

            case TelephonyManager.CALL_STATE_IDLE: //待ち受け（終了時）
                Toast.makeText( mContext, "待ち受け", Toast.LENGTH_LONG ).show();
                Log.d( LOG_TAG, "待ち受け ( mRecNow->"+mRecNow+" )" );
                if ( mRecNow ) {
                    if ( mMediaRecorder != null ) {
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();   //オブジェクトのリセット
                        //release()前であればsetAudioSourceメソッドを呼び出すことで再利用可能
                        mMediaRecorder.release(); //
                        mMediaRecorder = null;
                    }
                    mRecNow = false;
                    mReceive = false;
                    mCallNumber = "";
                }
                break;

            case TelephonyManager.CALL_STATE_RINGING: //着信
//                Toast.makeText( mContext, "着信："+callNumber, Toast.LENGTH_LONG ).show();
                Log.d( LOG_TAG, "着信："+callNumber );
                mCallNumber = callNumber;
                mRecNow = false; //着信があるという事は通話中ではないと断定してしまう。キャッチホンとかあるのかな・・・。留守電モードとか・・・。
                mReceive = true;
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK: //通話
//                Toast.makeText( mContext, "受話／発信", Toast.LENGTH_LONG ).show();
                Log.d( LOG_TAG, "受話／発信 ( mRecNow->"+mRecNow+" )" );

                //
                //録音
                //
                if ( !mRecNow ) { //通話中でなければ録音（通話中じゃないはずでしょ・・・）
                    mRecNow = true;
                    startRecording();
                }
                break;

            default:
                break;
        } //switch()
    }

    //
    //private methods
    //
    private String editFileName() {

        String fileName;

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get( Calendar.YEAR );
        int month = calendar.get( Calendar.MONTH ) + 1;
        int dayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );
        int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY );
        int minute = calendar.get( Calendar.MINUTE );
        int second = calendar.get( Calendar.SECOND );


        EnumFormatList f = EnumFormatList.valueOf( mFileType ); // 無ければ Exception です。
        String extString = f.getValue();

        // YYYYMMDDhhmmss_tel.mp4 (locationが関係してくるなんて・・・)
        fileName = String.format( Locale.US, "%04d%02d%02d_%02d%02d%02d_%s%s.%s",
                year, month, dayOfMonth, hourOfDay, minute, second,
                mReceive?"r":"s", mReceive?"_"+mCallNumber:"", extString ); //着信の時は番号付加。発信の時は番号なし（とりあえず）。
        Log.d( LOG_TAG, "file name->"+fileName );

        return fileName;
    }

    private void startRecording() {

        String fileName = editFileName();

        String filePath = recFilePath + "/" + fileName;
        Log.d( LOG_TAG, "full path ->"+filePath );

        Log.d( LOG_TAG, "mRecNow == false" );

            File mediaFile = new File( filePath );
            if( mediaFile.exists() ) {
                //ファイルが存在する場合は削除する
                boolean rtn = mediaFile.delete();
                if ( rtn ) {
                    Log.d( LOG_TAG, "fail to delete file." );
                }
            }

            mMediaRecorder = new MediaRecorder();

            //音声ソースを指定
            AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
            if ( audioManager != null ) {
                if ( audioManager.isBluetoothScoOn() ) {
                    Log.d( LOG_TAG, "SCO mode is ON" );
                    mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.VOICE_COMMUNICATION );
                } else {
                    Log.d( LOG_TAG, "SCO mode is OFF" );
//                    mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.VOICE_CALL ); //電話の受話送話両方、、だと思う。
                    mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.VOICE_COMMUNICATION ); //moto g5s plus ではこちらでないと mediarecorder.start() で異常が発生する・・・。しかし、キチンと通話を録音できるのか？
                }
            } else {
                Log.d( LOG_TAG, "AudioManager is null." );
//                mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.VOICE_CALL ); //電話の受話送話両方、、だと思う。
                mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.VOICE_COMMUNICATION ); // //moto g5s plus ではこちらでないと mediarecorder.start() で異常が発生する・・・。しかし、キチンと通話を録音できるのか？
            }

            //
            //出力フォーマット      MediaRecorder.setOutputFormat()
            //   javaDoc ) --> Sets the format of the output file produced during recording.
            //                  Call this after setAudioSource()/setVideoSource() but before prepare().
            //
            //mMediaRecorder.setOutputFormat( MediaRecorder.OutputFormat.MPEG_4 );
            //
            //エンコード形式      MediaRecorder.setAudioEncoder()
            //   javaDoc ) --> Sets the audio encoder to be used for recording.
            //                  If this method is not called, the output file will not contain an audio track. Call this after setOutputFormat() but before prepare().
            //
            if ( mFileType.equals( EnumFormatList.MP4.getValue() ) ) {
                mMediaRecorder.setOutputFormat( MediaRecorder.OutputFormat.MPEG_4 );
                mMediaRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AAC );
            } else {
                mMediaRecorder.setOutputFormat( MediaRecorder.OutputFormat.DEFAULT );
                mMediaRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.DEFAULT );
            }

            //ファイルの保存先を指定
            mMediaRecorder.setOutputFile( filePath );

        try{
            //録音の準備をする
            mMediaRecorder.prepare();
            //録音開始
            mMediaRecorder.start();
            Log.d( LOG_TAG, "recorder start !" );
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.d( LOG_TAG, "onInfo() start. what="+what+" / extra="+extra  );

        //入ってこないのです・・・・・( 一一)
        //mediarecorder.start() で失敗する理由がわからない・・・・
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.d( LOG_TAG, "onError() start. what="+what+" / extra="+extra  );

        //ここにも入ってこないのです・・・・・( 一一)
        //mediarecorder.start() で失敗する理由がわからない・・・・
    }
}
