package org.redcross.openmapkit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Clint Cabanero & Nicholas Hallahan on 3/17/15.
 *
 * nhallahan@spatialdev.com
 * ccabanero@spatialdev.com
 * * * * * * 
 */
public class Basemap {
    
    private static final String PREVIOUS_BASEMAP = "org.redcross.openmapkit.PREVIOUS_BASEMAP";
    
    private MapActivity mapActivity;
    private MapView mapView;
    private Context context;
    
    private static String selectedBasemap;
    
    public Basemap(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
        this.mapView = mapActivity.getMapView();
        this.context = mapActivity.getApplicationContext();

        if (selectedBasemap != null) {
            addOfflineDataSources(selectedBasemap);
        } else if (Connectivity.isConnected(context)) {
            addOnlineDataSources();
        } else {
            presentBasemapsOptions();
        }
    }
    
    private void addOnlineDataSources() {
        //create OSM tile layer
        String defaultTilePID = mapActivity.getString(R.string.defaultTileLayerPID);
        String defaultTileURL = mapActivity.getString(R.string.defaultTileLayerURL);
        String defaultTileName = mapActivity.getString(R.string.defaultTileLayerName);
        String defaultTileAttribution = mapActivity.getString(R.string.defaultTileLayerAttribution);

        WebSourceTileLayer ws = new WebSourceTileLayer(defaultTilePID, defaultTileURL);
        ws.setName(defaultTileName).setAttribution(defaultTileAttribution);

        selectedBasemap = null;
        
        //add OSM tile layer to map
        mapView.setTileSource(ws);
    }
    
    public void presentBasemapsOptions() {
        //shared preferences private to mapActivity
        final SharedPreferences sharedPreferences = mapActivity.getPreferences(Context.MODE_PRIVATE);
        String previousBasemap = sharedPreferences.getString(PREVIOUS_BASEMAP, null);

        //create an array of all mbtile options
        final List<String> basemaps = new ArrayList<>();

        //when device is connected, HOT OSM Basemap is the first option
        if(Connectivity.isConnected(context)) {
            basemaps.add(mapActivity.getString(R.string.hotOSMOptionTitle));
        }

        //add mbtiles names from external storage
        File[] mbtiles = ExternalStorage.fetchMBTilesFiles();
        if (mbtiles.length > 0) {
            for (File file : mbtiles) {
                String filePath = file.getAbsolutePath();
                basemaps.add(filePath);
            }
        }

        if (basemaps.size() == 0) {
            Toast prompt = Toast.makeText(context, "Device is offline. Please add .mbtiles file to " + ExternalStorage.getMBTilesDir() + " or check out a deployment.", Toast.LENGTH_LONG);
            prompt.show();
            return;
        }

        //create dialog of mbtiles choices
        AlertDialog.Builder builder = new AlertDialog.Builder(mapActivity);
        builder.setTitle(mapActivity.getString(R.string.mbtilesChooserDialogTitle));

        int len = basemaps.size();
        String[] names = new String[len];
        for (int i = 0; i < len; ++i) {
            String basemap = basemaps.get(i);
            if (basemap.equals(mapActivity.getString(R.string.hotOSMOptionTitle))) {
                names[i] = basemap;
            } else {
                names[i] = new File(basemap).getName();
            }
        }


        //default mbtiles option is based on previous selections (persisted in shared preferences) or connectivity state of device
        int defaultRadioButtonIndex = 0;
        if(previousBasemap == null) {
            //if user DID NOT previously choose an mbtiles option...
            if(Connectivity.isConnected(context)) {
                //the first radio button (for HOT OSM) will be selected by default
                defaultRadioButtonIndex = 0;
                //the default selected option is HOT OSM
                selectedBasemap = basemaps.get(0); //default choice
            } else {
                defaultRadioButtonIndex = -1; //no selected radio button by default
            }
        } else {
            //if user previously chose an mbtiles option ...
            for(int i = 0; i < basemaps.size(); ++i) {
                String filePath = basemaps.get(i);
                if(filePath.equals(previousBasemap)) {
                    defaultRadioButtonIndex = i;
                    selectedBasemap = filePath;
                }
            }
            if (selectedBasemap == null) {
                selectedBasemap = basemaps.get(0);
            }
        }

        //add choices to dialog
        builder.setSingleChoiceItems(names, defaultRadioButtonIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user tapped on radio button and changed previous choice or default
                selectedBasemap = basemaps.get(which);

                //add user's choice to shared preferences key
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREVIOUS_BASEMAP, selectedBasemap);
                editor.apply();
            }
        });

        //handle OK tap event of dialog
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //user clicked OK
                if(selectedBasemap.equals(mapActivity.getString(R.string.hotOSMOptionTitle))) {

                    addOnlineDataSources();

                } else {

                    addOfflineDataSources(selectedBasemap);
                }
            }
        });

        //handle cancel button tap event of dialog
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //user clicked cancel
            }
        });

        //present dialog to user
        builder.show();

    }

    /**
     * For instantiating a map (when the device is offline) and initializing the default mbtiles layer, extent, and zoom level
     */
    private void addOfflineDataSources(String mbtilesPath) {
        File mbtilesFile = new File(mbtilesPath);
        if(!mbtilesFile.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mapActivity);
            builder.setTitle("Device is Offline");
            builder.setMessage("Please add MBTiles to " + ExternalStorage.getMBTilesDir());
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //placeholder
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        //add mbtiles to map
        mapView.setTileSource(new MBTilesLayer(mbtilesFile));
    }
}
