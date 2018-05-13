package myrelrec.myappl.jp.mytelrec;

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

public class FragmentSelectFormat extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    final String[] FOMAT_LIST = { "WAV", "AAC ADTS", "AMR NB", "AMR WB", "MPEG2/TS", "MPEG4", "3GPP", "VP8" };
    public enum formatList { // String[] FOMAT_LIST に合わせる。
        WAV,
        AAC_ADTS,
        AMR_NB,
        AMR_WB,
        MPEG2,
        MPEG4,
        GPP_3,
        VP8
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

                    Fragment targetFragment = getTargetFragment();
                    targetFragment.onActivityResult( getTargetRequestCode(), 0, null );

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

//        ArrayList<String> arrayList = new ArrayList<>();
//        arrayList.add( "WAV" );
//        arrayList.add( "AAC ADTS" );
//        arrayList.add( "AMR NB" );
//        arrayList.add( "AMR WB" );
//        arrayList.add( "MPEG2/TS" );
//        arrayList.add( "MPEG4" );
//        arrayList.add( "3GPP" );
//        arrayList.add( "VP8" );
//
// simple_list_item_single_choice にぴったりなので使用する。        FormatListAdapter adapter = new FormatListAdapter( getContext(), R.layout.select_format_item, arrayList );

        ArrayAdapter<String> adapter = new ArrayAdapter( getContext(), android.R.layout.simple_list_item_single_choice );
        adapter.addAll( FOMAT_LIST );

        ListView listView = view.findViewById( R.id.list_formatOfSoundFile );
        listView.setChoiceMode( ListView.CHOICE_MODE_SINGLE ); // simple_list_item_single_choice でセットで使う。ラジオボタン付きリスト。楽です。
        listView.setAdapter( adapter );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String formatType = (String) parent.getItemAtPosition( position );
                Log.d( LOG_TAG, "click item. [" + formatType + "]");
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

                Fragment targetFragment = getTargetFragment();
                targetFragment.onActivityResult( getTargetRequestCode(), 0, null );

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
