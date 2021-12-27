package com.xuf.www.gobang.view.activity;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.xuf.www.gobang.interator.UserManager;
import com.xuf.www.gobang.util.Constants;
import com.xuf.www.gobang.view.fragment.CoupleGameFragment;
import com.xuf.www.gobang.view.fragment.NetGameFragment;

/**
 * Created by Administrator on 2015/12/8.
 */
public class GameActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserManager.context = this;
    }

    @Override
    protected Fragment createFragment() {
        int gameMode = getIntent().getIntExtra(Constants.GAME_MODE, Constants.INVALID_MODE);
        Fragment fragment = null;

        switch (gameMode) {
            case Constants.INVALID_MODE:
                break;
            case Constants.COUPE_MODE:
                fragment = new CoupleGameFragment();
                break;
            case Constants.Net_MODE:
                fragment = NetGameFragment.newInstance();
                break;
        }

        return fragment;
    }

    @Override
    public void onBackPressed() {

    }
}
