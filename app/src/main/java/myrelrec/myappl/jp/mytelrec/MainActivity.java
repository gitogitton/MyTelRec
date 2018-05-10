package myrelrec.myappl.jp.mytelrec;

import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class MainActivity extends AppCompatActivity
        implements FragmentMain.OnFragmentInteractionListener, FragmentSettingForm.OnFinishSettingFormListener {

    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();
//        adjustToolbarItemPos();
        setToolbarItemsListener();

        if ( savedInstanceState == null ) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentMain main = FragmentMain.newInstance("", "");
            fragmentTransaction.add(R.id.top_view, main);
//            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d( LOG_TAG, "onFragmentInteraction() start." );
    }

    @Override
    public void OnFinishSettingFormListener() {
        Log.d( LOG_TAG, "OnFinishSettingFormListener() start." );

        setToolbar();

        LinearLayout toolbarItem = findViewById( R.id.toolbar_item );
        toolbarItem.setVisibility( View.VISIBLE );
    }

    //
    //private methods
    //
    private void setToolbarItemsListener() {
        Toolbar toolbar = findViewById( R.id.my_toolbar );
        SwitchCompat switchCompat = toolbar.findViewById( R.id.switch_on_off );
        ImageView imageView = toolbar.findViewById( R.id.img_setting );

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d( LOG_TAG, "Switch changed to " + isChecked );
                setNotification( isChecked );
            }
        });
        imageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d( LOG_TAG, "click setting !" );
                showSettingForm();
            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById( R.id.my_toolbar );
        setSupportActionBar( toolbar );
        toolbar.setTitle( getString( R.string.app_name ) );

        adjustToolbarItemPos();
    }

    private void adjustToolbarItemPos() {

        // Toolbar の中のViewを右よりにしたいがレイアウトファイルでする事が出来なかった。（出来ないのだろうか？）
        // タイトルバーのカスタマイズとして自分でレイアウト定義してする方法もあるようだが面倒なので
        // 縦／横でマージンを動的に変更することにした・・・・。本当にレイアウトファイルで出来たら嬉しいんだけど、、どうなんだろう？

        int marginStart;

        Point point = new Point();
        this.getWindowManager().getDefaultDisplay().getRealSize( point );
        Log.d( "test", "point.(x,y)->"+"("+point.x+","+point.y+")" );

        int orientation = getResources().getConfiguration().orientation;
        if ( orientation == Configuration.ORIENTATION_PORTRAIT ) { // 縦向き
            marginStart = (int) ( point.x * 0.4);
            Log.d( LOG_TAG, "向き：縦！！"+marginStart );
        } else {
            marginStart = (int) ( point.x * 0.7);
            Log.d( LOG_TAG, "向き：横！！"+marginStart );
        }

        Toolbar toolbar = findViewById( R.id.my_toolbar );
        LinearLayout toolbarItems = toolbar.findViewById( R.id.toolbar_item );
        ViewGroup.LayoutParams layoutParams = toolbarItems.getLayoutParams();
        ( (Toolbar.LayoutParams)layoutParams ).setMarginStart( marginStart ); // １つ上のViewでCastしないとException。１つ上で内包してるって解釈かな？

// 起動時は値が０、、、まだ出来てないって事だろうな・・・
//        toolbarItems = findViewById( R.id.toolbar_item );
//
//        int[] pos = new int[2];
//        toolbarItems.getLocationOnScreen( pos );
//        toolbar = (Toolbar)( toolbarItems.getParent() );
//        int toolbarWidth = toolbar.getWidth();
//
//        Log.d( "test", "toolbar width->"+toolbarWidth );
//        Log.d( "test", "pos->"+pos[0]+" / "+pos[1] );
//        Log.d( "test", "left/top/right/bottom->"+toolbarItem.getLeft()+" / "+toolbarItem.getTop()+" / "+toolbarItem.getRight()+" / "+toolbarItem.getBottom());
//
//        int movePos = (int)( toolbarWidth * ratio );
//        toolbarItem.setLeft( movePos );
//        toolbarItem.setRight( movePos + toolbarWidth );
//
//        Log.d( "test", "movePos->"+movePos );
//        Log.d( "test", "left/top/right/bottom->"+toolbarItem.getLeft()+" / "+toolbarItem.getTop()+" / "+toolbarItem.getRight()+" / "+toolbarItem.getBottom());
    }

    private void showSettingForm() {

        LinearLayout toolbarItem = findViewById( R.id.toolbar_item );
        toolbarItem.setVisibility( View.INVISIBLE );

        FragmentSettingForm settingForm = FragmentSettingForm.newInstance( "", "" );
//        FragmentSettingForm settingForm = new FragmentSettingForm();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.top_view, settingForm );
        fragmentTransaction.addToBackStack( null );
        fragmentTransaction.commit();
    }

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
