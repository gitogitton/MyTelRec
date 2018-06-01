package myrelrec.myappl.jp.mytelrec;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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

public class FragmentMain extends Fragment {

    public static boolean mModeSco = false;

    private final String LOG_TAG = getClass().getSimpleName();
    private final String SETTING_FILE_NAME = "setting.csv"; //format : [file format],[auto start],[use bluetooth]
    private final String KEY_FILE_TYPE = "key_fileType";
    private final String REC_FILE_PATH = "/storage/sdcard0/telrec";      //録音ファイルの保存先
    private final int   MENU_TYPE_MAIN = 1;
    private final int   MENU_TYPE_FILE_DEL = 2;

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
    private int mMenuType = MENU_TYPE_MAIN;

//とりあえずコメント
//    private MyBroadcastReceiver mMyReceiver = new MyBroadcastReceiver();

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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
//        Log.d( LOG_TAG, "MyBoadcastReceiver is un-registered ! (onDetach)" );
//        LocalBroadcastManager.getInstance( mContext ).unregisterReceiver( mMyReceiver );
        super.onDetach();
    }

    @Override
    public void onStart() {
        Log.d( LOG_TAG, "onStart() start." );
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d( LOG_TAG, "onResume() start." );
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d( LOG_TAG, "onPause() start." );
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d( LOG_TAG, "onStop() start." );
        super.onStop();
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
                Log.d( LOG_TAG, "onCheckedChanged()" );
                if ( isChecked ) {
                    startTelRecService();
                } else {
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
                break;
            case MENU_TYPE_FILE_DEL : //ListViewでファイルを選択中
                menu.findItem( R.id.menu_activate ).setVisible( false );
                menu.findItem( R.id.menu_setting ).setVisible( false );
                menu.findItem( R.id.menu_file_delete ).setVisible( true );
                menu.findItem( R.id.menu_cancel ).setVisible( true );
                break;
            default :
                menu.findItem( R.id.menu_activate ).setVisible( true );
                menu.findItem( R.id.menu_setting ).setVisible( true );
                menu.findItem( R.id.menu_file_delete ).setVisible( false );
                menu.findItem( R.id.menu_cancel ).setVisible( false );
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.menu_setting :
                Log.d( LOG_TAG, "select menu_setting." );
                showSettingForm();
                break;
            case R.id.menu_file_delete : // ファイル削除
                if ( mSelectedView != null ) {
                    deleteSpecifiedFile();
                    refreshList();
                    mSelectedView = null;
                }
                mMenuType = MENU_TYPE_MAIN;
                ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
//                getActivity().invalidateOptionsMenu();
                break;
            case R.id.menu_cancel : //ファイル削除 中止
                mMenuType = MENU_TYPE_MAIN;
                mSelectedView.setSelected( false );
                ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
//                getActivity().invalidateOptionsMenu();
                mSelectedView = null;
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
//        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == REQ_SETTING ) { //設定画面からの戻り
            Log.d( LOG_TAG, "setting data->" + mSettingData.getFormat() + " / " + mSettingData.isAutoStart() + " / " + mSettingData.isBluetooth() );

            if ( isRecordServiceAlive() ) { //サービスを再起動する
                stopTelRecService();
                startTelRecService();
            }

            boolean useBluetooth = data.getBooleanExtra( "bluetooth", false );
            Log.d( LOG_TAG, "onActivityResult() userBluetooth->" + useBluetooth );
            if ( useBluetooth ) {

                AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
                if ( audioManager != null ) {
                    audioManager.setMode( AudioManager.MODE_IN_CALL );
                    audioManager.setBluetoothScoOn( true ); //scoモードに即入られるわけでなく、モード変更完了を待つ。
                    mModeSco = true;
                    Toast.makeText( mContext, "SCO mode [on]", Toast.LENGTH_LONG ).show();
                } else {
                    Toast.makeText( mContext, "Can't get AudioManager.[on]", Toast.LENGTH_LONG ).show();
                }

            } else {

                AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE );
                if ( audioManager != null ) {
                    audioManager.stopBluetoothSco();
                    audioManager.setMode( AudioManager.MODE_NORMAL );
                    Toast.makeText( mContext, "SCO mode [off]", Toast.LENGTH_LONG ).show();
                    mModeSco = false;
                } else {
                    Toast.makeText( mContext, "Can't get AudioManager.[off]", Toast.LENGTH_LONG ).show();
                }

            }
        }
    }

    //
    //private methods
    //
    private void refreshList() {
        TextView textView = mSelectedView.findViewById( R.id.text_phoneNumber );
        String fileName = textView.getText().toString();

        ListView listView = mView.findViewById( R.id.list_recordFileList );
        RecordingFileListAdapter adapter = (RecordingFileListAdapter) listView.getAdapter();
        for ( int i=0; i<adapter.getCount(); i++ ) {
            ItemData itemData = adapter.getItem( i );
            if ( itemData != null && fileName.equals( itemData.getPhoneNumber() ) ) {
                adapter.remove( itemData );
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteSpecifiedFile() {
        Log.d( LOG_TAG, "deleteSpecifiedFile() Go !!" );

        TextView textFileName = mSelectedView.findViewById( R.id.text_phoneNumber );
        String fileName = REC_FILE_PATH + "/" + textFileName.getText().toString();
        Log.d( LOG_TAG, "deleted file -> " + fileName );

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

// とりあえずコメント。リスナー置き換え＆キャンセルから始めよう・・
//        //BroadcastReceiver
//        IntentFilter intentFilter = new IntentFilter( TelephonyManager.ACTION_PHONE_STATE_CHANGED );  //Broadcast intent action indicating that the call state on the device has changed.
//        intentFilter.addAction( "android.intent.action.NEW_OUTGOING_CALL" );
////        mContext.registerReceiver( myReceiver, intentFilter );
//        LocalBroadcastManager.getInstance( mContext ).registerReceiver( mMyReceiver, intentFilter ); //アプリ内でしか使わないブロードキャスト(その方が安心・・・)
//
//        Log.d( LOG_TAG, "MyBroadcastReceiver is registered !" );

//// とりあえずコメント。Broadcastの理解に努めよう。
        //
        //サービス開始処理
        //　 開始出来た場合や既にある場合はその情報が返ってくる。
        //   起動失敗すると null で返ってくる。（と以下の英文を読んだからサービスが既に存在するか否かのチェックはいらない！！！、、と思った。）
        //

        // If the service is being started or is already running, the ComponentName of the actual service that was started is returned;
        // else if the service does not exist null is returned. (wrote by google site)
//        Intent intent = new Intent( mContext, TelRecIntentService.class );
        Intent intent = new Intent( mContext, TelRecService.class );
        mIntentFileType = mSettingData.getFormat();
        intent.putExtra( KEY_FILE_TYPE, mIntentFileType);
        ComponentName componentName = mContext.startService( intent );
        if ( componentName == null ) {
            Log.d( LOG_TAG, "Service doesn't exist." );
            return;
        }

        Log.d( LOG_TAG, "Service is started." );

    }

    private void stopTelRecService() {

// とりあえずコメント。リスナー置き換え＆キャンセルから始めよう・・
//        //BroadcastReceiver
////        mContext.unregisterReceiver( mMyReceiver ); //Unregister a previously registered BroadcastReceiver. All filters that have been registered for this BroadcastReceiver will be removed.
//        LocalBroadcastManager.getInstance( mContext ).unregisterReceiver( mMyReceiver );
//
//        Log.d( LOG_TAG, "MyBroadcastReceiver is un-registered !" );

//// とりあえずコメント。Broadcastの理解に努めよう。
        //
        //サービス終了処理
        //

//        Intent intent = new Intent( mContext, TelRecIntentService.class );
        Intent intent = new Intent( mContext, TelRecService.class );
        intent.putExtra(KEY_FILE_TYPE, mIntentFileType);
        // If there is a service matching the given Intent that is already running,
        // then it is stopped and true is returned;
        // else false is returned.  (wrote by google site)
        boolean result = mContext.stopService( intent );
        if ( !result ) {
            Log.d( LOG_TAG, "Service is not found." );
            return;
        }

        Log.d( LOG_TAG, "Service is stopped." );
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
                Log.d( LOG_TAG, "item->"+item.getDate()+" / "+item.getPhoneNumber() );
                //Log.d( LOG_TAG, "isSelected()->" + view.isSelected() ); // setSelected(true) してからタップした場合の isSelected() を確認。isSelected()==falseでした。

                Log.d( LOG_TAG, "mSelectedItem->" + mSelectedView );
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
                    mSelectedView = null;
                    mMenuType = MENU_TYPE_MAIN;
                    ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
//                    getActivity().invalidateOptionsMenu();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d( LOG_TAG, "ListView longClick pos->" + position );

                view.setSelected( !( view.isSelected() ) ); //true：バックグラウンド色が drawable/selector_list_item.xml で定義した色に変わる。
                if ( view.isSelected() ) {
                    mSelectedView = view;
                }

                //メニュー切り替え（削除を表示）
                mMenuType = MENU_TYPE_FILE_DEL;
                ( (AppCompatActivity)mContext ).invalidateOptionsMenu();
//                getActivity().invalidateOptionsMenu(); //--> onPrepareOptionsMenu()がCallされてるはずだが、Logを取るとonCreateOptionMenu() -> onPrepareOptionMenu()になっている・・・。
//                                                        // 起動時はonPrepareOptionsMenu()が2回Callされてる。
//                                                        // developer site によるとメニューの動的変更はonPrepareの方でするようだ。（そう認識してのだが。）
//                                                        // 記載されている理由は、onCreateOptionsの方は一度しか呼ばれないからという事だがLogを取ってみると
//                                                        // invalidateOptionsをCallすると呼ばれてる・・・。
//                                                        // ActivityとFragmentでの違いであればFragmentでの説明でActivityを参考にと記載されているのは・・・。

                return true; //onItemClick()には渡さない。長押しされた場合は選択中とする。
            }
        });
    }

    private ArrayList<ItemData> getFileList() {
        ArrayList<ItemData> arrayList = new ArrayList<>();

//        File fileList = new File( mContext.getFilesDir() + "/telrec/" );
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
            Log.d( LOG_TAG, "file->" + item );
            ItemData itemData = new ItemData();
            String date = item.substring( 0, 8 ); // ファイル名からyyyymmdd を取得
            if ( prevDate.equals( date ) ) {
                itemData.setDate( "" );
            } else {
                StringBuilder stringBuilder = new StringBuilder(); //足し算で出来るけれど、、、。いつもStringBufferとStringBuilderで迷う。。。マルチスレッドではないからね。
                stringBuilder.append( date.substring( 0, 4 ) );
                stringBuilder.append( "/" );
                stringBuilder.append( date.substring( 4, 6 ) );
                stringBuilder.append( "/" );
                stringBuilder.append( date.substring( 6, 8 ) );

                itemData.setDate( stringBuilder.toString() );
            }
            itemData.setPhoneNumber( item );
            arrayList.add( itemData );
            prevDate = date;
        }

        return arrayList;
    }

    private void setActionBar() {
        ActionBar actionBar = ( (AppCompatActivity)mContext ).getSupportActionBar();
//        ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
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
