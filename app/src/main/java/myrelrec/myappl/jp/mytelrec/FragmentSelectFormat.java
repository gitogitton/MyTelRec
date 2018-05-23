package myrelrec.myappl.jp.mytelrec;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class FragmentSelectFormat extends Fragment {

    public enum FormatList { // 初めて使ってみた・・・
        WAV( "WAV" ),
        MP3( "MP3" ),
        MP4( "MP4" );

        private final String type;
        private final String DEFAULT_TYPE = "WAV";

        /*private*/ FormatList( String wav ) {
            this.type = wav;
            Log.d( "test enum", "arg = "+wav );
        }

        public String getValue() {
            return this.type;
        }
    }

    private final String LOG_TAG = getClass().getSimpleName();
    private String mSpecifiedType;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "SETTING";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private Context mContext;



    public FragmentSelectFormat() {
        // Required empty public constructor
    }

    public static FragmentSelectFormat newInstance( Fragment f, int reqCode ) {
        FragmentSelectFormat fragment = new FragmentSelectFormat();
        fragment.setTargetFragment( f, reqCode );
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSpecifiedType  = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setActionBar();

        View view = inflater.inflate(R.layout.fragment_select_format, container, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP ) {
                    Log.d( LOG_TAG, "FragmentSelectFormat.onKey() start." );
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    actionBar.setDisplayHomeAsUpEnabled(false); // ←icon
                    setHasOptionsMenu(false); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)

                    Intent intent = new Intent();
                    intent.putExtra( "FILETYPE", mSpecifiedType );
                    Fragment targetFragment = getTargetFragment();
                    targetFragment.onActivityResult( getTargetRequestCode(), 0, intent );

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                    return true; // event処理をここで止める。
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        ArrayList<String> arrayList = new ArrayList<>();

        FormatList[] lists = FormatList.values();
        for ( FormatList list : lists ) {
            arrayList.add( list.getValue() );
        }

// simple_list_item_single_choice にぴったりなので使用する。        FormatListAdapter adapter = new FormatListAdapter( getContext(), R.layout.select_format_item, arrayList );

        ArrayAdapter<String> adapter = new ArrayAdapter( mContext, android.R.layout.simple_list_item_single_choice );
        adapter.addAll( arrayList );

        ListView listView = view.findViewById( R.id.list_formatOfSoundFile );
        listView.setChoiceMode( ListView.CHOICE_MODE_SINGLE ); // simple_list_item_single_choice でセットで使う。ラジオボタン付きリスト。楽です。
        listView.setAdapter( adapter );

        for ( int i=0; i<listView.getCount(); i++ ) { //初期値セット
            String type = (String) listView.getItemAtPosition( i );
            if ( type.equals( mSpecifiedType  ) ) {
                listView.setItemChecked( i, true );
                break;
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSpecifiedType  = (String) parent.getItemAtPosition( position );
                Log.d( LOG_TAG, "click item. [" + mSpecifiedType + "]");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d( LOG_TAG, "onOptionsItemSelected() start." );
        switch ( item.getItemId()) {
            case android.R.id.home : //←キー
                //Log.d( LOG_TAG, "android.R.id.home" );
                resetActionBar();

                Intent intent = new Intent();
                intent.putExtra( "FILETYPE", mSpecifiedType );
                Fragment targetFragment = getTargetFragment();
                targetFragment.onActivityResult( getTargetRequestCode(), 0, intent );

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //
    //private methods
    //
    private void setActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle( "フォーマット選択" );
        actionBar.setDisplayHomeAsUpEnabled( true ); // ←icon
        setHasOptionsMenu( true ); // option menu を使うことを宣言 (これをしないとHomeAsUpも効かない・・・)
    }

    private void resetActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled( false ); // ←icon
        setHasOptionsMenu( false ); // option menu
    }
}
