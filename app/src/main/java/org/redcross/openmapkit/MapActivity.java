package org.redcross.openmapkit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.OSMMapListener;
import com.spatialdev.osm.OSMUtil;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.model.OSMDataSet;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMXmlParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapActivity extends ActionBarActivity {

    private MapView mapView;
    OSMMapListener osmMapListener;
    private Button tagsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout
        setContentView(R.layout.activity_map);


        //get device connectivity status
        boolean deviceIsConnected = Connectivity.isConnected(getApplicationContext());


        //initialize map based on device connectivity status
        if (deviceIsConnected) {

            initializeOnlineMap();

        } else {

            initializeOfflineMap();
        }

        //add user location toggle button
        initializeLocationButton();


        //this activity implements mapViewListener events
//        mapView.setMapViewListener(this);
    }

    /**
     * For instantiating a map (when the device is online) and initializing the default tile layer, location, extent, and zoom level
     */
    private void initializeOnlineMap() {

        mapView = (MapView)findViewById(R.id.mapView);
        tagsButton = (Button) findViewById(R.id.tagsButton);

        //set the  default map tile layer (OSM)
        String defaultTilePID = getString(R.string.defaultTileLayerPID);
        String defaultTileURL = getString(R.string.defaultTileLayerURL);
        String defaultTileName = getString(R.string.defaultTileLayerName);
        String defaultTileAttribution = getString(R.string.defaultTileLayerAttribution);

        WebSourceTileLayer ws = new WebSourceTileLayer(defaultTilePID, defaultTileURL);
        ws.setName(defaultTileName).setAttribution(defaultTileAttribution);

        mapView.setTileSource(ws);

        //set default map extent and zoom
        LatLng initialCoordinate = new LatLng(23.707873,90.409774);
        mapView.setCenter(initialCoordinate);
        mapView.setZoom(19);

        initializeOsmXml();
        initializeTagsButton();
    }

    /**
     * For instantiating a map (when the device is offline) and initializing the default mbtiles layer, extent, and zoom level
     */
    private void initializeOfflineMap() {

        /*
        //instantiate map
        this.mapView = (MapView)findViewById(R.id.mapView);


        //offline tilelayer
        TileLayer tileLayer = new MBTilesLayer("dhaka2015-01-02.mbtiles");
        mapView.setTileSource(tileLayer);


        //set default map extent and zoom
        LatLng initialCoordinate = new LatLng(23.728791, 90.409412);
        mapView.setCenter(initialCoordinate);
        mapView.setZoom(12);
        */

        //test
        Toast toast = Toast.makeText(getApplicationContext(), "Offline - load data from external storage", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Loads OSM XML stored on the device.
     */
    private void initializeOsmXml() {
        try {
            OSMDataSet ds = OSMXmlParser.parseFromAssets(this, "osm/dhaka_roads_buildings_hospitals_tiny.osm");
            JTSModel jtsModel = new JTSModel(ds);
            osmMapListener = new OSMMapListener(mapView, jtsModel);
            ArrayList<Object> uiObjects = OSMUtil.createUIObjectsFromDataSet(ds);

            for (Object obj : uiObjects) {
                if (obj instanceof Marker) {
                    mapView.addMarker((Marker) obj);
                } else if (obj instanceof PathOverlay) {
                    List<Overlay> overlays = mapView.getOverlays();
                    overlays.add((PathOverlay) obj);
                }
            }
            if (uiObjects.size() > 0) {
                mapView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * For instantiating the location button and setting up its tap event handler
     */
    private void initializeLocationButton() {

        //instantiate location button
        ImageButton locationButton = (ImageButton)findViewById(R.id.locationButton);

        //set tap event
        locationButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                boolean userLocationIsEnabled = mapView.getUserLocationEnabled();

                if(userLocationIsEnabled) {

                    mapView.setUserLocationEnabled(false);

                } else {

                    mapView.setUserLocationEnabled(true);
                    mapView.goToUserLocation(true);
                    mapView.setUserLocationRequiredZoom(15);
                }
            }
        });
    }
    
    private void initializeTagsButton() {
        tagsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAlertDialog();
            }
        });
        
    }

    public void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.tagPromptTitle);

        builder.setItems(R.array.editoptions, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                switch(which) {
                    case 0:

                        Intent editTagIntent = new Intent(getApplicationContext(), TagEditorActivity.class);
                        startActivity(editTagIntent);
                        break;

                    case 1:

                        Intent createTagIntent = new Intent(getApplicationContext(), TagCreatorActivity.class);
                        startActivity(createTagIntent);
                        break;

                    default:

                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }


    /**
     * For handling when a user taps on a menu item (top right)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_onlinesettings) {

            //TODO

            return true;
        }
        else if (id == R.id.aciton_offlinesettings) {

            //TODO

            return true;
        }
        else if(id == R.id.action_about) {

            //TODO

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    public void selectedElementsChanged(LinkedList<OSMElement> selectedElements) {
//        tagsButton.setVisibility(View.VISIBLE);
//    }
    
}
