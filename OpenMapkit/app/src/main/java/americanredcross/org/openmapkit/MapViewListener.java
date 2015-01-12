package americanredcross.org.openmapkit;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;

public class MapViewListener implements com.mapbox.mapboxsdk.views.MapViewListener {

    Activity mActivity;

    public MapViewListener(Activity activity) {

        mActivity = activity;
    }

    @Override
    public void onShowMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onHideMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onTapMarker(MapView pMapView, Marker pMarker) {


    }

    @Override
    public void onLongPressMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onTapMap(MapView pMapView, ILatLng pPosition) {

        //TODO - call spatial intersection operation based on user tap

        //TODO - after spatial intersection prompt user to create or edit a tag for the tapped feature

        //PLACEHOLER - prompt user if they want to edit or create an OSM tag
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setTitle("Choose Option");

        builder.setItems(R.array.editoptions, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public void onLongPressMap(MapView pMapView, ILatLng pPosition) {

    }
}
