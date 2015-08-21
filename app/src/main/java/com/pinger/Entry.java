package com.pinger;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Eric on 7/31/2015.
 */
public class Entry {

    private Date time;
    private Location location;
    private float charge;
    private boolean isCharging;
    private boolean isConnected;
    private String networkType;
    private String ssid;
    private String ipaddr;

    /**
     * Creates an Entry type given all the info
     * @param time
     * @param loc
     * @param charge
     * @param isCharging
     * @param isConnected
     * @param networkType
     * @param ssid
     * @param ipaddr
     */
    public Entry(Date time, Location loc, float charge, boolean isCharging, boolean isConnected, String networkType, String ssid, String ipaddr)
    {
        this.time = time;
        this.location = loc;
        this.charge = charge;
        this.isCharging = isCharging;
        this.isConnected = isConnected;
        this.networkType = networkType;
        this.ssid = ssid;
        this.ipaddr = ipaddr;
    }

    /**
     * Creates a new Entry with the current time
     * @param loc
     * @param charge
     * @param isCharging
     * @param isConnected
     * @param networkType
     * @param ssid
     * @param ipaddr
     */
    public Entry(Location loc, float charge, boolean isCharging, boolean isConnected, String networkType, String ssid, String ipaddr)
    {
        this.time = new Date();
        this.location = loc;
        this.charge = charge;
        this.isCharging = isCharging;
        this.isConnected = isConnected;
        this.networkType = networkType;
        this.ssid = ssid;
        this.ipaddr = ipaddr;
    }

    public Date getTime() {
        return time;
    }

    public Location getLocation() {
        return location;
    }

    public float getCharge() {
        return charge;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getSsid() {
        return ssid;
    }

    public String getIpaddr() {
        return ipaddr;
    }
}
