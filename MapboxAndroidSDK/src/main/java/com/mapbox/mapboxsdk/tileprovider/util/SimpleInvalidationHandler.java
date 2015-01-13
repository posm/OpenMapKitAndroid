package com.mapbox.mapboxsdk.tileprovider.util;

import android.os.Handler;
import android.os.Message;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.views.MapView;

public class SimpleInvalidationHandler extends Handler {
    private final MapView mView;

    public SimpleInvalidationHandler(final MapView pView) {
        super();
        mView = pView;
    }

    @Override
    public void handleMessage(final Message msg) {
        switch (msg.what) {
            case MapTile.MAPTILE_SUCCESS_ID:
                mView.invalidate();
                //                mView.invalidate((Rect) msg.obj);
                break;
        }
    }
}
