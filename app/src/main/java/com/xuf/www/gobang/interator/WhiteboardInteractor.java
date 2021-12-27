package com.xuf.www.gobang.interator;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bluelinelabs.logansquare.LoganSquare;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WhiteDisplayerState;
import com.xuf.www.gobang.bean.Device;
import com.xuf.www.gobang.bean.Message;
import com.xuf.www.gobang.presenter.INetInteractorCallback;
import com.xuf.www.gobang.util.Constants;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Xuf on 2016/1/23.
 */
public class WhiteboardInteractor extends NetInteractor {
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = "WifiInteractor";

    private INetInteractorCallback mCallback;

    private Context mContext;
    private WhiteboardView mWhiteboard;
    private WhiteSdk mWhiteSdk;
    private Room mRoom;
    private RoomPhase mRoomPhase;
    private GameGlobalState mGameState = new GameGlobalState();

    private String userId;

    public WhiteboardInteractor(Context context, WhiteboardView whiteboard, INetInteractorCallback callback) {
        mContext = context;
        mCallback = callback;
        mWhiteboard = whiteboard;
    }

    public void init() {
        userId = UserManager.getUserID();

        WhiteSdkConfiguration conf = new WhiteSdkConfiguration(Constants.SIMPLE_APP_ID, true);
        mWhiteSdk = new WhiteSdk(mWhiteboard, mContext, conf);
        WhiteDisplayerState.setCustomGlobalStateClass(GameGlobalState.class);

        RoomParams roomParams = new RoomParams(
                Constants.SIMPLE_ROOM_UUID,
                Constants.SIMPLE_ROOM_TOKEN,
                userId
        );

        mWhiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                super.onPhaseChanged(phase);
                mRoomPhase = phase;
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                super.onRoomStateChanged(modifyState);
                if (modifyState.getGlobalState() != null) {
                    updateGameState();
                }
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mRoom = room;
                mRoom.addHighFrequencyEventListener(EVENT_NAME, events -> {
                    for (EventEntry event : events) {
                        Map<String, Object> map = (Map<String, Object>) event.getPayload();
                        GameEvent gameEvent = new GameEvent((String) map.get("uid"), (String) map.get("data"));
                        if (!userId.equals(gameEvent.uid)) {
                            mCallback.onDataReceived(gameEvent.data);
                        }
                    }
                }, 1000);
                updateGameState();
                mCallback.onInitSuccess();
            }

            @Override
            public void catchEx(SDKError t) {
                mCallback.onInitFailed();
            }
        });
    }

    private void updateGameState() {
        mGameState = (GameGlobalState) mRoom.getGlobalState();
        if (userId.equals(mGameState.playerHost)) {
            if (mGameState.state == GameGlobalState.STATE_PAIRED) {
                mCallback.onDeviceConnected(null);
            }
        }
    }

    public void unInit() {
        mWhiteboard.removeAllViews();
        mWhiteboard.destroy();
    }

    public void startHost() {
        if (mRoomPhase != null && mRoomPhase == RoomPhase.connected && mRoom != null) {
            GameGlobalState gameState = (GameGlobalState) mRoom.getGlobalState();

            gameState.state = GameGlobalState.STATE_PAIRING;
            gameState.playerHost = userId;
            gameState.playerClient = "";
            mRoom.setGlobalState(gameState);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    private void connectHost() {
        if (mRoomPhase != null && mRoomPhase == RoomPhase.connected && mRoom != null) {
            GameGlobalState gameState = (GameGlobalState) mRoom.getRoomState().getGlobalState();
            gameState.state = GameGlobalState.STATE_PAIRED;
            gameState.playerClient = userId;
            mRoom.setGlobalState(gameState);
        }
    }

    public void stopHost() {
        mRoom.disconnect();
    }

    public void findPeers() {
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                if (mGameState.state == GameGlobalState.STATE_PAIRING) {
                    Device device = new Device();
                    device.name = mGameState.playerHost;
                    device.uuid = mGameState.playerHost;

                    post(() -> mCallback.onFindPeers(Collections.singletonList(device)));
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            post(() -> mCallback.onPeersNotFound());
        }).start();
    }

    private void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void connectToHost(Device host) {
        connectHost();
    }

    public void sendToDevice(Message message) {
        try {
            String data = LoganSquare.serialize(message);
            mRoom.dispatchMagixEvent(new AkkoEvent(EVENT_NAME, new GameEvent(userId, data)));
        } catch (Exception ignore) {
        }
    }

    class GameGlobalState extends GlobalState {
        public static final int STATE_IDLE = 0;
        public static final int STATE_PAIRING = 1;
        public static final int STATE_PAIRED = 2;

        public int state;
        public String playerHost;
        public String playerClient;
    }

    private static final String EVENT_NAME = "Game";

    class GameEvent {
        public String uid;
        public String data;

        public GameEvent(String uid, String data) {
            this.uid = uid;
            this.data = data;
        }
    }
}
