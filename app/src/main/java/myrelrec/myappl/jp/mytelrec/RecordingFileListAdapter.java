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

    class ViewHolder {
        TextView textDate;
        TextView textPhoneNumber;
    }

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

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).getDate().equals( "" );
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        if ( convertView == null ) {
            convertView = mLayoutInflater.inflate( mResourceId, parent, false );
            viewHolder = new ViewHolder();
            viewHolder.textDate = convertView.findViewById( R.id.text_date );
            viewHolder.textPhoneNumber = convertView.findViewById( R.id.text_phoneNumber );
            convertView.setTag( viewHolder );
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ItemData itemData = getItem( position );

        if ( isEnabled( position ) ) {
            viewHolder.textDate.setVisibility( View.GONE );
            viewHolder.textPhoneNumber.setVisibility( View.VISIBLE );
            if ( itemData != null ) {
                viewHolder.textPhoneNumber.setText( itemData.getPhoneNumber() );
            }
        } else {
            viewHolder.textDate.setVisibility( View.VISIBLE );
            if ( itemData != null ) {
                viewHolder.textDate.setText( itemData.getDate() );
            }
            viewHolder.textPhoneNumber.setVisibility( View.GONE );
        }

//ListViewのリストを日付でグループ分けする。このままでは出来ないので修正
//        TextView textDate = convertView.findViewById( R.id.text_date );
//        TextView textNumber = convertView.findViewById( R.id.text_phoneNumber );
//
//        if ( itemData.getDate().equals( "" ) ) {
//            Log.d( "test", "  --GONE" );
//            textDate.setVisibility( View.GONE ); //詰めて非表示
//        } else {
//            textDate.setText( itemData.getDate() );
//        }
//        textNumber.setText( itemData.getPhoneNumber() );
//


        return convertView;
    }
}
