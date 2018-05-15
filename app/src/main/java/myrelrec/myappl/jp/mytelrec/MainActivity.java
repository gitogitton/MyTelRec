package myrelrec.myappl.jp.mytelrec;

import android.support.v4.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class MainActivity extends AppCompatActivity implements FragmentSettingForm.OnFinishSettingFormListener {

    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( savedInstanceState == null ) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentMain main = FragmentMain.newInstance("", "");
            fragmentTransaction.add(R.id.top_view, main);
            //fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void OnFinishSettingForm() {
        Log.d( LOG_TAG, "OnFinishSettingFormListener() start." );
    }

    //
    //private methods
    //
    private void setNotification( boolean state ) {
        if ( state ) {

            //通知をクリックした時起動するアプリ設定
            Intent notifyIntent = new Intent();
            notifyIntent.setClass( this, MainActivity.class );
            PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, notifyIntent, FLAG_ONE_SHOT );
            //通知を発行するmanager
            NotificationManager nManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
            //通知の設定
            Notification.Builder builder = new Notification.Builder( this );
            builder.setSmallIcon( R.mipmap.ic_launcher_round );
            builder.setContentTitle( "通話記録中" );
            builder.setContentText( "コンテンツテキストです" );
            builder.setContentIntent( pendingIntent );
            if ( nManager != null ) { nManager.notify( 1/*ID*/, builder.build() ); }
            //
            //通話録音を開始する。
            //

        } else {
            NotificationManager nManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
            if ( nManager != null ) { nManager.cancel( 1 ); }

            //
            //通話録音を終了する。
            //
            Log.d( LOG_TAG, "" );
        }
    }
}
