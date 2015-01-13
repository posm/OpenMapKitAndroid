/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 2/5/14 at 8:02 PM
 */

package com.mapbox.mapboxsdk.views.util;

import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public interface TileLoadedListener {
    public CacheableBitmapDrawable onTileLoaded(CacheableBitmapDrawable pDrawable);
}
