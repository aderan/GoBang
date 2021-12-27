package com.xuf.www.gobang.interator;

import android.content.Context;
import android.provider.Settings;

public class UserManager {
    // for test only
    public static Context context;

    // return user id, here is demo workaround
    public static String getUserID() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
