package com.wifi.wifistressapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by kirt-server on 2016/1/27.
 */
public class HotSpotOnOffFragment extends Fragment {
    private String TAG = "HotSpotOnOffFragment";
    private ToggleButton mBtn;
    private EditText mIntervals;
    private TextView mTimeLeft;
    private TextView mState;
    private WifiManager mWifiManager;
    private IntentFilter mWifiApStateFilter;
    private CountDownTimer mTimer;
    private WifiConfiguration mWifiConfig = null;
    private boolean wifiEnabled = false;

    private final BroadcastReceiver mWifiApStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onRecive " + intent.getAction());
            String action = intent.getAction();
            if (action.equals(WifiApControl.WIFI_AP_STATE_CHANGED_ACTION)) {
                handleWifiApStateChanged(intent.getIntExtra(WifiApControl.EXTRA_WIFI_AP_STATE, WifiApControl.WIFI_AP_STATE_FAILED));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hotspotonoff_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBtn = (ToggleButton) getActivity().findViewById(R.id.onoff_btn);
        mIntervals = (EditText) getActivity().findViewById(R.id.intervals);
        mTimeLeft = (TextView) getActivity().findViewById(R.id.left_time);
        mState = (TextView) getActivity().findViewById(R.id.wifiap_state);
        mWifiManager = (WifiManager) getActivity().getSystemService(getActivity().WIFI_SERVICE);
        final WifiApControl apControl = WifiApControl.getApControl(mWifiManager);
        if (apControl == null) {
            Log.e(TAG, "Hotspot is not supported on this device");
            return;
        }
        mWifiConfig = apControl.getWifiApConfiguration();
        if (mWifiConfig == null)
            Log.i(TAG, "WifiApConfiguration is null");

        mWifiApStateFilter = new IntentFilter(WifiApControl.WIFI_AP_STATE_CHANGED_ACTION);
        handleWifiApStateChanged(apControl.getWifiApState());

        mBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Log.i(TAG, "Hotspot on/off started!");
                    int intervals = Integer.parseInt(mIntervals.getText().toString());
                    mTimeLeft.setText(mIntervals.getText());
                    if (Build.VERSION.SDK_INT < 23) {
                        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                            mWifiManager.setWifiEnabled(false);
                            wifiEnabled = true;
                        }
                    }
                    mTimer = new CountDownTimer(intervals * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int sec = (int) millisUntilFinished / 1000;
                            mTimeLeft.setText(String.valueOf(sec));
                        }

                        @Override
                        public void onFinish() {
                            if (apControl.isWifiApEnabled()) {
                                Log.i(TAG, "Turn off hotspot");
                                apControl.setWifiApEnabled(mWifiConfig, false);
                            } else {
                                Log.i(TAG, "Turn on hotspot");
                                apControl.setWifiApEnabled(mWifiConfig, true);
                            }
                            mTimer.start();
                        }
                    }.start();
                } else {
                    // The toggle is disabled
                    Log.i(TAG, "Hotspot on/off stopped!");
                    mTimeLeft.setText("");
                    mTimer.cancel();
                    if (wifiEnabled) {
                        mWifiManager.setWifiEnabled(true);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()...");
        getActivity().registerReceiver(mWifiApStateReceiver, mWifiApStateFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()...");
        getActivity().unregisterReceiver(mWifiApStateReceiver);
    }

    private void handleWifiApStateChanged(int state) {
        switch (state) {
            case WifiApControl.WIFI_AP_STATE_DISABLING:
                mState.setText("Disabling");
                break;
            case WifiApControl.WIFI_AP_STATE_DISABLED:
                mState.setText("Disabled");
                break;
            case WifiApControl.WIFI_AP_STATE_ENABLING:
                mState.setText("Enabling");
                break;
            case WifiApControl.WIFI_AP_STATE_ENABLED:
                mState.setText("Enabled");
                break;
            default:
                mState.setText("Failed");
                break;
        }
    }
}
