package myrelrec.myappl.jp.mytelrec;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSettingForm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSettingForm extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    private final String SETTING_FILE_NAME = "setting.csv"; //format : [file format],[auto start],[use bluetooth]
    private View mView;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "SETTING";
    private static final String ARG_PARAM2 = "param2";

    private SettingData mSetting;
    private String mParam2;

    private Context mContext;
    private OnFinishSettingFormListener mListener;

    public interface OnFinishSettingFormListener {
        void OnFinishSettingForm();
    }

    public FragmentSettingForm() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSettingForm.
     */
    public static FragmentSettingForm newInstance(String param1, String param2) {
        FragmentSettingForm fragment = new FragmentSettingForm();
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
            mSetting = (SettingData) getArguments().getSerializable( ARG_PARAM1 );
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
        if ( context instanceof OnFinishSettingFormListener ) {
            mListener = (OnFinishSettingFormListener)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFinishSettingFormListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setActionBar();

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_setting_form, container, false);
        // Backキーを有効にしてイベントを処理
        mView.setFocusableInTouchMode( true );
        mView.requestFocus(); // フォーカスを持ってこなければBackキーイベントも入ってこない・・・
        mView.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP ) {
                    saveSettingData();
                    ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
                    actionBar.setDisplayHomeAsUpEnabled( false ); // <-icon
                    setHasOptionsMenu( false ); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                    mListener.OnFinishSettingForm();
                    return true; // event処理をここで止める。
                }
                return false;
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        LinearLayout formatArea = view.findViewById( R.id.set_format );
        formatArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectFormatForm();
            }
        });

        TextView format_title = formatArea.findViewById( R.id.text_title );
        TextView format_explain = formatArea.findViewById( R.id.text_explain );

        LinearLayout autoStartArea = view.findViewById( R.id.set_auto );
        TextView autoStart_title = autoStartArea.findViewById( R.id.text_title );
        TextView autoStart_explain = autoStartArea.findViewById( R.id.text_explain );
        Switch autoStart = autoStartArea.findViewById( R.id.switch_status );

        LinearLayout bluetoothArea = view.findViewById( R.id.set_bluetooth );
        TextView bluetooth_title = bluetoothArea.findViewById( R.id.text_title );
        TextView bluetooth_explain = bluetoothArea.findViewById( R.id.text_explain );
        Switch bluetooth = bluetoothArea.findViewById( R.id.switch_status );

        format_title.setText( getString( R.string.file_format ) );
        format_explain.setText( mSetting.getFormat() );

        autoStart_title.setText( getString( R.string.auto_start ) ) ;
        autoStart_explain.setText(  getString( R.string.auto_start_explain ) );
        autoStart.setChecked( mSetting.isAutoStart() );

        bluetooth_title.setText( getString( R.string.bluetooth ) );
        bluetooth_explain.setText( getString( R.string.bluetooth_explain ) );
        bluetooth.setChecked( mSetting.isBluetooth() );
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate( R.menu.menu_setting, menu );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d( LOG_TAG, "onOptionsItemSelected() start." );
        switch ( item.getItemId()) {
            case android.R.id.home : //←キー
            {
                //Log.d( LOG_TAG, "android.R.id.home" );
                saveSettingData();
                resetActionBar();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                mListener.OnFinishSettingForm();
                return true;
            }
            case R.id.menu_end_without_save : //保存せずに全画面に戻る。
            {
                resetActionBar();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                mListener.OnFinishSettingForm();
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d( LOG_TAG, "FragmentSettingForm#onActivityResult() start." );

        setActionBar();
        super.onActivityResult(requestCode, resultCode, data);
    }

    //
    //private methods
    //
    private void showSelectFormatForm() {
        Log.d( LOG_TAG, "showSelectFormatForm() start." );

        FragmentSelectFormat selectFormat = FragmentSelectFormat.newInstance( this, 0 );
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.top_view, selectFormat );
        fragmentTransaction.addToBackStack( null );
        fragmentTransaction.commit();
    }

    private boolean saveSettingData() {

        boolean rtn;
        rtn = checkSettingData();
        if ( !rtn ) {
            Log.d( LOG_TAG, "checkSettingData() returned false." );
            return false;
        }

        SettingData settin = new SettingData();
        FileOutputStream outputStream = null;
        try {
            String BR = System.getProperty( "line.separator" ); //改行コード取得
            String separator = ",";
            outputStream = getContext().openFileOutput( SETTING_FILE_NAME, Context.MODE_PRIVATE );

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.setLength( 0 );
            TextView textView = mView.findViewById( R.id.set_format ).findViewById( R.id.text_explain );
            stringBuilder.append( textView.getText().toString() );
            stringBuilder.append( separator );
            Switch autoStart = mView.findViewById( R.id.set_auto ).findViewById( R.id.switch_status );
            stringBuilder.append( autoStart.isChecked()?"1":"0" );
            stringBuilder.append( separator );
            Switch bluetooth = mView.findViewById( R.id.set_bluetooth ).findViewById( R.id.switch_status );
            stringBuilder.append( bluetooth.isChecked()?"1":"0" );
            stringBuilder.append( BR );

            outputStream.write( stringBuilder.toString().getBytes() );

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( outputStream != null ) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    //when NG data exist, return false.
    private boolean checkSettingData() {
        TextView textView = mView.findViewById( R.id.set_format ).findViewById( R.id.text_explain );
        if ( textView.getText().toString().equals( "" ) ) { // ファイル形式が空
            return false;
        }
        return true;
    }

    private void setActionBar() {
        ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
        actionBar.setTitle( "設定" );
        actionBar.setDisplayHomeAsUpEnabled( true ); // ←icon

        setHasOptionsMenu( true ); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)
    }

    private void resetActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false); // <-icon
        setHasOptionsMenu(false); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)
    }
}
