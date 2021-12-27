package com.xuf.www.gobang.presenter;

import com.xuf.www.gobang.bean.Device;

import java.util.List;

/**
 * Created by Xuf on 2016/1/23.
 */
public interface INetInteractorCallback {

    void onInitSuccess();

    void onInitFailed();

    void onDeviceConnected(Device device);

    void onStartServiceFailed();

    void onFindPeers(List<Device> deviceList);

    void onPeersNotFound();

    void onDataReceived(Object o);

    void onSendMessageFailed();
}
