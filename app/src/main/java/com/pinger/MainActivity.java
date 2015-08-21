package com.pinger;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private DatabaseHandler dbh;

    private final long LOCATION_REFRESH_TIME = 1000;
    private final float LOCATION_REFRESH_DISTANCE = 2;

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

    private Date time;

    private Intent batteryStatus;
    private boolean isCharging;
    private float batteryPct;

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

    private static NetworkInfo networkInfo;
    private boolean isConnected;
    private String netType;
    private String ssid;
    private String ipaddress;

    private Entry latest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Elements
        timeVal = (TextView) findViewById(R.id.text_timeval);
        longVal = (TextView) findViewById(R.id.text_longval);
        latVal = (TextView) findViewById(R.id.text_latval);
        battVal = (TextView) findViewById(R.id.text_battval);
        chargVal = (TextView) findViewById(R.id.text_chargingval);
        netConnVal = (TextView) findViewById(R.id.text_connval);
        netTypeVal = (TextView) findViewById(R.id.text_ntypeval);
        ssidVal = (TextView) findViewById(R.id.text_ssidval);
        ipVal = (TextView) findViewById(R.id.text_ipval);
        countText = (TextView) findViewById(R.id.text_count);

        // Location tools
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

        // Data containers
        latest = null;

    }

    /**
     * Ping button was pushed.
     * @param view
     */
    public void ping(View view)
    {
        Toast.makeText(this, "Ping!", Toast.LENGTH_SHORT).show();
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
        batteryStatus = this.registerReceiver(null, iFilter);

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
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                    WifiManager wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
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
    // End
}
