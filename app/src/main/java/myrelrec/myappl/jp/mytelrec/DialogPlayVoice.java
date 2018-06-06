package myrelrec.myappl.jp.mytelrec;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class DialogPlayVoice extends DialogFragment implements MediaPlayer.OnErrorListener {

    private final String LOG_TAG = getClass().getSimpleName();
    private final String mDialogTitle = "音声を再生";
    private final String mFilePath = "/storage/sdcard0/telrec/";
    private String mMessage = "";
    private View mView;
    private Context mContext;
    private int mLastProgress = -1; //SeekBarの位置。volume値とは違う。バー割合（みたい感じ）です！！勘違いするなよぉ～。もうしたけど・・・
    private int mLastPos = -1;
    private MediaPlayer mMediaPlayer = null;
    private Handler mHandler;
    private Runnable mRunnable;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        Log.d( LOG_TAG, "onCreateDialog()" );

        Bundle args = getArguments();
        if ( args != null ) {
            mMessage = args.getString( "target_file" );
        }

        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

//        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        LayoutInflater inflater = LayoutInflater.from( mContext );
        mView = inflater.inflate( R.layout.dialog_play_voice, (ViewGroup) null );

        builder.setView( mView );
        builder.setTitle( mDialogTitle );
        builder.setMessage( mMessage );

        builder.setPositiveButton("終了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Log.d( LOG_TAG, "click positive button" );
            }
        });

//        builder.setNegativeButton("NG", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Log.d( LOG_TAG, "click negative button" );
//            }
//        });

        initSeekBar();

        setListeners();

        startAudio();

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        Log.d( LOG_TAG, "onAttach()" );
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onPause() {
        Log.d( LOG_TAG, "onPause()" );
        super.onPause();
    }

    @Override
    public void onDetach() {
        Log.d( LOG_TAG, "onDetach()" );

        if ( mMediaPlayer != null ) { //mMediaPlayerのcreate()に失敗した時はmHandler,mRunnableをnewしないので問題ないはず！
            mHandler.removeCallbacks( mRunnable );

            if ( mMediaPlayer.isPlaying() ) { mMediaPlayer.stop(); } //いらない？
            mMediaPlayer.reset(); // mediaPlayer become idle.
            mMediaPlayer.release();
        }

        super.onDetach();
    }

    //
    // private methods
    //
    private void initSeekBar() {
        //再生位置のseekBarを現在の値に合わせる。
        SeekBar seekBarPlayPos = mView.findViewById( R.id.seekBar_play_position );
        seekBarPlayPos.setProgress( 0 ); //初期状態なので最初から

        //音声ファイルの最大再生時間が必要？


        //ボリュームのseekBarを現在の値に合わせる。
        SeekBar seekBarVolume = mView.findViewById( R.id.seekBar_volume );
        AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
        int volume = 0;
        if (audioManager != null) {
            volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        }
        int volumeMax = 0;
        if (audioManager != null) {
            volumeMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
        }
        int progressValue = (int)( ( (float)volume/(float)volumeMax) * (float)100.0 );
        seekBarVolume.setProgress( progressValue );
        //Log.d( LOG_TAG, "initSeekBar() currentVol/maxVol/progressVal->" + volume + " / " + volumeMax + " / " + progressValue );
    }

    private void setListeners() {

        final SeekBar seekBarVolume = mView.findViewById( R.id.seekBar_volume );
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.d( LOG_TAG, "seekBarVolume.onProgressChanged() progress/fromUser ->" + progress + " / " + fromUser );
                mLastProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                Log.d( LOG_TAG, "seekBarVolume.onStartTrackingTouch()" );
//                // for debug
//                AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
//                if (audioManager != null) {
//                    int volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
//                }
//                Log.d( LOG_TAG, "seekBarVolume.onStartTrackingTouch() Volume : current->" + volume );
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
                if (audioManager != null) {
                    int volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
                }
                int volumeMax = 0;
                if (audioManager != null) {
                    volumeMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
                }
                int setVolumeValue = (int)( (float)volumeMax * (float)mLastProgress/(float)seekBarVolume.getMax() );
                if (audioManager != null) {
                    audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, setVolumeValue, AudioManager.FLAG_SHOW_UI );
                    //audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, setVolumeValue, AudioManager.FLAG_VIBRATE );
                }

//                Log.d( LOG_TAG, "seekBarVolume.onStopTrackingTouch() Volume : getMax() / lastProgress->" + seekBarVolume.getMax() + " / " + mLastProgress );
//                Log.d( LOG_TAG, "seekBarVolume.onStopTrackingTouch() Volume : current/max->" + volume + " / " + volumeMax );
//                Log.d( LOG_TAG, "seekBarVolume.onStopTrackingTouch() Volume : setVolumeValue->" + setVolumeValue );
            }
        });

        SeekBar seekBarPlayPos = mView.findViewById( R.id.seekBar_play_position );
        seekBarPlayPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d( LOG_TAG, "seekBarPlayPos.onProgressChanged() progress/fromUser ->" + progress + " / " + fromUser );
                if ( fromUser ) {

                    //再生位置を変更されたらスレッドを止めて、再度起動しないといけないかな？？？
                    // --> 止めなくても大丈夫みたい。警告もエラーもなく動作してくれた。

                    int duration = mMediaPlayer.getDuration();
                    int pos = (int)( (float)duration * ( (float) progress / (float)100 ) );
//                    Log.d( LOG_TAG, "seekTo pos(duration)->"+pos+"("+duration+")" );
                    mMediaPlayer.seekTo( pos ); //いきなり飛ばしていいのだろうか？？-->警告、エラー等がないのでOKと思う。
                    mMediaPlayer.start();
                }

                //残り再生時間を描画
                indicateRemainPlayTime();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d( LOG_TAG, "seekBarPlayPos.onStartTrackingTouch()" );
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d( LOG_TAG, "seekBarPlayPos.onStopTrackingTouch()" );
            }
        });

    }

// mediaPlayerを止める）
//        if ( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
//            Log.d( LOG_TAG, "mediaPlayter stop." );
//            mMediaPlayer.pause();
//            mMediaPlayer.stop();
//            mMediaPlayer.reset(); // mediaPlayer is idle after reset.
//            mMediaPlayer.release(); // mediaPlayer end.
//        } else {
    private void startAudio()  {
        //
        //再生処理
        //
        Log.d( LOG_TAG, "mediaPlayter start." );
        mMediaPlayer = MediaPlayer.create( mContext, Uri.parse( mFilePath + mMessage ) );
        if ( mMediaPlayer == null ) {
            Toast.makeText( mContext, "MediaPlayer is null. ["+mMessage+"]", Toast.LENGTH_LONG ).show();
            return;
        }
        mMediaPlayer.setOnErrorListener( this );
        mMediaPlayer .setVolume( (float) 1.0, (float)1.0 ); // 0.0 - 1.0
        mMediaPlayer .setLooping( false );

        indicateRemainPlayTime();

// position seekBar 描画のためのハンドラー（ここから）：TimerTask／Timerクラスを使う事も可能なようだが今回はスレッドはこれだけなのでHandlerで実装。複数スレッドが発生する場合はTimerTask/Timerを使うほうがいいのかな？スレッドセーフ？
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d( LOG_TAG, "runnable" );

                int currentPos = mMediaPlayer.getCurrentPosition();
                int duration = mMediaPlayer.getDuration();
                float progressPos = (float)currentPos / (float)duration;

                SeekBar seekBar = mView.findViewById( R.id.seekBar_play_position );
                seekBar.setProgress( (int)( progressPos * (float)100 ) ); // --> onProgressChanged() with fromUser=false.

                mHandler.postDelayed( this, 1000 /*millisecond*/  ); // millisec 周期でスレッドにPOSTする
            }
        };
        mHandler.post( mRunnable );
// position seekBar 描画のためのハンドラー（ここまで）

//  MediaPlayerクラスでnewした場合にCallする。Instanceを取得（create）する場合はいらない。          mMediaPlayer.prepare();

        mMediaPlayer .start();
    }

    private void indicateRemainPlayTime() {

        int duration = mMediaPlayer.getDuration(); //the duration in milliseconds, if no duration is available (for example, if streaming live content), -1 is returned.
        int current = mMediaPlayer.getCurrentPosition();
        int diff = duration - current;

        float s = (float)diff / (float)1000.0;
        int hour = (int)( s / (float)3600 ); //60 * 60
        int minute = (int)( s / (float)60 );
        int second = (int)( s % (float)60 );

        String strTime;
        if ( hour > 0 ) {
            strTime = "残り再生時間："+hour+"時間"+minute+"分"+second+"秒";
        } else {
            strTime = "残り再生時間："+minute+"分"+second+"秒";
        }
        Log.d( LOG_TAG, strTime );

        TextView progressTime = mView.findViewById( R.id.text_progress_time );
        progressTime.setText( strTime );

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //MediaPlayerからエラー！！（プログラミングのベストプラクティスなんんだって。何をするとエラーが起こるの・・・。）
        Log.d( LOG_TAG, "MediaPlayer Error listener." );
        return false;
    }
}
