package com.spatialdev.osm.renderer;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.Node;
import com.spatialdev.osm.model.Way;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class OSMLine extends OSMPath {

    /**
     * This should only be constructed by
     * OSMPath.createOSMPath
     * * * *
     * @param w
     */
    protected OSMLine(Way w, MapView mv) {
        this(w, mv, Color.BLUE, 10.0f);
    }

    protected OSMLine(Way w, MapView mv, int color, float width) {
        super(w, mv);
        paint.setColor(color);
        paint.setAntiAlias(true);
        setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
    }

    /**
     * For lines, we only want to draw paths if they are inside of the viewport.
     * So, if the path is inside the viewport, we should either start the drawing
     * point (moveTo), or connect the drawing point (lineTo).
     * 
     * If it is outside of the viewport, we should do nothing but tell the canvas
     * that it should start a new line (moveTo) next time it reenters the viewport.
     * * * * * * * *
     * @param path
     * @param projectedPoint
     * @param screenPoint
     */
    @Override
    void clipOrDrawPath(Path path, double[] projectedPoint, double[] screenPoint) {

        int projX = (int) projectedPoint[0];
        int projY = (int) projectedPoint[1];
        
        if (viewPortBounds.contains(projX, projY)) {
            if (pathMoveToPlaced) {
                path.lineTo( (float) screenPoint[0], (float) screenPoint[1] );
            } else {
                path.moveTo( (float) screenPoint[0], (float) screenPoint[1] );
                pathMoveToPlaced = true;
            }
        } else {
            pathMoveToPlaced = false;
        }
    }
}
