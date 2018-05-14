package myrelrec.myappl.jp.mytelrec;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentMain.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentMain#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMain extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context mContext;
    private View mView;

    public FragmentMain() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentMain.
     */
    // TODO: Rename and change types and number of parameters
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
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        showRecordingFileList();
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
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
}
