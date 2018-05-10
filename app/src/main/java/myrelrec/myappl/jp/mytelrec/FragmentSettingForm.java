package myrelrec.myappl.jp.mytelrec;


import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSettingForm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSettingForm extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();
    private View mView;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private Context mContext;
    private OnFinishSettingFormListener mListener;

    public interface OnFinishSettingFormListener {
        void OnFinishSettingFormListener();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
        actionBar.setTitle( "設定" );
        actionBar.setDisplayHomeAsUpEnabled( true ); // <-icon

        setHasOptionsMenu( true ); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_setting_form, container, false);
        // Backキーを有効にしてイベントを処理
        mView.setFocusableInTouchMode( true );
        mView.requestFocus(); // フォーカスを持ってこなければBackキーイベントも入ってこない・・・
        mView.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP ) {
                    Log.d( LOG_TAG, "push back key." );
                    ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
                    actionBar.setDisplayHomeAsUpEnabled( false ); // <-icon
                    setHasOptionsMenu( false ); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                    mListener.OnFinishSettingFormListener();
                    return true; // event処理をここで止める。
                }
                return false;
            }
        });
        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.d( LOG_TAG, "onOptionsItemSelected() start." );
        switch ( item.getItemId()) {
            case android.R.id.home : //←キー
                //Log.d( LOG_TAG, "android.R.id.home" );
                ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled( false ); // <-icon
                setHasOptionsMenu( false ); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                mListener.OnFinishSettingFormListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
