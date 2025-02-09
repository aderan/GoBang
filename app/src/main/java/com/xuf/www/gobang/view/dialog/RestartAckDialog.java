package com.xuf.www.gobang.view.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xuf.www.gobang.R;
import com.xuf.www.gobang.EventBus.BusProvider;
import com.xuf.www.gobang.EventBus.RestartGameAckEvent;

/**
 * Created by Administrator on 2016/1/27.
 */
public class RestartAckDialog extends BaseDialog {

    public static final String TAG = "RestartAckDialog";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_ack_restart, container, false);

        Button agree = (Button) view.findViewById(R.id.btn_agree);
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BusProvider.getInstance().post(new RestartGameAckEvent(true));
            }
        });

        Button disagree = (Button) view.findViewById(R.id.btn_disagree);
        disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BusProvider.getInstance().post(new RestartGameAckEvent(false));
            }
        });

        return view;
    }
}
