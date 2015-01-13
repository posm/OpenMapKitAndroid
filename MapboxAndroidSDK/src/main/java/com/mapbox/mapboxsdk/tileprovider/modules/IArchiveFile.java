package com.mapbox.mapboxsdk.tileprovider.modules;

import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import java.io.InputStream;

public interface IArchiveFile {

    /**
     * Get the input stream for the requested tile.
     *
     * @return the input stream, or null if the archive doesn't contain an entry for the requested
     * tile
     */
    InputStream getInputStream(ITileLayer tileSource, MapTile tile);
}
