package com.xuf.www.gobang.interator;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.FrequencyEventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WhiteDisplayerState;
import com.peak.salut.SalutDevice;
import com.xuf.www.gobang.bean.Message;
import com.xuf.www.gobang.presenter.INetInteratorCallback;
import com.xuf.www.gobang.util.Constants;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Xuf on 2016/1/23.
 */
public class WhiteboardInteractor extends NetInteractor {
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = "WifiInteractor";

    private INetInteratorCallback mCallback;

    private Context mContext;
    private WhiteboardView mWhiteboard;
    private WhiteSdk mWhiteSdk;
    private Room mRoom;
    private RoomPhase mRoomPhase;
    private GameGlobalState mGameState = new GameGlobalState();

    private boolean isHost = false;

    public WhiteboardInteractor(Context context, WhiteboardView whiteboard, INetInteratorCallback callback) {
        mContext = context;
        mCallback = callback;
        mWhiteboard = whiteboard;
    }

    public boolean init() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(Constants.SIMPLE_APP_ID, true);
        mWhiteSdk = new WhiteSdk(mWhiteboard, mContext, configuration);
        WhiteDisplayerState.setCustomGlobalStateClass(GameGlobalState.class);
        return true;
    }

    public void unInit() {

    }

    public void startNetService() {
        RoomParams roomParams = new RoomParams(Constants.SIMPLE_ROOM_UUID,
                Constants.SIMPLE_ROOM_TOKEN,
                Constants.SIMPLE_UID_HOST);

        mWhiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                super.onPhaseChanged(phase);
                mRoomPhase = phase;
                updateHost();
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                super.onRoomStateChanged(modifyState);
                if (modifyState.getGlobalState() != null) {
                    mGameState = (GameGlobalState) modifyState.getGlobalState();
                    if (!isEmpty(mGameState.playerClient)) {
                        mCallback.onWifiDeviceConnected(null);
                    }
                }
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mRoom = room;
                updateHost();
                mRoom.addHighFrequencyEventListener("Game", new FrequencyEventListener() {
                    @Override
                    public void onEvent(EventEntry[] events) {
                        for (EventEntry event : events) {
                            Map<String, Object> map = (Map<String, Object>) event.getPayload();
                            GameEvent gameEvent = new GameEvent((boolean) map.get("toHost"), (String) map.get("data"));
                            if (gameEvent.toHost) {
                                mCallback.onDataReceived(gameEvent.data);
                            }
                        }
                    }
                }, 1000);
            }

            @Override
            public void catchEx(SDKError t) {
                mCallback.onStartWifiServiceFailed();
            }
        });
    }

    private boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    private void updateHost() {
        if (mRoomPhase != null && mRoomPhase == RoomPhase.connected && mRoom != null) {
            GameGlobalState gameState = (GameGlobalState) mRoom.getRoomState().getGlobalState();
            gameState.playerHost = Constants.SIMPLE_UID_HOST;
            gameState.playerClient = "";
            mRoom.setGlobalState(gameState);
        }
    }

    private void updateClient() {
        if (mRoomPhase != null && mRoomPhase == RoomPhase.connected && mRoom != null) {
            GameGlobalState gameState = (GameGlobalState) mRoom.getRoomState().getGlobalState();
            gameState.playerClient = Constants.SIMPLE_UID_Client;
            mRoom.setGlobalState(gameState);
        }
    }


    public void stopNetService() {
        mRoom.disconnect();
    }

    public void findPeers() {
        RoomParams roomParams = new RoomParams(Constants.SIMPLE_ROOM_UUID,
                Constants.SIMPLE_ROOM_TOKEN,
                Constants.SIMPLE_UID_Client);

        mWhiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                super.onPhaseChanged(phase);
                mRoomPhase = phase;
                updateClient();
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                super.onRoomStateChanged(modifyState);
                if (modifyState.getGlobalState() != null) {
                    mGameState = (GameGlobalState) modifyState.getGlobalState();
                }
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mRoom = room;
                updateClient();
                mRoom.addHighFrequencyEventListener("Game", new FrequencyEventListener() {
                    @Override
                    public void onEvent(EventEntry[] events) {
                        for (EventEntry event : events) {
                            Map<String, Object> map = (Map<String, Object>) event.getPayload();
                            GameEvent gameEvent = new GameEvent((boolean) map.get("toHost"), (String) map.get("data"));
                            if (!gameEvent.toHost) {
                                mCallback.onDataReceived(gameEvent.data);
                            }
                        }
                    }
                }, 1000);
            }

            @Override
            public void catchEx(SDKError t) {
                mCallback.onPeersNotFound();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    if (mGameState.playerHost != null) {
                        SalutDevice device = new SalutDevice();
                        device.deviceName = "Host";

                        post(() -> mCallback.onFindWifiPeers(Collections.singletonList(device)));
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                post(() -> mCallback.onPeersNotFound());
            }
        }).start();
    }

    private void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void connectToHost(SalutDevice salutHost, BluetoothDevice blueToothHost) {
        Log.i(TAG, "registerWithHost, registered success");
//        mCallback.onWifiDeviceConnected(salutHost);
//        mSendToDevice = salutHost;
//        mSalut.registerWithHost(salutHost, new SalutCallback() {
//            @Override
//            public void call() {
//                Log.i(TAG, "registerWithHost, registered success");
//            }
//        }, new SalutCallback() {
//            @Override
//            public void call() {
//                Log.i(TAG, "registerWithHost, registered failed");
//            }
//        });
    }

    public void sendToDevice(Message message, boolean isHost) {
        try {
            String data = LoganSquare.serialize(message);
            mRoom.dispatchMagixEvent(new AkkoEvent("Game", new GameEvent(!isHost, data)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GameGlobalState extends GlobalState {
        public String playerHost;
        public String playerClient;
    }

    class GameEvent {
        public boolean toHost;
        public String data;

        public GameEvent(boolean isHost, String data) {
            this.toHost = isHost;
            this.data = data;
        }
    }
}
