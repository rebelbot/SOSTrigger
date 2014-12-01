package com.mbientlab.sostrigger;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.MechanicalSwitch;
import com.mbientlab.sostrigger.MWScannerFragment.ScannerCallback;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import static android.provider.ContactsContract.CommonDataKinds.Phone.*;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements ScannerCallback {
    private final static String FRAGMENT_KEY= "com.mbientlab.sostrigger.MainActivity.FRAGMENT_KEY";
    private PlaceholderFragment mainFragment= null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mainFragment= new PlaceholderFragment();
            getFragmentManager().beginTransaction().add(R.id.container, mainFragment).commit();
        } else {
            mainFragment= (PlaceholderFragment) getFragmentManager().getFragment(savedInstanceState, FRAGMENT_KEY);
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
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mainFragment != null) {
            getFragmentManager().putFragment(outState, FRAGMENT_KEY, mainFragment);
        }
    }
    
    private static class ContactInfo {
        public String name;
        public String number;
        
        @Override
        public String toString() { return name; }
    }
    private static class ContactListAdapter extends ArrayAdapter<ContactInfo> {
        private final LayoutInflater mInflator;
        
        public ContactListAdapter(Context context, int resource, LayoutInflater inflator) {
            super(context, resource);
            this.mInflator= inflator;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView= mInflator.inflate(R.layout.contact_info, null);
                viewHolder= new ViewHolder();
                viewHolder.contactName= (TextView) convertView.findViewById(R.id.contact_name);
                viewHolder.contactNumber= (TextView) convertView.findViewById(R.id.contact_number);
                
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ContactInfo info= getItem(position);
            
            viewHolder.contactNumber.setText(info.number);
            viewHolder.contactName.setText(info.name);
            return convertView;
        }
        
        private class ViewHolder {
            public TextView contactName;
            public TextView contactNumber;
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements ServiceConnection {
        private MetaWearBleService mwService= null;
        private MetaWearController mwController= null;
        private EditText phoneNumText= null;
        private ContactListAdapter contacts= null;
        private ContactInfo saviour= null;
        private ListView possibleContacts= null;
        
        private TextWatcher txtWatcher= new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start,
                    int before, int count) {
                if (possibleContacts.getVisibility() != View.VISIBLE) {
                    possibleContacts.setVisibility(View.VISIBLE);
                }
                if (s.length() > 0) {
                    populate(s.toString());
                } else {
                    contacts.clear();
                    contacts.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
        
        public PlaceholderFragment() {
        }

        public void setBtDevice(BluetoothDevice device) {
            mwController.addDeviceCallback(new MetaWearController.DeviceCallbacks() {
                @Override
                public void connected() {
                    ((MechanicalSwitch) mwController.getModuleController(Module.MECHANICAL_SWITCH)).enableNotification();
                    Toast.makeText(getActivity(), R.string.toast_connected, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void disconnected() {
                    Toast.makeText(getActivity(), R.string.toast_disconnected, Toast.LENGTH_SHORT).show();
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
        public void onDestroy() {
            super.onDestroy();
            
            getActivity().getApplicationContext().unbindService(this);
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            setRetainInstance(true);
            setHasOptionsMenu(true);
            
            contacts= new ContactListAdapter(getActivity(), R.id.contact_info_layout, inflater);
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            phoneNumText= (EditText) view.findViewById(R.id.editText1);
            phoneNumText.addTextChangedListener(txtWatcher);
            
            possibleContacts= (ListView) view.findViewById(R.id.listView1); 
            possibleContacts.setAdapter(contacts);
            possibleContacts.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    saviour= contacts.getItem(position);
                    possibleContacts.setVisibility(View.GONE);
                    
                    phoneNumText.removeTextChangedListener(txtWatcher);
                    phoneNumText.setText(saviour.name);
                    phoneNumText.addTextChangedListener(txtWatcher);
                }
            });
        }
        
        @Override
        public boolean onOptionsItemSelected (MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_disconnect:
                if (mwService != null) {
                    mwService.close(true);
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
                    if (saviour != null) {
                        sendText(saviour.number);
                    } else if (contacts.getCount() == 1) {
                        sendText(contacts.getItem(0).name);
                    } else {
                        Toast.makeText(getActivity(), R.string.error_no_contact, Toast.LENGTH_SHORT).show();
                    }
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
            
            try {
                SmsManager.getDefault().sendTextMessage(phoneNum, null, 
                        getActivity().getResources().getString(R.string.text_sos_msg)
                        , null, null);
            } catch (IllegalArgumentException ex) {
                Toast.makeText(getActivity(), R.string.error_invalid_number, Toast.LENGTH_SHORT).show();
            }
        }
        
        private void populate(final String key) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    contacts.clear();
                    saviour= null;

                    ContentResolver contentResolver = getActivity().getContentResolver();
                    Cursor cursor = contentResolver.query(CONTENT_URI, new String[] {DISPLAY_NAME, NUMBER}, 
                            DISPLAY_NAME + " LIKE ?", new String[] {String.format("%s%%", key)}, null);
                    
                    if (cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                            
                            
                            ContactInfo newInfo= new ContactInfo();
                            newInfo.name= name;
                            newInfo.number= cursor.getString(cursor.getColumnIndex(NUMBER));
                            contacts.add(newInfo);       
                        }
                    }
                    
                    contacts.notifyDataSetChanged();
                }
            });
            
        }
    }
}
