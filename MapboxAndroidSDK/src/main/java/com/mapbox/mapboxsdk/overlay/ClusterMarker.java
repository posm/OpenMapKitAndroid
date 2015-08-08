package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import com.mapbox.mapboxsdk.views.NumberBitmapDrawable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by nitrog42 on 09/04/15.
 */
public class ClusterMarker extends Marker {
    private List<Marker> mMarkerList = new ArrayList<>();

    public ClusterMarker() {
        super("", "", new LatLng(0, 0));
    }

    public ClusterMarker(Marker marker) {
        super("", "", new LatLng(0, 0));
        addMarkerToCluster(marker);

    }

    @Override
    public Drawable getMarker(final int stateBitset) {
        if (mMarker == null && this.context != null) {
            setDefaultClusterMarker(this.context);
        }
        return mMarker;
    }

    public void setDefaultClusterMarker(Context context) {
        BitmapFactory.Options opts = BitmapUtils.getBitmapOptions(context.getResources().getDisplayMetrics());
        NumberBitmapDrawable numberBitmapDrawable = new NumberBitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.clusteri, opts), mMarkerList.size());
        setMarker(numberBitmapDrawable, true);
    }

    public void addMarkerToCluster(Marker marker) {
        mMarkerList.add(marker);
    }

    public void addMarkersToCluster(Collection<Marker> markers) {
        mMarkerList.addAll(markers);
    }

    public void updatePosition() {
        double centroidX = 0, centroidY = 0;
        for (Marker marker1 : mMarkerList) {
            centroidX += marker1.getPoint().getLongitude();
            centroidY += marker1.getPoint().getLatitude();
        }
        centroidX /= mMarkerList.size();
        centroidY /= mMarkerList.size();
        setPoint(new LatLng(centroidY, centroidX));
    }

    public interface OnDrawClusterListener {
        /**
         * Allow to change the display of a cluster
         * Use NumberBitmapDrawable to display a bitmap with a number on it.
         *
         * @param cluster
         * @return The drawable to display
         */
        Drawable drawCluster(ClusterMarker cluster);
    }

    public List<Marker> getMarkersReadOnly() {
        return Collections.unmodifiableList(mMarkerList);
    }

    public void setClusterItemCount(int count) {
        if (mMarker instanceof NumberBitmapDrawable) {
            ((NumberBitmapDrawable) mMarker).setCount(count);
        }
    }

    public void setTextPaint(Paint paint) {
        if (mMarker instanceof NumberBitmapDrawable) {
            ((NumberBitmapDrawable) mMarker).setTextPaint(paint);
        }
    }
}