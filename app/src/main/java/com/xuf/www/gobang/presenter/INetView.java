package com.xuf.www.gobang.presenter;

import android.bluetooth.BluetoothDevice;

import com.herewhite.sdk.WhiteboardView;
import com.xuf.www.gobang.bean.Device;

import java.util.List;

/**
 * Created by Xuf on 2016/1/23.
 */
public interface INetView {
    void onInitSuccess();

    void onInitFailed(String message);

    void onWifiDeviceConnected(Device device);

    void onStartWifiServiceFailed();

    void onFindWifiPeers(List<Device> deviceList);

    void onPeersNotFound();

    void onDataReceived(Object o);

    void onSendMessageFailed();

    WhiteboardView getWhiteboard();
}
