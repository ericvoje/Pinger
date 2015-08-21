package com.pinger;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

/**
 * A placeholder fragment containing a simple view.
 */
public class PingActivityFragment extends Fragment {

    private TextView timeVal;
    private TextView longVal;
    private TextView latVal;
    private TextView battVal;
    private TextView chargVal;
    private TextView netConnVal;
    private TextView netTypeVal;
    private TextView ssidVal;
    private TextView ipVal;
    private TextView countText;

    private Intent batteryStatus;
    private boolean isCharging;
    private float batteryPct;

    private static NetworkInfo networkInfo;
    private boolean isConnected;
    private String netType;
    private String ssid;
    private String ipaddress;

    private Entry latest;

    private LocationManager mLocationManager;
    private Location loc;
    private boolean loc_avail = false;

    private final LocationListener mLocationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            //your code here
            loc = location;
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        public void onProviderEnabled(String provider)
        {
            loc_avail = true;
        }

        public void onProviderDisabled(String provider)
        {
            loc_avail = false;
        }
    };

    private ViewGroup container;

    public PingActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.container = container;

        // UI Elements
        timeVal = (TextView) container.findViewById(R.id.text_timeval);
        longVal = (TextView) container.findViewById(R.id.text_longval);
        latVal = (TextView) container.findViewById(R.id.text_latval);
        battVal = (TextView) container.findViewById(R.id.text_battval);
        chargVal = (TextView) container.findViewById(R.id.text_chargingval);
        netConnVal = (TextView) container.findViewById(R.id.text_connval);
        netTypeVal = (TextView) container.findViewById(R.id.text_ntypeval);
        ssidVal = (TextView) container.findViewById(R.id.text_ssidval);
        ipVal = (TextView) container.findViewById(R.id.text_ipval);
        countText = (TextView) container.findViewById(R.id.text_count);

        return inflater.inflate(R.layout.fragment_ping, container, false);
    }

    /**
     * Ping button was pushed.
     * @param view
     */
    public void ping(View view)
    {
        Toast.makeText(view.getContext(), "Ping!", Toast.LENGTH_SHORT).show();
        getBatteryInfo();
        getNetInfo();
        latest = new Entry(loc, batteryPct, isCharging, isConnected, netType, ssid, ipaddress);
        updateView();
    }

    /**
     * Collect Battery information
     */
    private void getBatteryInfo()
    {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = container.getContext().registerReceiver(null, iFilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryPct = level / (float)scale;

    }

    /**
     * Collect network information
     */
    private void getNetInfo()
    {
        // Network Tools
        ConnectivityManager cm = (ConnectivityManager) container.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = cm.getActiveNetworkInfo();

        isConnected = networkInfo != null && networkInfo.isConnected();
        ssid = "[null]";
        ipaddress = "[null]";

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipaddress = inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }

        if(!isConnected)
        {
            netType = "None";
        }
        else {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    netType = "WiFi";
                    WifiManager wifiManager = (WifiManager) container.getContext().getSystemService(container.getContext().WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    ssid = wifiInfo.getSSID();
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    netType = "Network";
                    break;
                default:
                    netType = "Unknown [" + networkInfo.getType() + "]";
                    break;
            }
        }
    }

    /**
     * Update TextView containers with latest info
     */
    private void updateView()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SS");

        if(latest == null)
        {
            timeVal.setText("");
            longVal.setText("");
            latVal.setText("");
            battVal.setText("");
            chargVal.setText("");
            netConnVal.setText("");
            netTypeVal.setText("");
            ssidVal.setText("");
            ipVal.setText("");
            countText.setText("");
        } else {
            timeVal.setText(sdf.format(latest.getTime()));
            longVal.setText(latest.getLocation().getLongitude() + "");
            latVal.setText(latest.getLocation().getLatitude() + "");
            battVal.setText(latest.getCharge() + "");
            chargVal.setText(latest.isCharging() ? "True" : "False");
            netConnVal.setText(latest.isConnected() ? "True" : "False");
            netTypeVal.setText(latest.getNetworkType());
            ssidVal.setText(latest.getSsid());
            ipVal.setText(latest.getIpaddr());
            countText.setText("");
        }
    }

}
