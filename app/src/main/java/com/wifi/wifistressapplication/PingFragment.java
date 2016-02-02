package com.wifi.wifistressapplication;

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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by kirt-server on 2016/1/26.
 */
public class PingFragment extends Fragment {
    private final String TAG = "PingFragment";
    private EditText mDestAddress;
    private EditText mPingInterval;
    private EditText mPingTimeout;
    private TextView mLeftTime;
    private TextView mPassCount;
    private TextView mFailCount;
    private ToggleButton mBtn;
    private int pingInterval;
    private int pingTimeout;
    private InetAddress dest;
    private CountDownTimer mTimer;
    private int passCount;
    private int failCount;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.ping_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDestAddress = (EditText) getActivity().findViewById(R.id.dest_address);
        mPingInterval = (EditText) getActivity().findViewById(R.id.ping_interval);
        mPingTimeout = (EditText) getActivity().findViewById(R.id.ping_timeout);
        mLeftTime = (TextView) getActivity().findViewById(R.id.left_time);
        mPassCount = (TextView) getActivity().findViewById(R.id.pass_count);
        mFailCount = (TextView) getActivity().findViewById(R.id.fail_count);
        mBtn = (ToggleButton) getActivity().findViewById(R.id.onoff_btn);
        mBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String hostAddress = mDestAddress.getText().toString();
                    Log.i(TAG, "Try to ping to " + hostAddress);
                    try {
                        dest = InetAddress.getByName(hostAddress);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    pingInterval = Integer.parseInt(mPingInterval.getText().toString());
                    pingTimeout = Integer.parseInt(mPingTimeout.getText().toString());
                    passCount = 0;
                    failCount = 0;
                    mPassCount.setText(String.valueOf(passCount));
                    mFailCount.setText(String.valueOf(failCount));
                    Thread thread = new Thread(pingThread);
                    thread.start();
                    mTimer = new CountDownTimer(pingInterval*1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int sec = ((int) millisUntilFinished)/1000;
                            mLeftTime.setText(String.valueOf(sec));
                        }

                        @Override
                        public void onFinish() {
                            Thread thread = new Thread(pingThread);
                            thread.start();
                            mPassCount.setText(String.valueOf(passCount));
                            mFailCount.setText(String.valueOf(failCount));
                            mTimer.start();
                        }
                    }.start();
                } else {
                    mTimer.cancel();
                    mLeftTime.setText("");
                }
            }
        });
//        View.OnClickListener mDestAddressListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDestAddress.setText("");
//            }
//        };
//        mDestAddress.setOnClickListener(mDestAddressListener);
    }

    private Runnable pingThread = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "pingThread started");
            try {
                if (dest.isReachable(pingTimeout*1000)) passCount++;
                else failCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "pingThread finished");
        }
    };
}
