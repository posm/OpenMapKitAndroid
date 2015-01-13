/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 3/12/14 at 9:33 PM
 */

package com.mapbox.mapboxsdk.exceptions;

public class MissingAttributeException extends Exception {
    /**
     * Default Constructor
     *
     * @param detailMessage Information on missing attribute
     */
    public MissingAttributeException(final String detailMessage) {
        super(detailMessage);
    }
}
