package org.redcross.openmapkit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.OSMMap;
import com.spatialdev.osm.events.OSMSelectionListener;
import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.odkcollect.ODKCollectTagActivity;

import java.io.File;
import java.util.LinkedList;

public class MapActivity extends ActionBarActivity implements OSMSelectionListener {

    private MapView mapView;
    private OSMMap osmMap;
    private Button tagsButton;

    /**
     * intent request codes
     */
    private static final int ODK_COLLECT_TAG_ACTIVITY_CODE = 2015;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Register the intent to the ODKCollect handler
        // This will determine if we are in ODK Collect Mode or not.
        ODKCollectHandler.registerIntent(getIntent());

        //set layout
        setContentView(R.layout.activity_map);

        //get map from layout
        mapView = (MapView)findViewById(R.id.mapView);

        //add map data based on connectivity status
        boolean deviceIsConnected = Connectivity.isConnected(getApplicationContext());

        if (deviceIsConnected) {
            addOnlineDataSources();

        } else {
            addOfflineDataSources();
        }

        //add user location toggle button
        initializeLocationButton();

        //add tags button
        tagsButton = (Button) findViewById(R.id.tagsButton);
        initializeTagsButton();

        //set default map extent and zoom
        LatLng initialCoordinate = new LatLng(23.707873, 90.409774);
        mapView.setCenter(initialCoordinate);
        mapView.setZoom(19);
        mapView.setMaxZoomLevel(21);
    }

    /**
     * For adding data to map when online
     */
    private void addOnlineDataSources() {

        //create OSM tile layer
        String defaultTilePID = getString(R.string.defaultTileLayerPID);
        String defaultTileURL = getString(R.string.defaultTileLayerURL);
        String defaultTileName = getString(R.string.defaultTileLayerName);
        String defaultTileAttribution = getString(R.string.defaultTileLayerAttribution);

        WebSourceTileLayer ws = new WebSourceTileLayer(defaultTilePID, defaultTileURL);
        ws.setName(defaultTileName).setAttribution(defaultTileAttribution);

        //add OSM tile layer to map
        mapView.setTileSource(ws);

        //add osm xml from assets //TODO - this is a placeholder - fetch from Open Mapkit Server over network
        initializeOsmXml();
    }

    /**
     * For instantiating a map (when the device is offline) and initializing the default mbtiles layer, extent, and zoom level
     */
    private void addOfflineDataSources() {

        String fileName = "dhaka2015-01-02.mbtiles"; //TODO - allow user to declare in UI somehow

        String filePath = Environment.getExternalStorageDirectory() + "/" + getString(R.string.appFolderName) + "/" + getString(R.string.mbtilesFolderName) + "/";

        if(ExternalStorage.isReadable()) {

            //fetch mbtiles from application folder (e.g. openmapkit/mbtiles)
            File targetMBTiles = ExternalStorage.fetchFileFromExternalStorage(filePath + fileName);

            if(!targetMBTiles.exists()) {

                //inform user if no mbtiles was found
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Device is Offline");
                builder.setMessage("Please add mbtiles to ExternalStorage" + "/" + getString(R.string.appFolderName) + "/" + getString(R.string.mbtilesFolderName) + "/");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //placeholder
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {

                //add mbtiles to map
                mapView.setTileSource(new MBTilesLayer(targetMBTiles));
            }

        }

        //add osm xml from assets //TODO - this is a placeholder - fetch from OSM XML from External storage too
        initializeOsmXml();
    }

    /**
     * Loads OSM XML stored on the device.
     */
    private void initializeOsmXml() {
        try {
            OSMMapBuilder.buildMapFromExternalStorage(this);
            
//            OSMDataSet ds = OSMXmlParser.parseFromAssets(this, "osm/dhaka_roads_buildings_hospitals_med.osm");
//            JTSModel jtsModel = new JTSModel(ds);
//            osmMap = new OSMMap(mapView, jtsModel, this);
            
            // Uncomment this block to test drawing things 
            // using the highly problematic but simple PathOverlay.
//            ArrayList<Object> uiObjects = OSMUtil.createUIObjectsFromDataSet(ds);
//
//            for (Object obj : uiObjects) {
//                if (obj instanceof Marker) {
//                    mapView.addMarker((Marker) obj);
//                } else if (obj instanceof PathOverlay) {
//                    List<Overlay> overlays = mapView.getOverlays();
//                    overlays.add((PathOverlay) obj);
//                }
//            }
//            if (uiObjects.size() > 0) {
//                mapView.invalidate();
//            }
            
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

                if (userLocationIsEnabled) {
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
                if ( ODKCollectHandler.isStandaloneMode() ) {
                    showAlertDialog();
                } else {
                    Intent odkCollectTagIntent = new Intent(getApplicationContext(), ODKCollectTagActivity.class);
                    startActivityForResult(odkCollectTagIntent, ODK_COLLECT_TAG_ACTIVITY_CODE);
                }
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

    @Override
    public void selectedElementsChanged(LinkedList<OSMElement> selectedElements) {
        if (selectedElements != null && selectedElements.size() > 0) {
            tagsButton.setVisibility(View.VISIBLE);
        } else {
            tagsButton.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * For sending results from the 'create tag' or 'edit tag' activities back to a third party app (e.g. ODK Collect)
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == ODK_COLLECT_TAG_ACTIVITY_CODE ) {
            if(resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String osmXmlFileFullPath = extras.getString("OSM_PATH");
                String osmXmlFileName = ODKCollectHandler.getOSMFileName();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("OSM_PATH", osmXmlFileFullPath);
                resultIntent.putExtra("OSM_FILE_NAME", osmXmlFileName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }
    
    public MapView getMapView() {
        return mapView;
    }
    
}
