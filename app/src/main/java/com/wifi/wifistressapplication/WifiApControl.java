package com.wifi.wifistressapplication;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by kirt-server on 2016/1/27.
 */
public class WifiApControl {
    private final String TAG = "WifiApControl";
    private static Method getWifiApState;
    private static Method isWifiApEnabled;
    private static Method setWifiApEnabled;
    private static Method getWifiApConfiguration;

    public final static String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public final static String EXTRA_WIFI_AP_STATE = "wifi_state";

    public final static int WIFI_AP_STATE_DISABLING = 10;
    public final static int WIFI_AP_STATE_DISABLED = 11;
    public final static int WIFI_AP_STATE_ENABLING = 12;
    public final static int WIFI_AP_STATE_ENABLED = 13;
    public final static int WIFI_AP_STATE_FAILED = 14;

    static {
        // lookup methods and fields not defined publicly in the SDK
        Class<?> cls = WifiManager.class;
        for (Method method : cls.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.equals("getWifiApState")) {
                getWifiApState = method;
            } else if (methodName.equals("isWifiApEnabled")) {
                isWifiApEnabled = method;
            } else if (methodName.equals("setWifiApEnabled")) {
                setWifiApEnabled = method;
            } else if (methodName.equals("getWifiApConfiguration")) {
                getWifiApConfiguration = method;
            }
        }
    }

    public static boolean isApSupported() {
        return (getWifiApState != null && isWifiApEnabled != null &&
            setWifiApEnabled != null && getWifiApConfiguration != null);
    }

    private WifiManager mWifiManager;

    private WifiApControl(WifiManager mgr) {
        this.mWifiManager = mgr;
    }

    public static WifiApControl getApControl(WifiManager mgr) {
        if (!isApSupported())
            return null;
        return new WifiApControl(mgr);
    }

    public boolean isWifiApEnabled() {
        try {
            return (Boolean) isWifiApEnabled.invoke(mWifiManager);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getWifiApState() {
        try {
            return (Integer) getWifiApState.invoke(mWifiManager);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return WIFI_AP_STATE_FAILED;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return WIFI_AP_STATE_FAILED;
        }
    }

    public WifiConfiguration getWifiApConfiguration() {
        try {
            return (WifiConfiguration) getWifiApConfiguration.invoke(mWifiManager);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        try {
            return (Boolean) setWifiApEnabled.invoke(mWifiManager, config, enabled);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
}
