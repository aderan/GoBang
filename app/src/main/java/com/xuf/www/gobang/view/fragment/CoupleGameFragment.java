package com.xuf.www.gobang.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xuf.www.gobang.R;
import com.xuf.www.gobang.bean.Point;
import com.xuf.www.gobang.util.GameJudger;
import com.xuf.www.gobang.util.ToastUtil;
import com.xuf.www.gobang.widget.GoBangBoard;


/**
 * Created by Xuf on 2016/2/7.
 */
public class CoupleGameFragment extends Fragment implements GoBangBoard.PutChessListener
        , View.OnClickListener
        , View.OnTouchListener {

    private boolean mIsGameStarted = false;
    private boolean mIsWhiteFirst = true;
    private boolean mCurrentWhite;

    private GoBangBoard mGoBangBoard;
    private Button mStartGame;
    private Button mStartFirst;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_couple_game, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mGoBangBoard = (GoBangBoard) view.findViewById(R.id.go_bang_board);
        mGoBangBoard.setOnTouchListener(this);
        mGoBangBoard.setPutChessListener(this);

        mCurrentWhite = mIsWhiteFirst;

        mStartGame = (Button) view.findViewById(R.id.btn_start_game);
        mStartGame.setOnClickListener(this);

        mStartFirst = (Button) view.findViewById(R.id.btn_start_first);
        mStartFirst.setOnClickListener(this);

        Button exitGame = (Button) view.findViewById(R.id.btn_exit_game);
        exitGame.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_game:
                if (!mIsGameStarted) {
                    mIsGameStarted = true;
                    mCurrentWhite = mIsWhiteFirst;
                    setWidgets();
                }
                break;
            case R.id.btn_start_first:
                mIsWhiteFirst = !mIsWhiteFirst;
                mCurrentWhite = mIsWhiteFirst;
                String buttonText;
                if (mCurrentWhite) {
                    buttonText = "白子先手";
                } else {
                    buttonText = "黑子先手";
                }
                mStartFirst.setText(buttonText);
                break;
            case R.id.btn_exit_game:
                getActivity().finish();
                break;
        }
    }

    @Override
    public void onPutChess(int[][] board, int x, int y) {
        if (GameJudger.isGameEnd(board, x, y)) {
            String msg = String.format("%s赢了", mCurrentWhite ? "白棋" : "黑棋");
            ToastUtil.showShort(getActivity(), msg);
            mIsGameStarted = false;
            resetWidgets();
        }
    }

    private void setWidgets() {
        mGoBangBoard.clearBoard();
        mStartGame.setEnabled(false);
        mStartFirst.setEnabled(false);
    }

    private void resetWidgets() {
        mStartGame.setEnabled(true);
        mStartFirst.setEnabled(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mIsGameStarted) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                Point point = mGoBangBoard.convertPoint(x, y);
                if (mGoBangBoard.putChess(mCurrentWhite, point.x, point.y)) {
                    mCurrentWhite = !mCurrentWhite;
                }
                break;
        }
        return false;
    }
}
