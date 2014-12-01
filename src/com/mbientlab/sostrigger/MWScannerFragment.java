package com.mbientlab.sostrigger;

import java.util.Locale;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MWScannerFragment extends DialogFragment {
    private BluetoothAdapter mBluetoothAdapter= null;
    private BLEDeviceListAdapter mLeDeviceListAdapter= null;
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BtDeviceRssi temp= new BtDeviceRssi();
                    temp.device= device;
                    temp.rssi= rssi;
                    
                    mLeDeviceListAdapter.add(temp);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    
    public class BtDeviceRssi {
        public BluetoothDevice device;
        public int rssi;
    }
    public class BLEDeviceListAdapter extends ArrayAdapter<BtDeviceRssi> {
        private final LayoutInflater mInflator;
        
        public BLEDeviceListAdapter(Context context, int resource, LayoutInflater inflator) {
            super(context, resource);
            this.mInflator= inflator;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView= mInflator.inflate(R.layout.metawear_ble_info, null);
                viewHolder= new ViewHolder();
                viewHolder.deviceAddress= (TextView) convertView.findViewById(R.id.device_address);
                viewHolder.deviceName= (TextView) convertView.findViewById(R.id.device_name);
                viewHolder.deviceRSSI= (TextView) convertView.findViewById(R.id.rssi);
                
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BtDeviceRssi info= (BtDeviceRssi)getItem(position);
            final String deviceName= info.device.getName();
            
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknown Device");
            viewHolder.deviceAddress.setText(info.device.getAddress());
            viewHolder.deviceRSSI.setText(String.format(Locale.US, "%d", info.rssi));
            return convertView;
        }
        
        private class ViewHolder {
            public TextView deviceAddress;
            public TextView deviceName;
            public TextView deviceRSSI;
        }

    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLeDeviceListAdapter= new BLEDeviceListAdapter(getActivity(), R.id.mw_ble_info_layout, inflater);
        return inflater.inflate(R.layout.metawear_device_selection, container);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView devices= (ListView) view.findViewById(R.id.listView1);
        devices.setAdapter(mLeDeviceListAdapter);
        final BluetoothManager bluetoothManager=
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter= bluetoothManager.getAdapter();
        
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show();
            return;
        }
        
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }
    
    @Override
    public void onDestroyView() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        super.onDestroyView();
    }
}
