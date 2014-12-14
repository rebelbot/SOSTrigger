package com.mbientlab.sostrigger;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsFragment extends DialogFragment {
    public interface SettingsState {
        public void setButtonMessage(int position);
        public int getButtonMessage();
        
        public void setShakeMessage(int position);
        public int getShakeMessage();
    }
    
    private SettingsState aCallbacks;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof SettingsState)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        aCallbacks= (SettingsState) activity;
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container);
    }
    
    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.action_settings));
        
        final ArrayAdapter<CharSequence> messagesAdapter= ArrayAdapter.createFromResource(getActivity(), 
                R.array.message_array, android.R.layout.simple_spinner_item);
        messagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        Spinner spinnerObj= (Spinner) view.findViewById(R.id.button_message);
        spinnerObj.setAdapter(messagesAdapter);
        spinnerObj.setSelection(aCallbacks.getButtonMessage());
        spinnerObj.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                aCallbacks.setButtonMessage(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        
        spinnerObj= (Spinner) view.findViewById(R.id.shake_message);
        spinnerObj.setAdapter(messagesAdapter);
        spinnerObj.setSelection(aCallbacks.getShakeMessage());
        spinnerObj.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                aCallbacks.setShakeMessage(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
