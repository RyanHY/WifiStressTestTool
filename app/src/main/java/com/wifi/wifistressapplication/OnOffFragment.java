package com.wifi.wifistressapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by kirt-server on 2016/1/21.
 */
public class OnOffFragment extends Fragment {
    private String TAG = "OnOffFragment";
    private ToggleButton mBtn;
    private EditText mIntervals;
    private TextView mTimeLeft;
    private WifiManager mWifiManager;
    private IntentFilter mWifiStateFilter;
    private TextView mWifiState;
    private TextView mNetworkState;
    private CountDownTimer mTimer;

    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive " + intent.getAction());
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                handleNetworkStateChanged(info.getDetailedState());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.onoff_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "activity created");
        mBtn = (ToggleButton) getActivity().findViewById(R.id.onoff_btn);
        mIntervals = (EditText) getActivity().findViewById(R.id.intervals);
        mTimeLeft = (TextView) getActivity().findViewById(R.id.left_time);
        mWifiState = (TextView) getActivity().findViewById(R.id.wifi_state);
        mNetworkState = (TextView) getActivity().findViewById(R.id.network_state);

        mWifiManager = (WifiManager) getActivity().getSystemService(getActivity().WIFI_SERVICE);

        mWifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    String intervals = mIntervals.getText().toString();
                    mTimeLeft.setText(intervals);
                    mTimer = new CountDownTimer(Integer.parseInt(intervals) * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int sec = (int) millisUntilFinished / 1000;
                            mTimeLeft.setText(String.valueOf(sec));
                        }

                        @Override
                        public void onFinish() {
                            if (mWifiManager.isWifiEnabled()) {
                                mWifiManager.setWifiEnabled(false);
                            } else {
                                mWifiManager.setWifiEnabled(true);
                            }
                            mTimer.start();
                        }
                    }.start();

                } else {
                    // The toggle is disabled
                    mTimer.cancel();
                    mTimeLeft.setText("");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()...");
        getActivity().registerReceiver(mWifiStateReceiver, mWifiStateFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()...");
        getActivity().unregisterReceiver(mWifiStateReceiver);
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mWifiState.setText(R.string.wifistate_enabling);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                mWifiState.setText(R.string.wifistate_enabled);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mWifiState.setText(R.string.wifistate_disabling);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                mWifiState.setText(R.string.wifistate_disabled);
                break;
            default:
                mWifiState.setText(R.string.wifistate_unknown);
                break;
        }
    }

    private void handleNetworkStateChanged(NetworkInfo.DetailedState state) {
        if (mWifiManager.isWifiEnabled()) {
            if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                mNetworkState.setText(R.string.networkstate_authenticating);
            } else if (state == NetworkInfo.DetailedState.BLOCKED) {
                mNetworkState.setText(R.string.networkstate_blocked);
            } else if (state == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                mNetworkState.setText(R.string.networkstate_captive_portal_check);
            } else if (state == NetworkInfo.DetailedState.CONNECTED) {
                mNetworkState.setText(R.string.networkstate_connected);
            } else if (state == NetworkInfo.DetailedState.CONNECTING) {
                mNetworkState.setText(R.string.networkstate_conneecting);
            } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                mNetworkState.setText(R.string.networkstate_disconnected);
            } else if (state == NetworkInfo.DetailedState.DISCONNECTING) {
                mNetworkState.setText(R.string.networkstate_disconnecting);
            } else if (state == NetworkInfo.DetailedState.FAILED) {
                mNetworkState.setText(R.string.networkstate_failed);
            } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                mNetworkState.setText(R.string.networkstate_obtaining_ipaddr);
            } else if (state == NetworkInfo.DetailedState.SCANNING) {
                mNetworkState.setText(R.string.networkstate_scanning);
            } else if (state == NetworkInfo.DetailedState.SUSPENDED) {
                mNetworkState.setText(R.string.networkstate_suspended);
            } else if (state == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                mNetworkState.setText((R.string.networkstate_verifying_poor_link));
            } else if (state == NetworkInfo.DetailedState.IDLE) { // IDELE
                mNetworkState.setText(R.string.networkstate_idle);
            }
        } else {
            mNetworkState.setText(R.string.networkstate_disconnected);
        }
    }
}
