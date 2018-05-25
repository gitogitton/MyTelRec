package myrelrec.myappl.jp.mytelrec;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Objects;

public class DialogPlayVoice extends DialogFragment {

    private final String LOG_TAG = getClass().getSimpleName();
    private String mDialogTitle = "音声を再生";
    private String mMessage = "";
    private View mView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        Log.d( LOG_TAG, "onCreateDialog()" );

        Bundle args = getArguments();
        if ( args != null ) {
            mMessage = args.getString( "target_file" );
        }

        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        mView = inflater.inflate( R.layout.dialog_play_voice, null );
        builder.setView( mView );

        builder.setTitle( mDialogTitle );
        builder.setMessage( mMessage );

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d( LOG_TAG, "click positive button" );
            }
        });

        builder.setNegativeButton("NG", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d( LOG_TAG, "click negative button" );
            }
        });

        return builder.create();
    }

}
