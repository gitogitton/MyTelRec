package myrelrec.myappl.jp.mytelrec;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FragmentMain extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();
    private final String SETTING_FILE_NAME = "setting.csv"; //format : [file format],[auto start],[use bluetooth]

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context mContext;
    private View mView;
    private SwitchCompat mSwitchCompat;
    private SettingData mSettingData = new SettingData();

    private MediaPlayer mMediaPlayer = null;

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
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_main, container, false);

        setActionBar();
        setHasOptionsMenu( true );

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        showRecordingFileList();
        restoreSettingData();
        if ( isRecordServiceAlive() ) { //通話録音サービスが起動中なら
            mSettingData.setAutoStart( true ); //通話と同時に録音を開始に設定
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate( R.menu.menu_main, menu );

        MenuItem item = menu.findItem( R.id.menu_activate );
        SwitchCompat switchCompat = (SwitchCompat) item.getActionView();
        switchCompat.setChecked( mSettingData.isAutoStart() );
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.menu_setting :
                Log.d( LOG_TAG, "select menu_setting." );
                showSettingForm();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    //
    //private methods
    //
    private boolean isRecordServiceAlive() {
        //（確か）API 26から廃止
        ActivityManager manager = (ActivityManager) mContext.getSystemService( Context.ACTIVITY_SERVICE );
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
//        Intent intent = new Intent( mContext, TelRecIntentService.class );
        Intent intent = new Intent( mContext, TelRecService.class );
        ComponentName componentName = mContext.startService( intent );
        if ( componentName == null ) {
            Log.d( LOG_TAG, "Service doesn't exist." );
            return;
        }

        Log.d( LOG_TAG, "Service is started." );
    }

    private void stopTelRecService() {

        //
        //サービス終了処理
        //

//        Intent intent = new Intent( mContext, TelRecIntentService.class );
        Intent intent = new Intent( mContext, TelRecService.class );
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

    private boolean isServiceStarted() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService( Context.ACTIVITY_SERVICE );

        //
        // ActivityManager#getRunningServices()
        // This method was deprecated in API level 26.
        // ( use bellow)
        //

        if ( manager == null ) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> info = manager.getRunningServices( Integer.MAX_VALUE );
        for ( ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices( Integer.MAX_VALUE ) ) {
//            if ( YourService.class.getName().equals( serviceInfo.service.getClassName() ) ) {
                return true;
//            }
        }
        return false;
    }

    private void showRecordingFileList() {
        ArrayList<ItemData> arrayList = getFileList();
        RecordingFileListAdapter adapter = new RecordingFileListAdapter( mContext, R.layout.file_list_item, arrayList );

        ListView listView = mView.findViewById( R.id.list_recordFileList );
        listView.setAdapter( adapter );
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d( LOG_TAG, "select->" + position );
                ItemData item = (ItemData) parent.getItemAtPosition( position );
                Log.d( LOG_TAG, "item->"+item.getDate()+" / "+item.getPhoneNumber() );

                //
                //再生処理を書こう！！！
                //
                //
                // ↓　とりあえずの処理なのであとでちゃんと作れ！！
                if ( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                    Log.d( LOG_TAG, "mediaPlayter stop." );
                    mMediaPlayer.pause();
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                } else {
                    Log.d( LOG_TAG, "mediaPlayter start." );
                    mMediaPlayer = MediaPlayer.create( mContext, Uri.parse( "/storage/sdcard0/telrec/"+item.getPhoneNumber() ) );
                    mMediaPlayer.start();
                    mMediaPlayer.setVolume( (float) 1.0, (float)1.0 ); // 0.0 - 1.0
                    mMediaPlayer.setLooping( false );
                }
//                mMediaPlayer.setVolume(    );
            }
        });
    }

    private ArrayList<ItemData> getFileList() {
        ArrayList<ItemData> arrayList = new ArrayList<>();

//        File fileList = new File( mContext.getFilesDir() + "/telrec/" );
        File fileList = new File( "/storage/sdcard0/telrec/" );
        String[] dirList = fileList.list();
        if ( dirList == null || dirList.length <= 0 ) {
            return null;
        }

        for ( String item : dirList ) {
            Log.d( LOG_TAG, "file->" + item );
            ItemData itemData = new ItemData();
            itemData.setDate( "2018/5/15" );
            itemData.setPhoneNumber( item );
            arrayList.add( itemData );
        }

        return arrayList;
    }

    private void setActionBar() {
        ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
        actionBar.setTitle( R.string.app_name );
    }

    private void showSettingForm() {

        FragmentSettingForm settingForm = FragmentSettingForm.newInstance( "", "" );

        Bundle args = new Bundle();
        args.putSerializable( "SETTING", mSettingData );
        settingForm.setArguments( args );

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.top_view, settingForm );
        fragmentTransaction.addToBackStack( null );
        fragmentTransaction.commit();
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
            setting.setFormat( FragmentSelectFormat.FormatList.WAV.getValue() ); // 遠い・・・、enum 、外に出した方がいい・・・
            setting.setAutoStart( false );
            setting.setBluetooth( false );
            return setting;
        }

        try {
            inputStream = mContext.openFileInput( fileName );
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
