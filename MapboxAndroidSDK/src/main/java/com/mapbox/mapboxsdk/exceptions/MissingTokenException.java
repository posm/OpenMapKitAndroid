package com.mapbox.mapboxsdk.exceptions;

public class MissingTokenException extends Exception {

    /**
     * Default Constructor
     */
    public MissingTokenException() {
        super("An access token is required in order to use the Mapbox API. Obtain a token on your Mapbox account page at https://www.mapbox.com/account/apps/.");
    }
}
