package com.xuf.www.gobang.bean;

import android.net.wifi.p2p.WifiP2pDevice;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Map;

@JsonObject
public class Device {
    @JsonField
    public String name;

    @JsonField
    public String uuid;

    public Device() {
    }

    public Device(WifiP2pDevice device, Map<String, String> txtRecord) {
        this.name = device.deviceName;
    }
}
