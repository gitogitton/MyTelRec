package myrelrec.myappl.jp.mytelrec;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

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

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        mView = inflater.inflate( R.layout.dialog_play_voice, null );
        builder.setView( mView );

        builder.setTitle( mDialogTitle );
        builder.setMessage( mMessage );

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d( LOG_TAG, "click positive button" );
            }
        });

        builder.setNegativeButton("NG", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d( LOG_TAG, "click negative button" );
            }
        });

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
    public void onDetach() {
        Log.d( LOG_TAG, "onDetach()" );
        super.onDetach();
        mMediaPlayer.stop();
        mMediaPlayer.release();
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
        int volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        int volumeMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
        int progressValue = (int)( ( (float)volume/(float)volumeMax) * (float)100.0 );
        seekBarVolume.setProgress( progressValue );
        //Log.d( LOG_TAG, "initSeekBar() currentVol/maxVol/progressVal->" + volume + " / " + volumeMax + " / " + progressValue );
    }

    private void setListeners() {

        SeekBar seekBarPlayPos = mView.findViewById( R.id.seekBar_play_position );
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
                // for debug
                AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
                int volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
//                Log.d( LOG_TAG, "seekBarVolume.onStartTrackingTouch() Volume : current->" + volume );
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
                int volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
                int volumeMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
                int setVolumeValue = (int)( (float)volumeMax * (float)mLastProgress/(float)seekBarVolume.getMax() );
                audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, setVolumeValue, AudioManager.FLAG_SHOW_UI ); // デバッグ終わると変更 : third argument -> AudioManager.FLAG_VIBRATE

//                Log.d( LOG_TAG, "seekBarVolume.onStopTrackingTouch() Volume : getMax() / lastProgress->" + seekBarVolume.getMax() + " / " + mLastProgress );
//                Log.d( LOG_TAG, "seekBarVolume.onStopTrackingTouch() Volume : current/max->" + volume + " / " + volumeMax );
//                Log.d( LOG_TAG, "seekBarVolume.onStopTrackingTouch() Volume : setVolumeValue->" + setVolumeValue );
            }
        });

        seekBarPlayPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d( LOG_TAG, "seekBarPlayPos.onProgressChanged() progress/fromUser ->" + progress + " / " + fromUser );
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
        mMediaPlayer.setOnErrorListener( this );
        mMediaPlayer .setVolume( (float) 1.0, (float)1.0 ); // 0.0 - 1.0
        mMediaPlayer .setLooping( false );
//  MediaPlayerクラスでnewした場合にCallする。Instanceを取得（create）する場合はいらない。          mMediaPlayer.prepare();
        mMediaPlayer .start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //MediaPlayerからエラー！！（プログラミングのベストプラクティスなんんだって。何をするとエラーが起こるの・・・。）
        Log.d( LOG_TAG, "MediaPlayer Error listener." );
        return false;
    }
}
