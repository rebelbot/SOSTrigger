/*
 * Copyright 2014 MbientLab Inc. All rights reserved.
 */
package com.mbientlab.sostrigger;

import java.util.Locale;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment that scans for and reports discovered ble devices
 * @author Eric Tsai
 */
public class MWScannerFragment extends DialogFragment {
    public interface ScannerCallback {
        public void btDeviceSelected(BluetoothDevice device);
    }
    
    private final static int RSSI_BAR_SCALE= 100 / 5;
    private final static long SCAN_PERIOD= 10000;
    
    private BluetoothAdapter mBluetoothAdapter= null;
    private BLEDeviceListAdapter mLeDeviceListAdapter= null;
    private boolean isScanning;
    private Handler mHandler;
    
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
                viewHolder.rssiChart= (ImageView) convertView.findViewById(R.id.imageView1);
                
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
            viewHolder.deviceRSSI.setText(String.format(Locale.US, "%d dBm", info.rssi));
            viewHolder.rssiChart.setImageLevel((127 + info.rssi + 10) / RSSI_BAR_SCALE);
            return convertView;
        }
        
        private class ViewHolder {
            public TextView deviceAddress;
            public TextView deviceName;
            public TextView deviceRSSI;
            public ImageView rssiChart;
        }

    }
    
    private Button scanControl;
    private ScannerCallback callback;
    
    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof ScannerCallback)) {
            throw new RuntimeException("Acitivty does not implement ScannerCallback interface");
        }
        
        callback= (ScannerCallback) activity;
        super.onAttach(activity);
    }
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLeDeviceListAdapter= new BLEDeviceListAdapter(getActivity(), R.id.mw_ble_info_layout, inflater);
        mHandler= new Handler();
        return inflater.inflate(R.layout.metawear_device_selection, container);
    }    
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView scannedDevices= (ListView) view.findViewById(R.id.scanned_devices);
        scannedDevices.setAdapter(mLeDeviceListAdapter);
        scannedDevices.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                callback.btDeviceSelected(mLeDeviceListAdapter.getItem(position).device);
                dismiss();
            }
        });
        
        scanControl= (Button) view.findViewById(R.id.scan_control);
        scanControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning) {
                    stopBleScan();
                } else {
                    startBleScan();
                }
            }
        });
        
        //Main activity has already checked a bluetooth manager exists
        mBluetoothAdapter= ((BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        startBleScan();
    }
    
    @Override
    public void onDestroyView() {
        stopBleScan();
        super.onDestroyView();
    }
    
    private void startBleScan() {
        if (!isScanning) {
            mLeDeviceListAdapter.clear();
            isScanning= true;
            scanControl.setText(R.string.label_stop);
        
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopBleScan();
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);            
        }
    }
    
    private void stopBleScan() {
        if (isScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        
            isScanning= false;
            scanControl.setText(R.string.label_scan);
        }
    }
}
