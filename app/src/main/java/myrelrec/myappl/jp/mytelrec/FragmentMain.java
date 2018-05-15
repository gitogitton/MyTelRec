package myrelrec.myappl.jp.mytelrec;

import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

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
    private SettingData mSettingData = new SettingData();

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
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d( LOG_TAG, "onCheckedChanged()" );
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
    private void showRecordingFileList() {
        ArrayList<ItemData> arrayList = getFileList();
        RecordingFileListAdapter adapter = new RecordingFileListAdapter( mContext, R.layout.file_list_item, arrayList );

        ListView listView = mView.findViewById( R.id.list_recordFileList );
        listView.setAdapter( adapter );
    }

    private ArrayList<ItemData> getFileList() {
        ArrayList<ItemData> arrayList = new ArrayList<>();
//test data start
        ItemData[] itemData = new ItemData[10];
        for ( int i=0; i< itemData.length; i++ ) {
            itemData[ i ] = new ItemData();
            String stringDate;

//            Log.d( LOG_TAG, " i -> " + i );
//            if ( i!=0 && ( i%3 == 0 ) ) {
//                stringDate = "";
//            } else {
//                stringDate = "data" + Integer.toString( i );
//            }

            stringDate = "data" + Integer.toString( i );

            String stringNumber = "name" + Integer.toString( i );
            itemData[ i ].setDate( stringDate );
            itemData[ i ].setPhoneNumber( stringNumber );
        }
//test data end

//        for ( ItemData item : itemData ) {
//            arrayList.add( item );
//        }
        arrayList.addAll( Arrays.asList(itemData) );

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
