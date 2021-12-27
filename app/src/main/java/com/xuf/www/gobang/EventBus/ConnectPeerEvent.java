package com.xuf.www.gobang.EventBus;

import android.bluetooth.BluetoothDevice;

import com.xuf.www.gobang.bean.Device;

/**
 * Created by Administrator on 2016/1/25.
 */
public class ConnectPeerEvent {

    public Device mDevice;

    public ConnectPeerEvent(Device device) {
        mDevice = device;
    }
}
