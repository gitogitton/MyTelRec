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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class MainActivity extends AppCompatActivity implements FragmentSettingForm.OnFinishSettingFormListener {

    private final String LOG_TAG = getClass().getSimpleName();
    private final String SETTING_FILE_NAME = "setting.csv"; //format : [file format],[auto start],[use bluetooth]
    private SettingData mSettingData = new SettingData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();
        setToolbarItemsListener();

        restoreSettingData();

        if ( savedInstanceState == null ) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentMain main = FragmentMain.newInstance("", "");
            Bundle args = new Bundle();
            args.putSerializable( "SETTING", mSettingData ); //設定ファイルを読み込んだ結果を渡す
            main.setArguments( args );
            fragmentTransaction.add(R.id.top_view, main);
            //fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void OnFinishSettingForm() {
        Log.d( LOG_TAG, "OnFinishSettingFormListener() start." );

        LinearLayout toolbarItem = findViewById( R.id.toolbar_item );
        toolbarItem.setVisibility( View.VISIBLE );

        setToolbar();
        setToolbarItemsListener();
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
        //Log.d( "test", "point.(x,y)->"+"("+point.x+","+point.y+")" );

        int orientation = getResources().getConfiguration().orientation;
        if ( orientation == Configuration.ORIENTATION_PORTRAIT ) { // 縦向き
            marginStart = (int) ( point.x * 0.4);
            //Log.d( LOG_TAG, "向き：縦！！"+marginStart );
        } else {
            marginStart = (int) ( point.x * 0.7);
            //Log.d( LOG_TAG, "向き：横！！"+marginStart );
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
        toolbarItem.setVisibility( View.GONE );

        FragmentSettingForm settingForm = FragmentSettingForm.newInstance( "", "" );

        Bundle args = new Bundle();
        args.putSerializable( "SETTING", mSettingData );
        settingForm.setArguments( args );

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
    private void restoreSettingData() {

        mSettingData = readConfigFromFile();
    }

    private SettingData readConfigFromFile() {

        SettingData setting = new SettingData();

        String fileName = SETTING_FILE_NAME;
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;

        File f = new File( this.getFilesDir()+"/"+fileName );
        if ( ! f.exists() ) { //無ければデフォルト値を設定して終了。
            setting.setFormat( FragmentSelectFormat.FormatList.WAV.getValue() ); // 遠い・・・、enum 、外に出した方がいい・・・
            setting.setAutoStart( false );
            setting.setBluetooth( false );
            return setting;
        }

        try {
            inputStream = this.openFileInput( fileName );
            bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
            String readData = bufferedReader.readLine();
            if ( readData == null ) { // ファイルが壊れてる？？：デフォルト値を設定
                setting.setFormat( FragmentSelectFormat.FormatList.WAV.getValue() ); // 遠い・・・、enum 、外に出した方がいい・・・
                setting.setAutoStart( false );
                setting.setBluetooth( false );
                return setting;
            }

            String[] item = readData.split( "," );
            if ( item.length <= 0 ) { // ファイルが壊れてる？？：デフォルト値を設定
                setting.setFormat( FragmentSelectFormat.FormatList.WAV.getValue() ); // 遠い・・・、enum 、外に出した方がいい・・・
                setting.setAutoStart( false );
                setting.setBluetooth( false );
            } else {
                for ( int i=0; i<item.length; i++ ) {
                    switch ( i ) {
                        case 0 :
                            setting.setFormat( item[i] );
                            break;
                        case 1 :
                            setting.setAutoStart( item[i].equals( "1" ) );
                            break;
                        case 2 :
                            setting.setBluetooth( item[i].equals( "1" ) );
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if ( inputStream != null ) inputStream.close();
                if ( bufferedReader != null ) bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return setting;
    }
}
