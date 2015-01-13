/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 2/5/14 at 8:02 PM
 */

package com.mapbox.mapboxsdk.views.util;

public interface OnMapOrientationChangeListener {
    /**
     * @param angle the new map orientation angle
     */
    public void onMapOrientationChange(float angle);
}
