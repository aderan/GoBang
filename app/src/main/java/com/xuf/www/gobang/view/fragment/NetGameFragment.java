package com.xuf.www.gobang.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.herewhite.sdk.WhiteboardView;
import com.squareup.otto.Subscribe;
import com.xuf.www.gobang.EventBus.ConnectPeerEvent;
import com.xuf.www.gobang.EventBus.ExitGameAckEvent;
import com.xuf.www.gobang.EventBus.MoveBackAckEvent;
import com.xuf.www.gobang.EventBus.RestartGameAckEvent;
import com.xuf.www.gobang.EventBus.WifiBeginWaitingEvent;
import com.xuf.www.gobang.EventBus.WifiCancelCompositionEvent;
import com.xuf.www.gobang.EventBus.WifiCancelPeerEvent;
import com.xuf.www.gobang.EventBus.WifiCancelWaitingEvent;
import com.xuf.www.gobang.EventBus.WifiCreateGameEvent;
import com.xuf.www.gobang.EventBus.WifiJoinGameEvent;
import com.xuf.www.gobang.R;
import com.xuf.www.gobang.bean.Device;
import com.xuf.www.gobang.bean.Message;
import com.xuf.www.gobang.bean.Point;
import com.xuf.www.gobang.presenter.INetView;
import com.xuf.www.gobang.presenter.NetPresenter;
import com.xuf.www.gobang.util.GameJudger;
import com.xuf.www.gobang.util.MessageWrapper;
import com.xuf.www.gobang.util.OperationQueue;
import com.xuf.www.gobang.util.ToastUtil;
import com.xuf.www.gobang.view.dialog.DialogCenter;
import com.xuf.www.gobang.widget.GoBangBoard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xuf on 2016/1/23.
 */
public class NetGameFragment extends BaseGameFragment implements INetView, GoBangBoard.PutChessListener
        , View.OnTouchListener, View.OnClickListener {

    private static final int MOVE_BACK_TIMES = 2;

    private boolean mIsHost;
    private boolean mIsMePlay = false;
    private boolean mIsGameEnd = false;
    private boolean mIsOpponentLeaved = false;
    private boolean mCanClickConnect = true;
    private int mLeftMoveBackTimes = MOVE_BACK_TIMES;

    private OperationQueue mOperationQueue;

    private NetPresenter mNetPresenter;

    private DialogCenter mDialogCenter;
    private GoBangBoard mBoard;
    private Button mMoveBack;

    private WhiteboardView mWhiteboard;

    public static NetGameFragment newInstance() {
        NetGameFragment netGameFragment = new NetGameFragment();
        return netGameFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unInit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView(view);
        init();
        return view;
    }

    private void initView(View view) {
        mBoard = (GoBangBoard) view.findViewById(R.id.go_bang_board);
        mBoard.setOnTouchListener(this);
        mBoard.setPutChessListener(this);

        Button restart = (Button) view.findViewById(R.id.btn_restart);
        restart.setOnClickListener(this);

        Button exitGame = (Button) view.findViewById(R.id.btn_exit);
        exitGame.setOnClickListener(this);

        mMoveBack = (Button) view.findViewById(R.id.btn_move_back);
        mMoveBack.setOnClickListener(this);
        mMoveBack.setText(makeMoveBackString());

        mWhiteboard = (WhiteboardView) view.findViewById(R.id.whiteboard);
    }

    private void init() {
        mDialogCenter = new DialogCenter(getActivity());
        mDialogCenter.showCompositionDialog();
        mNetPresenter = new NetPresenter(getActivity(), this);
        mNetPresenter.init();
        mOperationQueue = new OperationQueue();
    }

    private void unInit() {
        mNetPresenter.unInit();
    }

    private void reset() {
        mBoard.clearBoard();
        mIsMePlay = mIsHost;
        mIsGameEnd = false;
        mOperationQueue.clear();
        mLeftMoveBackTimes = MOVE_BACK_TIMES;
        mMoveBack.setText(makeMoveBackString());
        mMoveBack.setEnabled(true);
    }

    private void sendMessage(Message message) {
        mNetPresenter.sendToDevice(message);
    }

    private void moveBackReq() {
        if (mIsMePlay || mIsGameEnd) {
            return;
        }
        Message message = MessageWrapper.getGameMoveBackReqMessage();
        sendMessage(message);
        mDialogCenter.showMoveBackWaitingDialog();
    }

    private void doMoveBack() {
        mOperationQueue.removeLastOperation();
        Point point = mOperationQueue.getLastOperation();
        mBoard.moveBack(point);
    }

    private String makeMoveBackString() {
        return "悔  棋" + "(" + mLeftMoveBackTimes + ")";
    }

    @Override
    public void onInitSuccess() {
        ToastUtil.showShort(getActivity(), "Init Success");
    }

    @Override
    public void onInitFailed(String message) {
        ToastUtil.showShort(getActivity(), message);
        getActivity().finish();
    }

    @Override
    public void onWifiDeviceConnected(Device device) {
        ToastUtil.showShort(getActivity(), "onWifiDeviceConnected");
        mDialogCenter.enableWaitingPlayerDialogsBegin();
    }

    @Override
    public void onStartWifiServiceFailed() {
        ToastUtil.showShort(getActivity(), "onStartWifiServiceFailed");
    }

    @Override
    public void onFindWifiPeers(List<Device> deviceList) {
        mDialogCenter.updateWifiPeers(deviceList);
    }

    @Override
    public void onPeersNotFound() {
        ToastUtil.showShort(getActivity(), "found no peers");
        mDialogCenter.updateWifiPeers(new ArrayList<Device>());
    }

    @Override
    public void onDataReceived(Object o) {
        String str = (String) o;
        try {
            Message message = LoganSquare.parse(str, Message.class);
            int type = message.mMessageType;
            switch (type) {
                case Message.MSG_TYPE_HOST_BEGIN:
                    //joiner
                    mDialogCenter.dismissPeersAndComposition();
                    Message ack = MessageWrapper.getHostBeginAckMessage();
                    sendMessage(ack);
                    ToastUtil.showShort(getActivity(), "游戏开始");
                    mCanClickConnect = true;
                    break;
                case Message.MSG_TYPE_BEGIN_ACK:
                    //host
                    mDialogCenter.dismissWaitingAndComposition();
                    mIsMePlay = true;
                    break;
                case Message.MSG_TYPE_GAME_DATA:
                    mBoard.putChess(message.mIsWhite, message.mGameData.x, message.mGameData.y);
                    mIsMePlay = true;
                    break;
                case Message.MSG_TYPE_GAME_END:
                    ToastUtil.showShortDelay(getActivity(), message.mMessage, 1000);
                    mIsMePlay = false;
                    mIsGameEnd = true;
                    break;
                case Message.MSG_TYPE_GAME_RESTART_REQ:
                    if (mIsGameEnd) {
                        Message resp = MessageWrapper.getGameRestartRespMessage(true);
                        sendMessage(resp);
                        reset();
                    } else {
                        mDialogCenter.showRestartAckDialog();
                    }
                    break;
                case Message.MSG_TYPE_GAME_RESTART_RESP:
                    if (message.mAgreeRestart) {
                        reset();
                        ToastUtil.showShort(getActivity(), "游戏开始");
                    } else {
                        ToastUtil.showShort(getActivity(), "对方不同意重新开始游戏");
                    }
                    mDialogCenter.dismissRestartWaitingDialog();
                    break;
                case Message.MSG_TYPE_MOVE_BACK_REQ:
                    if (mIsMePlay) {
                        mDialogCenter.showMoveBackAckDialog();
                    }
                    break;
                case Message.MSG_TYPE_MOVE_BACK_RESP:
                    if (message.mAgreeMoveBack) {
                        doMoveBack();
                        mIsMePlay = true;
                        mLeftMoveBackTimes--;
                        mMoveBack.setText(makeMoveBackString());
                        if (mLeftMoveBackTimes == 0) {
                            mMoveBack.setEnabled(false);
                        }
                    } else {
                        ToastUtil.showShort(getActivity(), "对方不同意你悔棋");
                    }
                    mDialogCenter.dismissMoveBackWaitingDialog();
                    break;
                case Message.MSG_TYPE_EXIT:
                    ToastUtil.showShort(getActivity(), "对方已离开游戏");
                    mIsMePlay = true;
                    mIsGameEnd = true;
                    mIsOpponentLeaved = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSendMessageFailed() {
        ToastUtil.showShort(getActivity(), "send message failed");
    }

    @Override
    public WhiteboardView getWhiteboard() {
        return mWhiteboard;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_restart:
                sendMessage(MessageWrapper.getGameRestartReqMessage());
                mDialogCenter.showRestartWaitingDialog();
                break;
            case R.id.btn_move_back:
                moveBackReq();
                break;
            case R.id.btn_exit:
                if (mIsOpponentLeaved) {
                    getActivity().finish();
                } else {
                    mDialogCenter.showExitAckDialog();
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mIsGameEnd && mIsMePlay) {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    Point point = mBoard.convertPoint(x, y);
                    if (mBoard.putChess(mIsHost, point.x, point.y)) {
                        Message data = MessageWrapper.getSendDataMessage(point, mIsHost);
                        sendMessage(data);
                        mIsMePlay = false;
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public void onPutChess(int[][] board, int x, int y) {
        if (mIsMePlay && GameJudger.isGameEnd(board, x, y)) {
            ToastUtil.showShort(getActivity(), "你赢了");
            Message end = MessageWrapper.getGameEndMessage("你输了");
            sendMessage(end);
            mIsMePlay = false;
            mIsGameEnd = true;
        }
        Point point = new Point();
        point.setXY(x, y);
        mOperationQueue.addOperation(point);
    }

    @Subscribe
    public void onCreateGame(WifiCreateGameEvent event) {
        mIsHost = true;
        mDialogCenter.showWaitingPlayerDialog();
        mNetPresenter.startService();
    }

    @Subscribe
    public void onJoinGame(WifiJoinGameEvent event) {
        mIsHost = false;
        mDialogCenter.showPeersDialog();
        mNetPresenter.findPeers();
    }

    @Subscribe
    public void onCancelCompositionDialog(WifiCancelCompositionEvent event) {
        getActivity().finish();
    }

    @Subscribe()
    public void onConnectPeer(ConnectPeerEvent event) {
        if (mCanClickConnect) {
            mNetPresenter.connectToHost(event.mDevice);
            mCanClickConnect = false;
        }
    }

    @Subscribe
    public void onCancelConnectPeer(WifiCancelPeerEvent event) {
        mDialogCenter.dismissPeersDialog();
    }

    @Subscribe
    public void onBeginGame(WifiBeginWaitingEvent event) {
        Message begin = MessageWrapper.getHostBeginMessage();
        sendMessage(begin);
    }

    @Subscribe
    public void onCancelWaitingDialog(WifiCancelWaitingEvent event) {
        mDialogCenter.dismissWaitingPlayerDialog();
        mNetPresenter.stopService();
    }

    @Subscribe
    public void onRestartAck(RestartGameAckEvent event) {
        Message ack = MessageWrapper.getGameRestartRespMessage(event.mAgreeRestart);
        sendMessage(ack);
        if (event.mAgreeRestart) {
            reset();
        }
        mDialogCenter.dismissRestartAckDialog();
    }

    @Subscribe
    public void onExitAck(ExitGameAckEvent event) {
        if (event.mExit) {
            Message ack = MessageWrapper.getGameExitMessage();
            sendMessage(ack);
            getActivity().finish();
        } else {
            mDialogCenter.dismissExitAckDialog();
        }
    }

    @Subscribe
    public void onMoveBackAck(MoveBackAckEvent event) {
        Message ack = MessageWrapper.getGameMoveBackRespMessage(event.mAgreeMoveBack);
        sendMessage(ack);
        mDialogCenter.dismissMoveBackAckDialog();
        if (event.mAgreeMoveBack) {
            doMoveBack();
            mIsMePlay = false;
        }
    }
}