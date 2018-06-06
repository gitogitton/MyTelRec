package myrelrec.myappl.jp.mytelrec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;


public class MyBroadcastReceiver extends BroadcastReceiver {

    private final String LOG_TAG = getClass().getSimpleName();

    private Context mContext;

    @Override
    public void onReceive( Context context, Intent intent ) {

        Log.d( LOG_TAG, "onReceive() start.[Action->"+intent.getAction()+"]" );

        mContext = context;

        if ( AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals( intent.getAction() ) ) {
            int status = intent.getIntExtra( AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR );
            if ( status == AudioManager.SCO_AUDIO_STATE_CONNECTED ) {
                //SCO audio channel is established
                Log.d( LOG_TAG, "SCO_AUDIO_STATE_CONNECTED" );
            } else if ( status == AudioManager.SCO_AUDIO_STATE_CONNECTING ){
                //SCO audio channel is being established
                Log.d( LOG_TAG, "SCO_AUDIO_STATE_CONNECTING" );
            } else if ( status == AudioManager.SCO_AUDIO_STATE_DISCONNECTED ) {
                //SCO audio channel is not established
                Log.d( LOG_TAG, "SCO_AUDIO_STATE_DISCONNECTED" );
            }
        }
    }
}
