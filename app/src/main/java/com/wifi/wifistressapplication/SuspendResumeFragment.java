package com.wifi.wifistressapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by kirt-server on 2016/1/22.
 */
public class SuspendResumeFragment extends Fragment {
    private final String TAG = "SuspendResumeFragment";
    private EditText mOnIntervals;
    private EditText mOffIntervals;
    private ToggleButton mBtn;
    private IntentFilter mScreenStateFilter;
    private IntentFilter mAlarmFilter;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock wakeLock;
    private int onTime;
    private int offTime;
    private CountDownTimer mTimer;
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;
    private final String ALARM_ACTION = "com.wifi.action.alarm";
    private TextView mLeftTime;
    private int mScreenOffTimeout;

    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                handleScreenStateChanged(true);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                handleScreenStateChanged(false);
            }
        }
    };

    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ALARM_ACTION.equals(action)) {
                Log.i(TAG, "WakeLock acquired");
                if (!wakeLock.isHeld())
                    wakeLock.acquire();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.suspendresume_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOnIntervals = (EditText) getActivity().findViewById(R.id.on_intervals);
        mOffIntervals = (EditText) getActivity().findViewById(R.id.off_intervals);
        mBtn = (ToggleButton) getActivity().findViewById(R.id.onoff_btn);
        mScreenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        mScreenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mAlarmFilter = new IntentFilter(ALARM_ACTION);
        mLeftTime = (TextView) getActivity().findViewById(R.id.left_time);

        mPowerManager = (PowerManager) getActivity().getSystemService(getActivity().POWER_SERVICE);
        wakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ScreenLock");

        mAlarmManager = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(ALARM_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(getActivity())) {
            Log.d(TAG, "Send intent to grant");
            Intent grantIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(grantIntent);
        }

        try {
            mScreenOffTimeout = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        mBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Log.i(TAG, "Suspend/Resume test started");
                    onTime = Integer.parseInt(mOnIntervals.getText().toString());
                    offTime = Integer.parseInt(mOffIntervals.getText().toString());
                    getActivity().registerReceiver(mScreenStateReceiver, mScreenStateFilter);
                    getActivity().registerReceiver(mAlarmReceiver, mAlarmFilter);
                    if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(getActivity())) {
                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);
                        wakeLock.acquire();
                        mTimer = new CountDownTimer(onTime*1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                int sec = (int) millisUntilFinished / 1000;
                                mLeftTime.setText(String.valueOf(sec));
                            }

                            @Override
                            public void onFinish() {
                                if (wakeLock.isHeld())
                                    wakeLock.release();
                            }
                        }.start();
                    } else {
                        Toast.makeText(getContext(), "Turn off screen to start the test!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // The toggle is disabled
                    Log.i(TAG, "Suspend/Resume test stopped");
                    getActivity().unregisterReceiver(mAlarmReceiver);
                    getActivity().unregisterReceiver(mScreenStateReceiver);
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    mTimer.cancel();
                    if (Build.VERSION.SDK_INT < 23  || Settings.System.canWrite(getActivity())) {
                        mLeftTime.setText("");
                        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, mScreenOffTimeout);
                    } else {
                        Toast.makeText(getContext(), "Test stopped!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()...");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()...");
    }

    public void handleScreenStateChanged(boolean state) {
        if (state) {
            Log.i(TAG, "Screen on received, screen off after " + onTime + " seconds!");
            // In Screen On State
            mAlarmManager.cancel(mPendingIntent);
            mTimer = new CountDownTimer(onTime*1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(getActivity())) {
                        int sec = (int) millisUntilFinished / 1000;
                        mLeftTime.setText(String.valueOf(sec));
                    }
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "WakeLock release");
                    if (wakeLock.isHeld())
                        wakeLock.release();
                }
            }.start();
        } else {
            Log.i(TAG, "Screen off received, screen on after " + offTime + " seconds!");
            // In Screen Off State
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + offTime * 1000, mPendingIntent);
        }
    }
}