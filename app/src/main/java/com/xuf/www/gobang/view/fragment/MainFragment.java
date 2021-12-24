package com.xuf.www.gobang.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gc.materialdesign.views.ButtonRectangle;
import com.xuf.www.gobang.util.Constants;
import com.xuf.www.gobang.R;
import com.xuf.www.gobang.view.activity.GameActivity;

/**
 * Created by Administrator on 2015/12/9.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initView(view);

        return view;
    }

    private void initView(View root){
        ButtonRectangle coupeTextView = (ButtonRectangle) root.findViewById(R.id.tv_coupe_mode);
        ButtonRectangle wifiTextView = (ButtonRectangle) root.findViewById(R.id.tv_wifi_mode);
        ButtonRectangle blueToothTextView = (ButtonRectangle) root.findViewById(R.id.tv_blue_tooth_mode);

        coupeTextView.setOnClickListener(this);
        wifiTextView.setOnClickListener(this);
        blueToothTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), GameActivity.class);
        switch (v.getId()){
            case R.id.tv_coupe_mode:
                intent.putExtra(Constants.GAME_MODE, Constants.COUPE_MODE);
                break;
            case R.id.tv_wifi_mode:
                intent.putExtra(Constants.GAME_MODE, Constants.WIFI_MODE);
                break;
            case R.id.tv_blue_tooth_mode:
                intent.putExtra(Constants.GAME_MODE, Constants.BLUE_TOOTH_MODE);
                break;
        }
        startActivity(intent);
    }
}
