package myrelrec.myappl.jp.mytelrec;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class FragmentMain extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();
    private final String SETTING_FILE_NAME = "setting.csv"; //format : [file format],[auto start],[use bluetooth]
    private final String KEY_FILE_TYPE = "key_fileType";
    private final String REC_FILE_PATH = SettingData.sRecordingFilePath;      //録音ファイルの保存先
    private final int   MENU_TYPE_MAIN = 1;
    private final int   MENU_TYPE_FILE_DEL = 2;
    private final int   MY_PERMISSIONS_REQUEST = 1;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int REQ_SETTING = 0;

    private String mParam1;
    private String mParam2;

    private Context mContext;
    private View mView;
    private SettingData mSettingData = new SettingData();
    private String mIntentFileType;
    private View mSelectedView = null; //選択中（バックグランド色）を解除するため選択行のViewを保存
//複数ファイル選択を可能にする（追加）
    private ArrayList<String> mSelectedFiles = null;
//複数ファイル選択を可能にする（追加ここまで）
    private int mMenuType = MENU_TYPE_MAIN;
    private boolean mUseAddressBook = false;
    private MyBroadcastReceiver mMyReceiver = null;

    public FragmentMain() {
        // Required empty public constructor
    }

    public static FragmentMain newInstance(String param1, String param2) {
        FragmentMain fragment = new FragmentMain();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d( LOG_TAG, "onCreate() start." );
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d( LOG_TAG, "onCreateView() start." );
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_main, container, false);

        setActionBar();
        setHasOptionsMenu( true );

        //機種変更に伴う処理追加（android4.4からandroid7.1.1へ変わった）
        //パーミッションチェックを追加（api23から変わったらしい）
        if ( Build.VERSION.SDK_INT >= 23 ) {
            checkPermissions();
        } else {

        }

        Log.d( LOG_TAG, "path->"+REC_FILE_PATH );

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d( LOG_TAG, "onViewCreated() start." );

        showRecordingFileList();
        restoreSettingData();

        if ( mSettingData.isAutoStart() ) { // autoStart が設定されているときはアプリ起動とともに録音モードに入る。
            startTelRecService();
        }

        AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
        if ( audioManager != null ) {
            if ( !audioManager.isBluetoothScoOn() ) {
                mSettingData.setBluetooth( false );
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        Log.d( LOG_TAG, "onDetach()" );
        super.onDetach();
    }

    @Override
    public void onStart() {
        Log.d( LOG_TAG, "onStart()" );
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d( LOG_TAG, "onResume()" );
        super.onResume();
        refreshListAll();
    }

    @Override
    public void onPause() {
        Log.d( LOG_TAG, "onPause()" );
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d( LOG_TAG, "onStop()" );
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d( LOG_TAG, "onDestroy()" );
        super.onDestroy();
        resetReceiver();
        //setBluetoothOff();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d( LOG_TAG, "onCreateOptionsMenu() start." );

        inflater.inflate( R.menu.menu_main, menu );

        MenuItem item = menu.findItem( R.id.menu_activate );
        SwitchCompat switchCompat = (SwitchCompat) item.getActionView();

        // switchCompat で android:padding～ 属性を指定しても効かない。下記方法で効いた。
        final int paddingRightOffset = 100;  //switch の右にオフセット
        switchCompat.setPadding( switchCompat.getPaddingStart(), switchCompat.getPaddingTop(),
                switchCompat.getPaddingRight() + paddingRightOffset, switchCompat.getPaddingBottom() );
        switchCompat.setText( "録音モード" );
        switchCompat.setTextOff( "Off" );
        switchCompat.setTextOn( "On" );
        switchCompat.setShowText( true ); // <--- これがないと setTextOn(), setTextOff() が効かない・・・。

        switchCompat.setChecked( isRecordServiceAlive() );
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Log.d( LOG_TAG, "onCheckedChanged()" );
                if ( isChecked ) {
                    setReceiver();
                    startTelRecService();
                } else {
                    resetReceiver();
                    stopTelRecService();
                }
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d( LOG_TAG, "onPrepareOptionsMenu() start." + mMenuType );

        super.onPrepareOptionsMenu(menu);

        switch ( mMenuType ) {
            case MENU_TYPE_MAIN :
                menu.findItem( R.id.menu_activate ).setVisible( true );
                menu.findItem( R.id.menu_activate ).setChecked( isRecordServiceAlive() );
                menu.findItem( R.id.menu_setting ).setVisible( true );
                menu.findItem( R.id.menu_file_delete ).setVisible( false );
                menu.findItem( R.id.menu_cancel ).setVisible( false );
                menu.findItem( R.id.menu_user_addressBook ).setVisible( true );
                if (mUseAddressBook) {
                    menu.findItem( R.id.menu_user_addressBook ).setTitle( getString( R.string.menu_not_use_address_book ) );
                } else {
                    menu.findItem( R.id.menu_user_addressBook ).setTitle( getString( R.string.menu_use_address_book ) );
                }
                break;
            case MENU_TYPE_FILE_DEL : //ListViewでファイルを選択中
                menu.findItem( R.id.menu_activate ).setVisible( false );
                menu.findItem( R.id.menu_setting ).setVisible( false );
                menu.findItem( R.id.menu_file_delete ).setVisible( true );
                menu.findItem( R.id.menu_cancel ).setVisible( true );
                menu.findItem( R.id.menu_user_addressBook ).setVisible( false );
                break;
            default :
                menu.findItem( R.id.menu_activate ).setVisible( true );
                menu.findItem( R.id.menu_setting ).setVisible( true );
                menu.findItem( R.id.menu_file_delete ).setVisible( false );
                menu.findItem( R.id.menu_cancel ).setVisible( false );
                menu.findItem( R.id.menu_user_addressBook ).setVisible( true );
                if (mUseAddressBook) {
                    menu.findItem( R.id.menu_user_addressBook ).setTitle( getString( R.string.menu_not_use_address_book ) );
                } else {
                    menu.findItem( R.id.menu_user_addressBook ).setTitle( getString( R.string.menu_use_address_book ) );
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.menu_setting :
                //Log.d( LOG_TAG, "select menu_setting." );
                showSettingForm();
                break;
            case R.id.menu_file_delete : // ファイル削除
                if ( mSelectedView != null ) {
//複数ファイル選択を可能にする（コメント化）
//                    deleteSpecifiedFile();
//                    refreshListWithRemove();
//複数ファイル選択を可能にする（コメント化ここまで）

//複数ファイル選択を可能にする（追加）
                    deleteSpecifiedFiles();
                    refreshListAll();
//複数ファイル選択を可能にする（追加ここまで）
                    mSelectedView = null;
                    mSelectedFiles.clear();
                }
                mMenuType = MENU_TYPE_MAIN;
                ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
                break;
            case R.id.menu_cancel : //ファイル削除 中止
                mMenuType = MENU_TYPE_MAIN;
                mSelectedView.setSelected( false );
                ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
//複数ファイルの選択を可能にする（コメント化）
//                mSelectedView = null;
//複数ファイルの選択を可能にする（コメント化ここまで）

//複数ファイルの選択を可能にする（追加）
                if ( mSelectedView != null ) { //ファイル選択中なら解除する
                    Log.d( LOG_TAG, "selected file->"+mSelectedFiles );
                    refreshListAll();
                    mSelectedView = null;
                    mSelectedFiles.clear();
                    //Log.d( LOG_TAG, "selected file (after clear)->"+mSelectedFiles );
                }
//複数ファイルの選択を可能にする（追加ここまで）

                break;
            case R.id.menu_user_addressBook :
                mUseAddressBook = !mUseAddressBook;
                if (mUseAddressBook) {
                    //Log.d( LOG_TAG, "redrawListWithAddrBook()" );
                    redrawListWithAddrBook();
                } else {
                    //Log.d( LOG_TAG, "refreshListAll()" );
                    refreshListAll();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == REQ_SETTING ) { //設定画面からの戻り
            //Log.d( LOG_TAG, "setting data->" + mSettingData.getFormat() + " / " + mSettingData.isAutoStart() + " / " + mSettingData.isBluetooth() );

            if ( isRecordServiceAlive() ) { //サービスを再起動する
                stopTelRecService();
                startTelRecService();
            }

            boolean useBluetooth = data.getBooleanExtra( "bluetooth", false );
            //Log.d( LOG_TAG, "onActivityResult() userBluetooth->" + useBluetooth );

            if ( useBluetooth ) { //bluetoothデバイスを使用する、が返ってきた場合使えるようにする
                //
                //TelRecServiceにBindしてBluetoothSCOモードOnを通知する。
                // 通知を受けたService側でAudioManagerをBluetoothScoモードをOnにする。
                //Unbind()を忘れない事！！
                //
                setReceiver();
                setBluetoothOn();
            } else {
                //
                //TelRecServiceにBindしてBluetoothSCOモードOffを通知する。
                // 通知を受けたService側でAudioManagerをBluetoothScoモードをOffにする。
                //Unbind()を忘れない事！！
                //
                resetReceiver();
                setBluetoothOff();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d( LOG_TAG, "requestCode="+requestCode );
        int len = permissions.length;
        for ( int i=0; i<len; i++ ) {
            Log.d( LOG_TAG, "permissions["+i+"]="+permissions[i]+" / grantResults["+i+"]="+grantResults[i] );
        }
        if ( requestCode == MY_PERMISSIONS_REQUEST ) {
            if ( ( grantResults.length >= 7 ) &&
                    ( grantResults[0] == PermissionChecker.PERMISSION_GRANTED &&
                            grantResults[1] == PermissionChecker.PERMISSION_GRANTED &&
                            grantResults[2] == PermissionChecker.PERMISSION_GRANTED &&
                            grantResults[3] == PermissionChecker.PERMISSION_GRANTED &&
                            grantResults[4] == PermissionChecker.PERMISSION_GRANTED &&
                            grantResults[5] == PermissionChecker.PERMISSION_GRANTED &&
                            grantResults[6] == PermissionChecker.PERMISSION_GRANTED ) ) {
                Toast.makeText(mContext, "get All permission !", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "could not get some permission !", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mContext, "Un-defined Request Code !", Toast.LENGTH_LONG).show();
        }
    }



    //
    //private methods
    //
    private void checkPermissions() {

        //Apiバージョン２３以上なら！！
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            int permissionRead = ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE );
            int permissionWrite = ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE );
            int permissionRecordAudio = ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.RECORD_AUDIO );
            int permissionReadPhoneState = ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.READ_PHONE_STATE );
            int permissionReadContact = ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.READ_CONTACTS );
            int permissionModifyAudioSetting = ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS );
            int permissionBluetooth = ContextCompat.checkSelfPermission( getActivity(), Manifest.permission.BLUETOOTH );

            if ( permissionRead == PERMISSION_GRANTED && permissionWrite == PERMISSION_GRANTED &&
                    permissionRecordAudio == PERMISSION_GRANTED && permissionReadPhoneState == PERMISSION_GRANTED &&
                    permissionReadContact == PERMISSION_GRANTED && permissionModifyAudioSetting == PERMISSION_GRANTED &&
                    permissionBluetooth == PERMISSION_GRANTED ) {
//                showListOfDirectory( INIT_DIR );
            }
            else {
                reqPermissions();
//                Toast.makeText( getApplicationContext(), "パーミッションエラー", Toast.LENGTH_LONG ).show();
            }
        }
    }

    private void reqPermissions() {
        boolean should = ActivityCompat.shouldShowRequestPermissionRationale(  getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE  );
        // パーミッションが必要である理由をユーザに説明すべきときはこちら！！
        if ( should ) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //説明のためのUIを自分で実装しなきゃならない！！
            Log.d( LOG_TAG, "shouldShowRequestPermissionRationale() == true" );

// 権限チェックした結果、持っていない場合はダイアログを出す
            new AlertDialog.Builder(getContext())
                    .setTitle("パーミッションの追加理由")
                    .setMessage("このアプリでは次に続く複数の権限が必要になります。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(
                                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.RECORD_AUDIO,
                                            Manifest.permission.READ_PHONE_STATE,
                                            Manifest.permission.READ_CONTACTS,
                                            Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                            Manifest.permission.BLUETOOTH },
                                    MY_PERMISSIONS_REQUEST );
                        }
                    })
                    .create()
                    .show();

        } else {    //ユーザに説明が必要ない場合はこちら

            requestPermissions(
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.MODIFY_AUDIO_SETTINGS,
                            Manifest.permission.BLUETOOTH  },
                    MY_PERMISSIONS_REQUEST);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    private void redrawListWithAddrBook() {
        //
        //電話番号がわかっているものについて電話帳から名前を取得し、リストを再描画する。
        //

        ArrayList<ItemData> arrayList = new ArrayList<ItemData>();
        Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null );
        if ( cursor != null ) {
            String phoneNumber;
            while( cursor.moveToNext() ) {
                phoneNumber = cursor.getString( cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DATA1 ) );
                phoneNumber = phoneNumber.replace( "-", "" );
                ItemData itemData = new ItemData();
                itemData.setPhoneNumber( phoneNumber );
                itemData.setName( cursor.getString( cursor.getColumnIndex( ContactsContract.PhoneLookup.DISPLAY_NAME ) ) );
                arrayList.add( itemData );
            }

            cursor.close();

            //
            //比較
            //今は着信しか電話番号を取ってないので着信のみ対象とする。
            //
            //ListViewからデータ取得
            ListView listView = mView.findViewById( R.id.list_recordFileList );
            RecordingFileListAdapter adapter = (RecordingFileListAdapter) listView.getAdapter();
            int listCount = adapter.getCount();
            if ( listCount <= 0 ) {
                Log.d( LOG_TAG, "ListView : no data ["+listCount+"]" );
                return;
            }
            //Log.d( LOG_TAG, "ListView : List Count-> "+listCount );

            for ( int i=0; i<listCount; i++ ) {
                ItemData item = adapter.getItem( i );
                //Log.d( LOG_TAG, "item.getPhoneNumber()->"+item.getPhoneNumber() );
                //電話番号抽出
                if ( item == null ) { continue; }
                int startPos  = item.getPhoneNumber().lastIndexOf( "_" ) + 1;
                int lastPos = item.getPhoneNumber().lastIndexOf( "." );
                //Log.d( LOG_TAG, "startPos - lastPos -> "+startPos+" - "+lastPos );
                if ( startPos < 0 || lastPos < 0 || startPos >= lastPos ) { continue; }

                String itemPhoneNumber = item.getPhoneNumber().substring( startPos, lastPos );
                //Log.d( LOG_TAG, "itemPhoneNumber->"+itemPhoneNumber );

                for ( int j=0; j<arrayList.size(); j++ ) {
                    //電話番後が同じ場合
                    //Log.d( LOG_TAG, "arrayList.getPhoneNumber()->"+arrayList.get( j ).getPhoneNumber() );
                    if ( itemPhoneNumber.equals( arrayList.get( j ).getPhoneNumber() ) ) {
                        item.setName( arrayList.get( j ).getName() );
                        item.setDisplayString( arrayList.get( j ).getName() );
                        //Log.d( LOG_TAG, "set name : "+arrayList.get( j ).getName()+" (Number:"+itemPhoneNumber+")" );
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }



//確認作業(ここから)
//            Log.d( LOG_TAG, "cursor->"+ cursor );
//            Log.d( LOG_TAG, "cursor.getCount()->"+cursor.getCount() );
//            cursor.moveToFirst();
//            String[] columnNames = cursor.getColumnNames();
//            Log.d( LOG_TAG, "columnNames.length->"+columnNames.length );
//
//            for ( int i=0; i<columnNames.length; i++ ) {
//                Log.d( LOG_TAG, "columnName->"+columnNames[i] );
//            }
//
//            Log.d( LOG_TAG, "==============================" );
//            Log.d( LOG_TAG, "display_name->"+ cursor.getString( cursor.getColumnIndex( ContactsContract.PhoneLookup.DISPLAY_NAME ) ) );
//            Log.d( LOG_TAG, "contact_id->"+ cursor.getString( cursor.getColumnIndex( "contact_id" ) ) );
//            Log.d( LOG_TAG, "has_phone_number->"+ cursor.getString( cursor.getColumnIndex( ContactsContract.PhoneLookup.HAS_PHONE_NUMBER ) ) );
//            Log.d( LOG_TAG, "PHONETIC_NAME->"+ cursor.getString( cursor.getColumnIndex( ContactsContract.PhoneLookup.PHONETIC_NAME ) ) );
//            Log.d( LOG_TAG, "_id->"+ cursor.getString( cursor.getColumnIndex( ContactsContract.PhoneLookup._ID ) ) );
//
//            //ContactsContract.PhoneLookup でアクセスしても電話番号にアクセス出来ないので ContactsContract.CommonDataKinds.Phone でアクセスする。
//            //電話番号は複数登録出来る。～.Phone.DATA で複数取れる（？やってないので「？」）。とりあえず1件だけで「～.Phone.DATA1」としている。
//            //ちなみに、E-mailも複数登録出来る。ContactsContract.CommonDataKinds.Email というものがある。ここからアクセスするのだろう。
//            //更にPhoneとMailを合わせて ContactsContract.CommonDataKinds.Contactables なるものがあるようだ。
//            Log.d( LOG_TAG, "data1->"+ cursor.getString( cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DATA1 ) ) );
//
//            cursor.close();
//確認作業(ここまで)

        } //if ( cursor!=null)
    }

    private void setReceiver() {
        IntentFilter intentFilter = new IntentFilter( AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED );  //Broadcast intent action indicating that the call state on the device has changed.
        mMyReceiver = new MyBroadcastReceiver();
        Intent returnedIntent = mContext.registerReceiver( mMyReceiver, intentFilter );
        if ( returnedIntent == null ) {
            Log.d( LOG_TAG, "returnedIntent is null" );
        }
    }

    private void resetReceiver() {
        if ( mMyReceiver != null ) {
            mContext.unregisterReceiver( mMyReceiver );
            mMyReceiver = null;
        }
    }

    private void setBluetoothOn() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
        if ( audioManager != null ) {
            if (!audioManager.isBluetoothScoOn()) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.setBluetoothScoOn(true);
                audioManager.startBluetoothSco(); //scoモードに即入られるわけでなく、モード変更完了を待つ。
                Toast.makeText(mContext, "SCO mode [on]", Toast.LENGTH_LONG).show();
                //Log.d(LOG_TAG, "SCO mode [on]");
            } else {
                Toast.makeText(mContext, "already SCO mode is already ON. [on]", Toast.LENGTH_LONG).show();
                //Log.d(LOG_TAG, "already SCO mode is already ON. [on]");
            }
        } else {
            Toast.makeText(mContext, "AudioManager Get ERROR. [sco on]", Toast.LENGTH_LONG).show();
            //Log.d( LOG_TAG, "AudioManager Get ERROR. [sco on]" );
        }
    }

    private void setBluetoothOff() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
        if ( audioManager != null ) {
            if ( audioManager.isBluetoothScoOn()) {
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn( false );
                audioManager.setMode( AudioManager.MODE_NORMAL );
                Toast.makeText(mContext, "SCO mode [off]", Toast.LENGTH_LONG).show();
                //Log.d( LOG_TAG, "SCO mode [off]" );
            } else {
                Toast.makeText( mContext, "SCO mode is already OFF. [off]", Toast.LENGTH_LONG ).show();
                //Log.d( LOG_TAG, "SCO mode is already OFF. [off]" );
            }
        } else {
            Toast.makeText( mContext, "AudioManager Get ERROR. [sco off]", Toast.LENGTH_LONG ).show();
            //Log.d( LOG_TAG, "AudioManager Get ERROR. [sco off]" );
        }
    }

    private void refreshListAll() {
        ArrayList<ItemData> arrayList = getFileList();
        RecordingFileListAdapter adapter = new RecordingFileListAdapter( mContext, R.layout.file_list_item, arrayList );

        ListView listView = mView.findViewById( R.id.list_recordFileList );
        TextView emptyTextView = mView.findViewById( R.id.emptyTextView );

        listView.setEmptyView( emptyTextView );
        listView.setAdapter( adapter );
    }

    private void refreshListWithRemove() {
//複数ファイル選択に対応する。（コメント化）
//        TextView textView = mSelectedView.findViewById( R.id.text_phoneNumber );
//        String fileName = textView.getText().toString();
//
//        ListView listView = mView.findViewById( R.id.list_recordFileList );
//        RecordingFileListAdapter adapter = (RecordingFileListAdapter) listView.getAdapter();
//        for ( int i=0; i<adapter.getCount(); i++ ) {
//            ItemData itemData = adapter.getItem( i );
//            if ( itemData != null && fileName.equals( itemData.getPhoneNumber() ) ) {
//                adapter.remove( itemData );
//                break;
//            }
//        }
//        adapter.notifyDataSetChanged();
//複数ファイル選択に対応する。（コメント化ここまで）

//複数ファイル選択に対応する。（追加）
        if ( mSelectedFiles.size() <= 0 ) {
            Toast.makeText( mContext, "No file selected !", Toast.LENGTH_LONG ).show();
            return;
        }

        ListView listView = mView.findViewById( R.id.list_recordFileList );
        RecordingFileListAdapter adapter = (RecordingFileListAdapter) listView.getAdapter();
        for ( String selectedFile : mSelectedFiles ) {
            for ( int i=0; i<adapter.getCount(); i++ ) {
                ItemData itemData = adapter.getItem( i );
                if (itemData != null && selectedFile.equals(itemData.getPhoneNumber())) {
                    adapter.remove(itemData);
                    break;
                }
            }
        }
        adapter.notifyDataSetChanged();
//複数ファイル選択に対応する。（追加ここまで）
    }

    private void deleteSpecifiedFile() {
        //Log.d( LOG_TAG, "deleteSpecifiedFile() Go !!" );

        TextView textFileName = mSelectedView.findViewById( R.id.text_phoneNumber );
        String fileName = REC_FILE_PATH + "/" + textFileName.getText().toString();
        //Log.d( LOG_TAG, "deleted file -> " + fileName );

        File file = new File( fileName );
        if ( file.exists() ) {
            boolean result = file.delete();
            if ( !result ) {
                Toast.makeText( mContext, "Delete File Error !! [" + fileName + "]", Toast.LENGTH_LONG ).show();
            }
        } else {
            Toast.makeText( mContext, "File doesn't exist. [" + fileName + "]", Toast.LENGTH_LONG ).show();
        }

        mSelectedView.setSelected( false );
    }

//複数ファイル選択を可能にする。（追加）
    private void deleteSpecifiedFiles() {

        if ( mSelectedFiles.size() <= 0 ) {
            Toast.makeText( mContext, "file count is 0.", Toast.LENGTH_LONG ).show();
            return;
        }

        for ( String selectedFile : mSelectedFiles ) {

            String fileName = REC_FILE_PATH + "/" + selectedFile;
            //Log.d( LOG_TAG, "deleted file -> " + fileName );

            File file = new File( fileName );
            if ( file.exists() ) {
                boolean result = file.delete();
                if ( !result ) {
                    Toast.makeText(mContext, "Delete File Error !! [" + fileName + "]", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mContext, "File doesn't exist. [" + fileName + "]", Toast.LENGTH_LONG).show();
            }
        }
    }
//複数ファイル選択を可能にする。（追加ここまで）

    private boolean isRecordServiceAlive() {
        //（確か）API 26から廃止
        ActivityManager manager = (ActivityManager) mContext.getSystemService( Context.ACTIVITY_SERVICE );

        if ( manager == null ) return false;

        for ( ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE ) ) {
            if ( TelRecService.class.getName().equals( serviceInfo.service.getClassName() ) ) {
                return true;
            }
        }
        return false;
    }

    private void startTelRecService() {
        //
        //サービス開始処理
        //　 開始出来た場合や既にある場合はその情報が返ってくる。
        //   起動失敗すると null で返ってくる。（と以下の英文を読んだからサービスが既に存在するか否かのチェックはいらない！！！、、と思った。）
        //
        // If the service is being started or is already running, the ComponentName of the actual service that was started is returned;
        // else if the service does not exist null is returned. (wrote by google site)
        //
        if ( ! isRecordServiceAlive() ) {
            Intent intent = new Intent( mContext, TelRecService.class );
            mIntentFileType = mSettingData.getFormat();
            intent.putExtra( KEY_FILE_TYPE, mIntentFileType);
            ComponentName componentName = mContext.startService( intent );
            if ( componentName == null ) {
                //Log.d( LOG_TAG, "Service doesn't exist." );
                Toast.makeText( mContext, "Service doesn't exist.", Toast.LENGTH_LONG ).show();
                return;
//        } else {
//            Log.d( LOG_TAG, "componentName.getClassName()->"+componentName.getClassName() );
//            Log.d( LOG_TAG, "componentName.toString()->"+componentName.toString() );
            }

            Toast.makeText( mContext, "Service is started.", Toast.LENGTH_LONG ).show();
            //Log.d( LOG_TAG, "Service is started." );
        } else {
            Toast.makeText( mContext, "Service is already started.", Toast.LENGTH_LONG ).show();
        }
    }

    private void stopTelRecService() {

        //
        //サービス終了処理
        //
        if ( isRecordServiceAlive() ) {
            Intent intent = new Intent( mContext, TelRecService.class );
            intent.putExtra(KEY_FILE_TYPE, mIntentFileType);
            // If there is a service matching the given Intent that is already running,
            // then it is stopped and true is returned;
            // else false is returned.  (wrote by google site)
            boolean result = mContext.stopService( intent );
            if ( !result ) {
                Toast.makeText( mContext, "Service is not found.", Toast.LENGTH_LONG ).show();
                //Log.d( LOG_TAG, "Service is not found." );
                return;
            }

            Toast.makeText( mContext, "Service is stopped.", Toast.LENGTH_LONG ).show();
            //Log.d( LOG_TAG, "Service is stopped." );
        } else {
            Toast.makeText( mContext, "Service is already stopped.", Toast.LENGTH_LONG ).show();
        }
    }

    private void showRecordingFileList() {

        ArrayList<ItemData> arrayList = getFileList();
        RecordingFileListAdapter adapter = new RecordingFileListAdapter( mContext, R.layout.file_list_item, arrayList );

        ListView listView = mView.findViewById( R.id.list_recordFileList );
        TextView emptyTextView = mView.findViewById( R.id.emptyTextView );

        listView.setEmptyView( emptyTextView );
        listView.setAdapter( adapter );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d( LOG_TAG, "select->" + position );
                ItemData item = (ItemData) parent.getItemAtPosition( position );
                //Log.d( LOG_TAG, "item->"+item.getDate()+" / "+item.getPhoneNumber() );
                //Log.d( LOG_TAG, "isSelected()->" + view.isSelected() ); // setSelected(true) してからタップした場合の isSelected() を確認。isSelected()==falseでした。

                //Log.d( LOG_TAG, "mSelectedItem->" + mSelectedView );
                if ( mSelectedView == null ) {
                    DialogPlayVoice dialogPlayVoice = new DialogPlayVoice();  // --> new よりも newInstance() の方がいい？ 自作SFTPアプリではそうしてるんだけど・・。やり方が安定しないなぁ。まぁ、ケースバイケースで拡張性の要不要を熟慮すればいいんだけど・・・。
                    Bundle args = new Bundle();
                    args.putString( "target_file", item.getPhoneNumber() );
                    dialogPlayVoice.setArguments( args );
                    FragmentManager fm = getFragmentManager();
                    if ( fm != null ) {
                        dialogPlayVoice.show(fm, "PlayVoice");
                    } else {
                        Toast.makeText( mContext, "getFragmentManager() returned null.", Toast.LENGTH_LONG  ).show();
                    }
                } else {
//複数選択できるようにする。（コメント化）
//                    mSelectedView = null;
//                    mMenuType = MENU_TYPE_MAIN;
//                    ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
//複数選択できるようにする。（コメント化ここまで）

//複数選択できるようにする（追加）
                    //mSelectedView != null -> ファイル選択中：選択状態にする
                    if ( mSelectedView != null ) {
                        boolean findFlag = false;
                        for ( String selectedFile : mSelectedFiles ) {
                            if ( selectedFile.equals( item.getPhoneNumber() ) ) { //既にリストにあるものはArrayから除外し、バックグランド色も戻す。
                                findFlag = true;
                                break;
                            }
                        }
                        if ( findFlag ) { //削除対象として選択されているので解除する。
                            view.setBackgroundColor( Color.WHITE  );
                            mSelectedFiles.remove( item.getPhoneNumber() );
                        } else { //削除対象として追加する。
                            view.setBackgroundColor( Color.GREEN  );
                            mSelectedFiles.add( item.getPhoneNumber() );
                        }
                    }
//複数選択できるようにする（追加ここまで）

                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d( LOG_TAG, "ListView longClick pos->" + position );
//複数選択できるようにする。（コメント化）
//                view.setSelected( !( view.isSelected() ) ); //true：バックグラウンド色が drawable/selector_list_item.xml で定義した色に変わる。
//                if ( view.isSelected() ) {
//                    mSelectedView = view;
//                }
//
//                //メニュー切り替え（削除を表示）
//                mMenuType = MENU_TYPE_FILE_DEL;
//                ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
//複数選択できるようにする。（コメント化ここまで）

//複数選択できるようにする。（追加）

                ItemData itemData = (ItemData) parent.getItemAtPosition( position );

                if ( mSelectedView == null ) {

                    view.setBackgroundColor( Color.GREEN );
                    mSelectedView = view;

                    if ( mSelectedFiles != null ) {
                        mSelectedFiles.clear();
                    }
                    mSelectedFiles = new ArrayList<String>();
                    mSelectedFiles.add( itemData.getPhoneNumber() );

                    //メニュー切り替え（削除を表示）
                    mMenuType = MENU_TYPE_FILE_DEL;
                    ( (AppCompatActivity)mContext ).invalidateOptionsMenu();

                }

//複数選択できるようにする。（追加ここまで）

                return true; //onItemClick()には渡さない。長押しされた場合は選択中とする。
            }
        });
    }

    private ArrayList<ItemData> getFileList() {
        ArrayList<ItemData> arrayList = new ArrayList<>();

        File fileList = new File( REC_FILE_PATH + "/" );
        String[] dirList = fileList.list();
        if ( dirList == null || dirList.length <= 0 ) {
            return arrayList;
        }

//        Collections.sort( Arrays.asList( dirList ) );  //降順ソート
//        Collections.reverse( Arrays.asList( dirList ) ); //昇順ソート
        Collections.sort( Arrays.asList( dirList ), new FileNameComparator() ); //直近から古いものへとソート

        String prevDate = "";
        for ( String item : dirList ) {
//            Log.d( LOG_TAG, "file->" + item );

            String date = item.substring( 0, 8 ); // ファイル名からyyyymmdd を取得

            if ( !prevDate.equals( date ) ) {
                ItemData itemData = new ItemData();
                itemData.setDate( date.substring( 0, 4 )+"/"+date.substring( 4, 6 )+"/"+date.substring( 6, 8 ) );
                itemData.setPhoneNumber( "" );
                arrayList.add( itemData );
            }

            ItemData itemData = new ItemData();
            itemData.setDate( "" );
            itemData.setPhoneNumber( item ); //setPhoneNumber()といいつつ、実はPhoneNumberを含むファイル名文字列なんです。(発信では番号取ってないので・・)
            itemData.setName( "" ); //アドレス帳での登録名（このアプリではここまでしない！！）アドレス帳参照は「名前で表示」メニューで実装している。
            if ( mUseAddressBook == true ) {
                //ListViewに表示するデータにアドレス帳での登録名を使用する。（itemData.getName()）
                //でも今はしない。不便を感じてから考える。アドレス帳参照は「名前で表示」メニューで実装している。（着信だけ）
                //itemData.setDisplayString( itemData.getName() )
                itemData.setDisplayString( item );
            } else {
                //ListViewに表示するデータにアドレス帳を使用しない。
                //（ファイル名をそのまま使用）
                itemData.setDisplayString( item );
            }
            arrayList.add( itemData );

            prevDate = date;
        }

        return arrayList;
    }

    private void setActionBar() {
        ActionBar actionBar = ( (AppCompatActivity)mContext ).getSupportActionBar();
        if ( actionBar != null ) {
            actionBar.setTitle(R.string.app_name);
        }
    }

    private void showSettingForm() {

        FragmentSettingForm settingForm = FragmentSettingForm.newInstance( this, REQ_SETTING );

        Bundle args = new Bundle();
        args.putSerializable( "SETTING", mSettingData );
        settingForm.setArguments( args );

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = null;
        if (fragmentManager != null) {
            fragmentTransaction = fragmentManager.beginTransaction();
        }
        if (fragmentTransaction != null) {
            fragmentTransaction.replace( R.id.top_view, settingForm );
            fragmentTransaction.addToBackStack( null );
            fragmentTransaction.commit();
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

        File f = new File( mContext.getFilesDir()+"/"+fileName );
        if ( ! f.exists() ) { //無ければデフォルト値を設定して終了。
            setting.setFormat( EnumFormatList.WAV.getValue() ); // 遠い・・・、enum 、外に出した方がいい・・・
            setting.setAutoStart( false );
            setting.setBluetooth( false );
            return setting;
        }

        try {
            inputStream = mContext.openFileInput( fileName );
            bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
            String readData = bufferedReader.readLine();
            if ( readData == null ) { // ファイルが壊れてる？？：デフォルト値を設定
                setting.setFormat( EnumFormatList.WAV.getValue() ); // 遠い・・・、enum 、外に出した方がいい・・・
                setting.setAutoStart( false );
                setting.setBluetooth( false );
                return setting;
            }

            String[] item = readData.split( "," );
            if ( item.length <= 0 ) { // ファイルが壊れてる？？：デフォルト値を設定
                setting.setFormat( EnumFormatList.WAV.getValue() ); // 遠い・・・、enum 、外に出した方がいい・・・
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
