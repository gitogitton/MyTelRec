package myrelrec.myappl.jp.mytelrec;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordingFileListAdapter extends ArrayAdapter<ItemData> {

    private int mResourceId;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<ItemData> mArrayList;

    public RecordingFileListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ItemData> objects) {
        super(context, resource, objects);
        mLayoutInflater = LayoutInflater.from( context );
        mContext = context;
        mResourceId = resource;
        mArrayList = objects;
    }

    @Override
    public int getCount() {
        return mArrayList==null ? 0:mArrayList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if ( convertView == null ) {
            convertView = mLayoutInflater.inflate( mResourceId, parent, false );
        }

        ItemData itemData = getItem( position );

        TextView textDate = convertView.findViewById( R.id.text_date );
        TextView textNumber = convertView.findViewById( R.id.text_phoneNumber );
        if ( itemData.getDate().equals( "" ) ) {
            textDate.setVisibility( View.GONE ); //詰めて非表示
        } else {
            textDate.setText( itemData.getDate() );
        }
        textNumber.setText( itemData.getPhoneNumber() );

        return convertView;
    }
}
