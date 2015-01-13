package com.mapbox.mapboxsdk.tileprovider.util;

public final class LowMemoryException extends Exception {
    private static final long serialVersionUID = 146526524087765134L;

    public LowMemoryException(final String pDetailMessage) {
        super(pDetailMessage);
    }

    public LowMemoryException(final Throwable pThrowable) {
        super(pThrowable);
    }
}
