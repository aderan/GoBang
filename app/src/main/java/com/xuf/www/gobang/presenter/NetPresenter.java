package com.xuf.www.gobang.presenter;

import android.content.Context;
import android.util.Log;

import com.xuf.www.gobang.bean.Device;
import com.xuf.www.gobang.bean.Message;
import com.xuf.www.gobang.interator.WhiteboardInteractor;

import java.util.List;

/**
 * Created by Xuf on 2016/1/23.
 */
public class NetPresenter implements INetInteractorCallback {
    private INetView mNetView;
    private WhiteboardInteractor mNetInteractor;

    public NetPresenter(Context context, INetView netView) {
        mNetView = netView;
        mNetInteractor = new WhiteboardInteractor(context, mNetView.getWhiteboard(), this);
    }

    public void init() {
        mNetInteractor.init();
    }

    public void unInit() {
        mNetInteractor.unInit();
    }

    public void startService() {
        mNetInteractor.startHost();
    }

    public void stopService() {
        mNetInteractor.stopHost();
    }

    public void sendToDevice(Message message) {
        mNetInteractor.sendToDevice(message);
    }

    public void findPeers() {
        mNetInteractor.findPeers();
    }

    public void connectToHost(Device host) {
        mNetInteractor.connectToHost(host);
    }

    @Override
    public void onInitSuccess() {
        mNetView.onInitSuccess();
    }

    @Override
    public void onInitFailed() {
        mNetView.onInitFailed("");
    }

    @Override
    public void onDeviceConnected(Device device) {
        mNetView.onWifiDeviceConnected(device);
    }

    @Override
    public void onStartServiceFailed() {
        mNetView.onStartWifiServiceFailed();
    }

    @Override
    public void onFindPeers(List<Device> deviceList) {
        mNetView.onFindWifiPeers(deviceList);
    }

    @Override
    public void onPeersNotFound() {
        mNetView.onPeersNotFound();
    }

    @Override
    public void onDataReceived(Object o) {
        Log.e("Gobang", "message " + o.toString());
        mNetView.onDataReceived(o);
    }

    @Override
    public void onSendMessageFailed() {
        mNetView.onSendMessageFailed();
    }
}
