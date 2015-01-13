package com.mapbox.mapboxsdk.tileprovider.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.mapbox.mapboxsdk.tileprovider.IRegisterReceiver;

public class SimpleRegisterReceiver implements IRegisterReceiver {

    private final Context mContext;

    public SimpleRegisterReceiver(final Context pContext) {
        super();
        mContext = pContext;
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver aReceiver, final IntentFilter aFilter) {
        return mContext.registerReceiver(aReceiver, aFilter);
    }

    @Override
    public void unregisterReceiver(final BroadcastReceiver aReceiver) {
        mContext.unregisterReceiver(aReceiver);
    }
}
