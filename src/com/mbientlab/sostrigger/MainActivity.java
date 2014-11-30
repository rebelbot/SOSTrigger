package com.mbientlab.sostrigger;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container,
                    false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            final EditText phoneNum= (EditText) view.findViewById(R.id.editText1);
            
            ((Button) view.findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL, 
                            Uri.parse(String.format(Locale.US, "tel:%s", phoneNum.getEditableText().toString())));
                    startActivity(callIntent);
                }
            });
            
            ((Button) view.findViewById(R.id.button2)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    Intent it = new Intent(Intent.ACTION_SENDTO, 
                            Uri.parse(String.format("smsto:%s", phoneNum.getEditableText().toString())));
                    it.putExtra(Intent.EXTRA_TEXT, "Halp! SOS!"); 
                    startActivity(it);
                     */
                    SmsManager sm = SmsManager.getDefault();
                    
                    sm.sendTextMessage(phoneNum.getEditableText().toString(), null, "SOS!", null, null);
                }
            });
        }

    }

}
