package com.mapbox.mapboxsdk.views;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.overlay.Marker;

public interface MapViewListener {
    public void onShowMarker(final MapView pMapView, final Marker pMarker);

    public void onHideMarker(final MapView pMapView, final Marker pMarker);

    public void onTapMarker(final MapView pMapView, final Marker pMarker);

    public void onLongPressMarker(final MapView pMapView, final Marker pMarker);

    public void onTapMap(final MapView pMapView, final ILatLng pPosition);

    public void onLongPressMap(final MapView pMapView, final ILatLng pPosition);
}
