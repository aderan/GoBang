package com.xuf.www.gobang.view.dialog;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xuf.www.gobang.EventBus.BusProvider;
import com.xuf.www.gobang.EventBus.ConnectPeerEvent;
import com.xuf.www.gobang.EventBus.WifiCancelPeerEvent;
import com.xuf.www.gobang.R;
import com.xuf.www.gobang.bean.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenov0 on 2015/12/27.
 */
public class PeersDialog extends BaseDialog {

    public static final String TAG = "PeersDialog";

    private ListView mListView;
    private ProgressBar mProgressBar;

    private DeviceAdapter mAdapter;
    private List<Device> mDevices = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_peer_list, container, false);

        mListView = (ListView) view.findViewById(R.id.lv_peers);
        mAdapter = new DeviceAdapter();
        mListView.setAdapter(mAdapter);

        mProgressBar = view.findViewById(R.id.progressBarCircular);

        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getInstance().post(new WifiCancelPeerEvent());
                mDevices.clear();
            }
        });

        return view;
    }

    public void updateWifiPeers(List<Device> data) {
        mAdapter.setDevices(data);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private class DeviceAdapter extends BaseAdapter {
        public void setDevices(List<Device> data) {
            mDevices.clear();
            mDevices.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mDevices.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_device, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            String device = mDevices.get(position).name;
            holder = (ViewHolder) convertView.getTag();
            holder.device.setText(device);
            holder.device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BusProvider.getInstance().post(new ConnectPeerEvent(mDevices.get(position)));
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public TextView device;

            public ViewHolder(View view) {
                device = (TextView) view.findViewById(R.id.tv_device);
            }
        }
    }
}
