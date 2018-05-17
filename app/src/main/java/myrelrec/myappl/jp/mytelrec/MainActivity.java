package myrelrec.myappl.jp.mytelrec;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements FragmentSettingForm.OnFinishSettingFormListener {

    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( savedInstanceState == null ) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentMain main = FragmentMain.newInstance("", "");
            fragmentTransaction.add(R.id.top_view, main);
            //fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void OnFinishSettingForm() {
        Log.d( LOG_TAG, "OnFinishSettingFormListener() start." );
    }
}
