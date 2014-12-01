package com.mbientlab.sostrigger;

import java.util.Locale;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.MechanicalSwitch;
import com.mbientlab.sostrigger.MWScannerFragment.ScannerCallback;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements ScannerCallback {
    private PlaceholderFragment mainFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mainFragment= new PlaceholderFragment();
            getFragmentManager().beginTransaction().add(R.id.container, mainFragment).commit();
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
        
        switch(item.getItemId()) {
        case R.id.action_settings:
            return true;
        case R.id.action_connect:
            new MWScannerFragment().show(getFragmentManager(), "metawear_scanner_fragment");
        }        
        return super.onOptionsItemSelected(item);
    }
    
    

    @Override
    public void btDeviceSelected(BluetoothDevice device) {
        mainFragment.setBtDevice(device);
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(MetaWearBleService.getMetaWearBroadcastReceiver(), 
                MetaWearBleService.getMetaWearIntentFilter());
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MetaWearBleService.getMetaWearBroadcastReceiver());
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements ServiceConnection {
        private MetaWearBleService mwService= null;
        private MetaWearController mwController= null;
        private EditText phoneNumText= null;
        
        public PlaceholderFragment() {
        }

        public void setBtDevice(BluetoothDevice device) {
            mwController.addDeviceCallback(new MetaWearController.DeviceCallbacks() {
                @Override
                public void connected() {
                    ((MechanicalSwitch) mwController.getModuleController(Module.MECHANICAL_SWITCH)).enableNotification();
                    Toast.makeText(getActivity(), R.string.toast_connected, Toast.LENGTH_SHORT).show();
                    mwController.removeDeviceCallback(this);
                }
            });
            mwService.connect(device);
        }
        
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            activity.getApplicationContext().bindService(new Intent(activity,MetaWearBleService.class), 
                    this, Context.BIND_AUTO_CREATE);
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            phoneNumText= (EditText) view.findViewById(R.id.editText1);
            
            ((Button) view.findViewById(R.id.scan_control)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL, 
                            Uri.parse(String.format(Locale.US, "tel:%s", phoneNumText.getEditableText().toString())));
                    startActivity(callIntent);
                }
            });
            
            ((Button) view.findViewById(R.id.button2)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendText(phoneNumText.getEditableText().toString());
                }
            });
        }
        
        @Override
        public boolean onOptionsItemSelected (MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_disconnect:
                if (mwService != null) {
                    mwService.close();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mwService= ((MetaWearBleService.LocalBinder) service).getService();
            mwController= mwService.getMetaWearController();
            mwController.addModuleCallback(new MechanicalSwitch.Callbacks() {
                @Override
                public void pressed() {
                    sendText(phoneNumText.getEditableText().toString());
                }
            });
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) { }
        
        private void sendText(String phoneNum) {
            /*
            Intent it= new Intent(Intent.ACTION_SENDTO, 
                    Uri.parse(String.format("smsto:%s", phoneNum.getEditableText().toString())));
            it.putExtra(Intent.EXTRA_TEXT, "Halp! SOS!"); 
            startActivity(it);
             */
            SmsManager sm= SmsManager.getDefault();
            sm.sendTextMessage(phoneNum, null, "SOS!", null, null);
        }
        
    }
}
